package com.fooddelivery.user.ui.compose.screens

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.BusRouteResult
import com.amap.api.services.route.DriveRouteResult
import com.amap.api.services.route.RideRouteResult
import com.amap.api.services.route.RouteSearch
import com.amap.api.services.route.WalkRouteResult
import com.fooddelivery.user.model.Order
import com.fooddelivery.user.model.DeliveryTask
import com.fooddelivery.user.network.ApiClient
import com.fooddelivery.user.ui.compose.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 动态配送跟踪页面
 * 
 * 功能亮点：
 * 1. 使用高德地图 RouteSearch 进行真实骑行路线规划
 * 2. 自定义动画实现骑手图标沿路线平滑移动（替代SmoothMoveMarker）
 * 3. 到达终点后自动调用后端接口完成配送
 * 
 *
 */
private val brandOrange = Color(0xFFFF8C00)
private val successGreen = Color(0xFF22C55E)

/**
 * 配送状态枚举
 */
private enum class DeliveryState {
    LOADING,           // 加载中
    ROUTE_PLANNING,    // 路线规划中
    DELIVERING,        // 配送中
    ARRIVED,           // 已送达
    ERROR              // 错误
}

/**
 * 动态配送跟踪主屏幕
 * 
 * @param order 订单信息
 * @param deliveryTask 配送任务信息
 * @param userLatitude 用户纬度
 * @param userLongitude 用户经度
 * @param merchantLatitude 商家纬度
 * @param merchantLongitude 商家经度
 * @param merchantName 商家名称
 * @param onBack 返回回调
 */
@Composable
fun DynamicDeliveryTrackingScreen(
    order: Order? = null,
    deliveryTask: DeliveryTask? = null,
    userLatitude: Double = 0.0,
    userLongitude: Double = 0.0,
    merchantLatitude: Double = 0.0,
    merchantLongitude: Double = 0.0,
    merchantName: String = "商家",
    onBack: () -> Unit = {},
    onDeliveryComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // ========== 状态管理 ==========
    // 地图对象
    var aMap by remember { mutableStateOf<AMap?>(null) }
    var mapView by remember { mutableStateOf<com.amap.api.maps.MapView?>(null) }
    
    // 配送状态
    var deliveryStatus by remember { mutableStateOf(DeliveryState.LOADING) }
    
    // 路线规划结果点集合
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    
    // 骑手标记
    var riderMarker by remember { mutableStateOf<Marker?>(null) }
    
    // 用户位置标记
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    
    // 商家位置标记
    var merchantMarker by remember { mutableStateOf<Marker?>(null) }
    
    // 路线折线
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    
    // 是否已完成配送（防止重复提交）
    var hasCompletedDelivery by remember { mutableStateOf(false) }
    
    // 预计送达时间
    var estimatedTime by remember { mutableStateOf(15) }
    
    // 当前阶段描述
    var currentPhaseDesc by remember { mutableStateOf("正在获取配送信息...") }
    
    // 动画进度
    var progress by remember { mutableStateOf(0f) }
    
    // 动画对象
    var animator by remember { mutableStateOf<ValueAnimator?>(null) }
    
    // 路线搜索对象
    val routeSearch = remember {
        // 先设置隐私合规（在Application中已设置，这里重复设置不影响）
        RouteSearch(context)
    }
    
    // 默认坐标（北京）- 使用有效范围内的坐标
    val defaultUserLat = 39.904200  // 北京朝阳
    val defaultUserLng = 116.407400
    val defaultMerchantLat = 39.907761  // 北京
    val defaultMerchantLng = 116.478927
    
    // 安全获取坐标，处理无效坐标（中国范围：经度73-135，纬度3-54）
    fun getValidCoordinate(value: Double?, default: Double): Double {
        if (value == null || value < 1.0) {
            return default
        }
        // 检查是否在中国范围内
        if (value < 73 || value > 135) {  // 可能是经度超范围
            return default
        }
        // 额外检查：如果值在合理范围内但不在中国（需要两次判断，这里简化处理）
        return value
    }
    
    // 最终使用的坐标（优先使用deliveryTask中的坐标）
    val finalUserLat = remember(userLatitude, deliveryTask) { 
        val taskLat = deliveryTask?.deliveryLatitude?.toDouble()
        val lat = getValidCoordinate(taskLat, 0.0).let { if (it > 0) it else getValidCoordinate(userLatitude.toDouble(), defaultUserLat) }
        Log.d("DeliveryScreen", "finalUserLat: taskLat=$taskLat, userLatitude=$userLatitude, result=$lat")
        lat
    }
    val finalUserLng = remember(userLongitude, deliveryTask) { 
        val taskLng = deliveryTask?.deliveryLongitude?.toDouble()
        val lng = getValidCoordinate(taskLng, 0.0).let { if (it > 0) it else getValidCoordinate(userLongitude.toDouble(), defaultUserLng) }
        Log.d("DeliveryScreen", "finalUserLng: taskLng=$taskLng, userLongitude=$userLongitude, result=$lng")
        lng
    }
    val finalMerchantLat = remember(merchantLatitude, deliveryTask) { 
        val taskLat = deliveryTask?.pickupLatitude?.toDouble()
        val lat = getValidCoordinate(taskLat, 0.0).let { if (it > 0) it else getValidCoordinate(merchantLatitude.toDouble(), defaultMerchantLat) }
        Log.d("DeliveryScreen", "finalMerchantLat: taskLat=$taskLat, merchantLatitude=$merchantLatitude, result=$lat")
        lat
    }
    val finalMerchantLng = remember(merchantLongitude, deliveryTask) { 
        val taskLng = deliveryTask?.pickupLongitude?.toDouble()
        val lng = getValidCoordinate(taskLng, 0.0).let { if (it > 0) it else getValidCoordinate(merchantLongitude.toDouble(), defaultMerchantLng) }
        Log.d("DeliveryScreen", "finalMerchantLng: taskLng=$taskLng, merchantLongitude=$merchantLongitude, result=$lng")
        lng
    }

    // 打印传入参数的完整调试信息
    LaunchedEffect(Unit) {
        Log.d("DeliveryScreen", "=== 坐标参数调试 ===")
        Log.d("DeliveryScreen", "deliveryTask: $deliveryTask")
        Log.d("DeliveryScreen", "deliveryTask pickup: ${deliveryTask?.pickupLatitude}, ${deliveryTask?.pickupLongitude}")
        Log.d("DeliveryScreen", "deliveryTask delivery: ${deliveryTask?.deliveryLatitude}, ${deliveryTask?.deliveryLongitude}")
        Log.d("DeliveryScreen", "userLatitude=$userLatitude, userLongitude=$userLongitude")
        Log.d("DeliveryScreen", "merchantLatitude=$merchantLatitude, merchantLongitude=$merchantLongitude")
    }

    // 每次deliveryTask变化时打印
    LaunchedEffect(deliveryTask) {
        if (deliveryTask != null) {
            Log.e("DATA_CHECK", "=== 开始检查从后端拿到的数据 ===")
            Log.e("DATA_CHECK", "原始deliveryTask对象: orderId=${deliveryTask?.orderId}, status=${deliveryTask?.status}")
            Log.e("DATA_CHECK", "准备传给高德的变量值: " + 
                "\n 商户Lat: " + deliveryTask?.pickupLatitude +
                "\n 商户Lng: " + deliveryTask?.pickupLongitude +
                "\n 用户Lat: " + deliveryTask?.deliveryLatitude +
                "\n 用户Lng: " + deliveryTask?.deliveryLongitude)
            Log.e("DATA_CHECK", "=== 检查结束 ===")
            
            // 打印最终使用的坐标
            Log.e("DATA_CHECK", "最终使用的坐标: merchant=($finalMerchantLng, $finalMerchantLat), user=($finalUserLng, $finalUserLat)")
        }
    }
    
    // 打印最终坐标（每次变化时）
    LaunchedEffect(finalMerchantLat, finalMerchantLng, finalUserLat, finalUserLng) {
        Log.e("DATA_CHECK", "坐标更新: merchant=($finalMerchantLng, $finalMerchantLat), user=($finalUserLng, $finalUserLat)")
    }
    
    // 骑手实时位置（从后端获取）
    var riderLocationLat by remember { mutableStateOf<Double?>(null) }
    var riderLocationLng by remember { mutableStateOf<Double?>(null) }
    var riderPhase by remember { mutableStateOf<String?>(null) }
    
    // 标记是否已初始化路线
    var routeInitialized by remember { mutableStateOf(false) }
    
    // ========== 第一步：初始化地图并立即生成路线 ==========
    LaunchedEffect(finalMerchantLat, finalMerchantLng, finalUserLat, finalUserLng) {
        // 检查坐标是否有效
        if (finalMerchantLat != 0.0 && finalMerchantLng != 0.0 && 
            finalUserLat != 0.0 && finalUserLng != 0.0) {
            
            Log.d("DeliveryScreen", "初始化路线: merchant=$finalMerchantLng,$finalMerchantLat -> user=$finalUserLng,$finalUserLat")
            
            // 立即生成模拟路径，不等待deliveryTask
            routePoints = generateSimulatedPath(finalMerchantLng, finalMerchantLat, finalUserLng, finalUserLat)
            Log.d("DeliveryScreen", "立即生成了 ${routePoints.size} 个路线点")
            
            if (routePoints.isNotEmpty()) {
                deliveryStatus = DeliveryState.DELIVERING
                currentPhaseDesc = "骑手正在火速赶来"
                routeInitialized = true
            }
        } else {
            Log.w("DeliveryScreen", "坐标无效，跳过初始化: merchant=$finalMerchantLng,$finalMerchantLat, user=$finalUserLng,$finalUserLat")
        }
    }
    
    // ========== 定期从后端获取骑手位置并更新进度 ==========
    LaunchedEffect(order?.id) {
        val orderId = order?.id ?: return@LaunchedEffect
        while (deliveryStatus != DeliveryState.ARRIVED && deliveryStatus != DeliveryState.ERROR) {
            try {
                val apiService = ApiClient.getInstance().apiService
                
                // 获取骑手位置
                apiService.getDeliveryLocation(orderId).enqueue(object : Callback<com.fooddelivery.user.model.Result<com.fooddelivery.user.model.DeliveryLocation>> {
                    override fun onResponse(call: Call<com.fooddelivery.user.model.Result<com.fooddelivery.user.model.DeliveryLocation>>, response: Response<com.fooddelivery.user.model.Result<com.fooddelivery.user.model.DeliveryLocation>>) {
                        val body = response.body()
                        if (body?.code == 200 && body.data != null) {
                            val location = body.data
                            
                            // 根据currentPosition从routeData计算骑手位置，确保与路线一致
                            val currentPosition = location.currentPosition ?: 0
                            if (deliveryTask?.routeData != null && currentPosition > 0) {
                                try {
                                    val gson = Gson()
                                    val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                                    val routeDataList: List<Map<String, Any>> = gson.fromJson(deliveryTask.routeData, type)
                                    if (currentPosition < routeDataList.size) {
                                        val step = routeDataList[currentPosition]
                                        val rLat = (step["latitude"] as? Number)?.toDouble()
                                        val rLng = (step["longitude"] as? Number)?.toDouble()
                                        if (rLat != null && rLng != null) {
                                            riderLocationLat = rLat
                                            riderLocationLng = rLng
                                            Log.d("DeliveryScreen", "从routeData计算骑手位置: pos=$currentPosition, lat=$rLat, lng=$rLng")
                                        } else {
                                            riderLocationLat = location.latitude?.toDouble()
                                            riderLocationLng = location.longitude?.toDouble()
                                        }
                                    } else {
                                        riderLocationLat = location.latitude?.toDouble()
                                        riderLocationLng = location.longitude?.toDouble()
                                    }
                                } catch (e: Exception) {
                                    riderLocationLat = location.latitude?.toDouble()
                                    riderLocationLng = location.longitude?.toDouble()
                                    Log.e("DeliveryScreen", "解析routeData失败: ${e.message}")
                                }
                            } else {
                                riderLocationLat = location.latitude?.toDouble()
                                riderLocationLng = location.longitude?.toDouble()
                            }
                            
                            riderPhase = location.phase
                            estimatedTime = location.estimatedTime ?: 1

                            // 根据配送任务的状态计算进度
                            val totalPoints = 90 // 总共90个路径点（12+6+72）
                            progress = (currentPosition.toFloat() / totalPoints.toFloat()).coerceIn(0f, 1f)

                            Log.d("DeliveryScreen", "进度更新: currentPosition=$currentPosition, totalPoints=$totalPoints, progress=$progress, phase=${location.phase}")

                            // 更新阶段描述
                            currentPhaseDesc = when (location.phase) {
                                "going_to_merchant" -> "骑手正在前往商家"
                                "picking_up" -> "骑手正在取餐"
                                "delivering" -> {
                                    val dist = location.distanceToUser ?: 0
                                    if (dist > 0) "距离您还有 ${dist}米" else "配送中"
                                }
                                "arrived" -> "已送达"
                                else -> location.description ?: "配送中"
                            }

                            // 检查是否已送达（状态为2或距离小于10米）
                            if (location.status == 2 || location.phase == "arrived" || (location.distanceToUser != null && location.distanceToUser <= 10)) {
                                if (!hasCompletedDelivery) {
                                    hasCompletedDelivery = true
                                    deliveryStatus = DeliveryState.ARRIVED
                                    progress = 1f
                                    currentPhaseDesc = "外卖已送达，祝您用餐愉快！"
                                    completeDelivery(orderId, context, onDeliveryComplete)
                                }
                            }
                        } else {
                            Log.w("DeliveryScreen", "获取骑手位置失败: code=${body?.code}, message=${body?.message}")
                        }
                    }
                    override fun onFailure(call: Call<com.fooddelivery.user.model.Result<com.fooddelivery.user.model.DeliveryLocation>>, t: Throwable) {
                        Log.e("DeliveryScreen", "获取骑手位置失败: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("DeliveryScreen", "获取骑手位置异常: ${e.message}")
            }
            // 每3秒获取一次骑手位置
            kotlinx.coroutines.delay(3000)
        }
    }
    
    // 清理函数
    DisposableEffect(Unit) {
        onDispose {
            animator?.cancel()
            mapView?.onDestroy()
        }
    }
    
    // ========== 第二步：创建地图视图 ==========
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // 创建高德地图 MapView
                val mapView = com.amap.api.maps.MapView(ctx).apply {
                    // 必须在 onCreate 中调用 MapView.onCreate()
                    onCreate(Bundle())
                    
                    // 获取 AMap 对象并配置
                    aMap = map.apply {
                        // 地图加载完成监听
                        setOnMapLoadedListener {
                            // 使用经过验证的坐标
                            val validUserLat = if (finalUserLat > 0) finalUserLat else defaultUserLat
                            val validUserLng = if (finalUserLng > 0) finalUserLng else defaultUserLng
                            val validMerchantLat = if (finalMerchantLat > 0) finalMerchantLat else defaultMerchantLat
                            val validMerchantLng = if (finalMerchantLng > 0) finalMerchantLng else defaultMerchantLng
                            
                            // 计算中心点
                            val centerLat = if (validUserLat > 0) validUserLat else 
                                           if (validMerchantLat > 0) validMerchantLat else defaultUserLat
                            val centerLng = if (validUserLng > 0) validUserLng else
                                           if (validMerchantLng > 0) validMerchantLng else defaultUserLng
                            
                            // 移动到中心位置
                            if (centerLat > 0 && centerLng > 0) {
                                moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(centerLat, centerLng), 15f))
                            }
                        }
                        
                        // 配置 UI 设置
                        uiSettings.apply {
                            // 隐藏缩放按钮（自定义缩放按钮更美观）
                            isZoomControlsEnabled = false
                            // 显示指南针
                            isCompassEnabled = true
                            // 隐藏定位按钮
                            isMyLocationButtonEnabled = false
                        }
                    }
                }
                
                // 设置路线搜索监听器（正确的方法名是 setRouteSearchListener）
                routeSearch.setRouteSearchListener(object : RouteSearch.OnRouteSearchListener {
                    override fun onBusRouteSearched(p0: com.amap.api.services.route.BusRouteResult?, p1: Int) {
                        Log.d("DeliveryScreen", "公交路线搜索完成: $p1")
                    }
                    
                    override fun onDriveRouteSearched(p0: com.amap.api.services.route.DriveRouteResult?, p1: Int) {
                        Log.d("DeliveryScreen", "驾车路线搜索完成: $p1")
                    }
                    
                    override fun onWalkRouteSearched(result: com.amap.api.services.route.WalkRouteResult?, errorCode: Int) {
                        Log.d("DeliveryScreen", "步行路线搜索完成: $errorCode")
                        
                        if (errorCode == 1000 && result != null && result.paths != null && result.paths.isNotEmpty()) {
                            // 步行路线规划成功
                            val walkPath = result.paths[0]
                            
                            // 提取路线点
                            val points = mutableListOf<LatLng>()
                            for (step in walkPath.steps) {
                                val stepPoints = step.polyline
                                for (point in stepPoints) {
                                    points.add(LatLng(point.latitude, point.longitude))
                                }
                            }
                            
                            if (points.isNotEmpty()) {
                                routePoints = points
                                deliveryStatus = DeliveryState.DELIVERING
                                currentPhaseDesc = "骑手正在火速赶来"
                            }
                        } else {
                            // 路线规划失败，使用模拟路径
                            Log.w("DeliveryScreen", "路线规划失败，errorCode: $errorCode，使用模拟路径")
                            routePoints = generateSimulatedPath(finalMerchantLng, finalMerchantLat, finalUserLng, finalUserLat)
                            deliveryStatus = DeliveryState.DELIVERING
                            currentPhaseDesc = "骑手正在火速赶来"
                        }
                    }
                    
                    override fun onRideRouteSearched(result: com.amap.api.services.route.RideRouteResult?, errorCode: Int) {
                        Log.d("DeliveryScreen", "骑行路线搜索完成: $errorCode")
                        
                        if (errorCode == 1000 && result != null && result.paths != null && result.paths.isNotEmpty()) {
                            // 骑行路线规划成功
                            val ridePath = result.paths[0]
                            
                            // 提取路线点
                            val points = mutableListOf<LatLng>()
                            for (step in ridePath.steps) {
                                val stepPoints = step.polyline
                                for (point in stepPoints) {
                                    points.add(LatLng(point.latitude, point.longitude))
                                }
                            }
                            
                            if (points.isNotEmpty()) {
                                routePoints = points
                                deliveryStatus = DeliveryState.DELIVERING
                                currentPhaseDesc = "骑手正在火速赶来"
                                
                                // 计算预计送达时间（假设骑行速度 15km/h）
                                val distanceMeters = ridePath.distance
                                val timeMinutes = (distanceMeters / 1000.0 / 15.0 * 60).toInt() + 3 // 加上3分钟取餐时间
                                estimatedTime = timeMinutes.coerceAtLeast(3)
                            }
                        } else {
                            // 骑行路线规划失败，尝试步行路线
                            Log.w("DeliveryScreen", "骑行路线规划失败，errorCode: $errorCode，尝试步行路线")
                            planWalkRoute(routeSearch, finalMerchantLat, finalMerchantLng, finalUserLat, finalUserLng)
                        }
                    }
                })
                
                mapView
            },
            update = { mapView ->
                val currentMap = mapView.map
                
                // ========== 添加商家位置标记 ==========
                if (finalMerchantLat != 0.0 && finalMerchantLng != 0.0) {
                    if (merchantMarker == null) {
                        // 创建商家图标（使用系统图标）
                        merchantMarker = currentMap.addMarker(MarkerOptions()
                            .position(LatLng(finalMerchantLat, finalMerchantLng))
                            .title(merchantName)
                            .snippet("取餐地点")
                            .icon(BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_info_details)
                            ))
                            .draggable(false))
                    }
                }
                
                // ========== 添加用户位置标记 ==========
                if (finalUserLat != 0.0 && finalUserLng != 0.0) {
                    if (userMarker == null) {
                        userMarker = currentMap.addMarker(MarkerOptions()
                            .position(LatLng(finalUserLat, finalUserLng))
                            .title("配送地址")
                            .snippet("送餐地点")
                            .icon(BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_mylocation)
                            ))
                            .draggable(false))
                    }
                }
                
                // ========== 执行路线规划 ==========
                // 立即生成模拟路径作为默认显示
                if (routePoints.isEmpty()) {
                    // 首先生成模拟路径（确保地图上有路线显示）
                    routePoints = generateSimulatedPath(finalMerchantLng, finalMerchantLat, finalUserLng, finalUserLat)
                    deliveryStatus = DeliveryState.DELIVERING
                    currentPhaseDesc = "骑手正在火速赶来"
                    
                    // 尝试骑行路线规划（可能失败，但地图已有路线）
                    planRideRoute(routeSearch, finalMerchantLat, finalMerchantLng, finalUserLat, finalUserLng)
                }
                
                // ========== 绘制商家标记 ==========
                if (merchantMarker == null && finalMerchantLat != 0.0 && finalMerchantLng != 0.0) {
                    val markerOptions = MarkerOptions()
                        .position(LatLng(finalMerchantLat, finalMerchantLng))
                        .title("商家")
                        .snippet(merchantName)
                        .anchor(0.5f, 1.0f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    merchantMarker = currentMap.addMarker(markerOptions)
                }
                
                // ========== 绘制用户标记 ==========
                if (userMarker == null && finalUserLat != 0.0 && finalUserLng != 0.0) {
                    val markerOptions = MarkerOptions()
                        .position(LatLng(finalUserLat, finalUserLng))
                        .title("送餐地址")
                        .anchor(0.5f, 1.0f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    userMarker = currentMap.addMarker(markerOptions)
                }
                
                // ========== 绘制路线（优先使用后端提供的路线数据）==========
                Log.d("DeliveryScreen", "routePoints empty=${routePoints.isEmpty()}, hasRouteData=${deliveryTask?.routeData != null}")
                
                // 优先使用deliveryTask中的路线数据（与后端一致）
                if (deliveryTask?.routeData != null) {
                    // 重新解析路线（确保与骑手位置一致）
                    try {
                        val gson = Gson()
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val routeDataList: List<Map<String, Any>> = gson.fromJson(deliveryTask.routeData, type)
                        val newRoutePoints = routeDataList.mapNotNull { point ->
                             val lng = (point["longitude"] as? Number)?.toDouble()
                             val lat = (point["latitude"] as? Number)?.toDouble()
                             // 严格验证坐标范围：经度 -180~180，纬度 -90~90，且在中国范围内
                             if (lng != null && lat != null && 
                                 lng >= -180 && lng <= 180 && 
                                 lat >= -90 && lat <= 90 &&
                                 lng >= 73 && lng <= 135 &&
                                 lat >= 3 && lat <= 54) {
                                 LatLng(lat, lng)
                             } else {
                                 Log.w("DeliveryScreen", "无效坐标: lng=$lng, lat=$lat")
                                 null
                             }
                        }
                        if (newRoutePoints.isNotEmpty()) {
                            routePoints = newRoutePoints
                            Log.d("DeliveryScreen", "从deliveryTask解析了 ${routePoints.size} 个路径点")
                        } else {
                            Log.w("DeliveryScreen", "路线数据全部无效，使用模拟路径")
                        }
                    } catch (e: Exception) {
                        Log.e("DeliveryScreen", "解析路线数据失败: ${e.message}")
                    }
                }
                
                // 如果仍然没有路线，生成模拟路径
                if (routePoints.isEmpty()) {
                    Log.d("DeliveryScreen", "生成模拟路径: merchant=$finalMerchantLng,$finalMerchantLat -> user=$finalUserLng,$finalUserLat")
                    routePoints = generateSimulatedPath(finalMerchantLng, finalMerchantLat, finalUserLng, finalUserLat)
                    Log.d("DeliveryScreen", "生成了 ${routePoints.size} 个模拟路径点")
                }
                
                // 绘制路线
                if (routePoints.isNotEmpty()) {
                    Log.d("DeliveryScreen", "绘制路线，点数: ${routePoints.size}, routePolyline=$routePolyline")
                    // 移除旧的路线（如果有）
                    routePolyline?.remove()
                    routePolyline = currentMap.addPolyline(PolylineOptions()
                        .addAll(routePoints)
                        .width(15f)
                        .color(android.graphics.Color.parseColor("#FF8C00"))
                        .setUseTexture(false))
                    
                    // 调整地图视野
                    val boundsBuilder = LatLngBounds.Builder()
                    merchantMarker?.position?.let { boundsBuilder.include(it) }
                    userMarker?.position?.let { boundsBuilder.include(it) }
                    for (point in routePoints) {
                        boundsBuilder.include(point)
                    }
                    try {
                        currentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
                    } catch (e: Exception) {
                        val centerLat = (finalMerchantLat + finalUserLat) / 2
                        val centerLng = (finalMerchantLng + finalUserLng) / 2
                        currentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(centerLat, centerLng), 14f))
                    }
                }
                
                // ========== 创建或更新骑手标记（使用后端真实位置）==========
                if (deliveryStatus == DeliveryState.DELIVERING || deliveryStatus == DeliveryState.ROUTE_PLANNING) {
                    // 如果还没有骑手标记，先创建一个
                    if (riderMarker == null) {
                        val markerOptions = MarkerOptions()
                            .position(LatLng(finalMerchantLat, finalMerchantLng))
                            .title("骑手")
                            .snippet("配送中")
                        try {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(com.fooddelivery.user.R.drawable.ic_rider))
                        } catch (e: Exception) {
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        }
                        riderMarker = currentMap.addMarker(markerOptions)
                        deliveryStatus = DeliveryState.DELIVERING
                    }
                    
                    // 如果从后端获取到了骑手位置，更新标记
                    val rLat = riderLocationLat
                    val rLng = riderLocationLng
                    // 验证骑手坐标在中国范围内
                    if (rLat != null && rLng != null && rLat >= 3 && rLat <= 54 && rLng >= 73 && rLng <= 135) {
                        riderMarker?.position = LatLng(rLat, rLng)
                        // 移动地图视角跟随骑手
                        currentMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(rLat, rLng)))
                    }
                }
            }
        )
        
        // ========== 顶部状态栏 ==========
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
                
                // 订单号
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "订单号: #${order?.orderNo?.takeLast(7) ?: "---"}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // 分享按钮（预留）
                Surface(
                    onClick = { },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Share, contentDescription = "分享")
                    }
                }
            }
            
            // ========== 预计送达时间卡片 ==========
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = brandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("预计送达", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            if (deliveryStatus == DeliveryState.ARRIVED) "已送达" else "约${estimatedTime}分钟",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 配送状态标签
                    Surface(
                        color = when (deliveryStatus) {
                            DeliveryState.LOADING -> Color.Gray
                            DeliveryState.ROUTE_PLANNING -> Color(0xFF3B82F6)
                            DeliveryState.DELIVERING -> successGreen
                            DeliveryState.ARRIVED -> successGreen
                            DeliveryState.ERROR -> Color.Red
                        }.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            when (deliveryStatus) {
                                DeliveryState.LOADING -> "加载中"
                                DeliveryState.ROUTE_PLANNING -> "规划路线"
                                DeliveryState.DELIVERING -> "配送中"
                                DeliveryState.ARRIVED -> "已送达"
                                DeliveryState.ERROR -> "错误"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (deliveryStatus) {
                                DeliveryState.LOADING -> Color.Gray
                                DeliveryState.ROUTE_PLANNING -> Color(0xFF3B82F6)
                                DeliveryState.DELIVERING -> successGreen
                                DeliveryState.ARRIVED -> successGreen
                                DeliveryState.ERROR -> Color.Red
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // ========== 配送进度条 ==========
            Spacer(modifier = Modifier.weight(1f))
            
            // ========== 骑手信息卡片 ==========
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                deliveryTask?.riderName ?: "骑手配送中",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                currentPhaseDesc,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        if (deliveryStatus != DeliveryState.ARRIVED) {
                            Surface(
                                color = brandOrange.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "约${estimatedTime}分钟",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandOrange,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = brandOrange.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.LocalShipping,
                                            contentDescription = null,
                                            tint = brandOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                // 在线状态指示
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .size(12.dp)
                                        .background(successGreen, CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        deliveryTask?.riderName ?: "骑手",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = brandOrange,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        " 4.9",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = brandOrange
                                    )
                                }
                                Text(
                                    "专业骑手",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 骑行路线规划
 * 使用高德地图 RouteSearch 进行骑行路线规划
 * 
 * @param routeSearch 路线搜索对象
 * @param startLat 起点纬度
 * @param startLng 起点经度
 * @param endLat 终点纬度
 * @param endLng 终点经度
 */
private fun planRideRoute(
    routeSearch: RouteSearch,
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double
) {
    // 验证坐标有效性，防止"非法坐标值"错误
    if (startLat <= 0 || startLng <= 0 || endLat <= 0 || endLng <= 0 ||
        startLat > 90 || startLng > 180 || endLat > 90 || endLng > 180) {
        Log.w("DeliveryScreen", "坐标无效，跳过路线规划: start=($startLng,$startLat), end=($endLng,$endLat)")
        return
    }
    
    try {
        // 打印调试坐标
        Log.d("DeliveryScreen", "planRideRoute: start=($startLng,$startLat), end=($endLng,$endLat)")
        
        // 创建起点和终点
        val startPoint = LatLonPoint(startLat, startLng)
        val endPoint = LatLonPoint(endLat, endLng)
        
        // 创建起终点参数
        val fromAndTo = RouteSearch.FromAndTo(startPoint, endPoint)
        
        // 创建骑行路线查询参数
        // 骑行模式：0-普通自行车，1-电动自行车
        val query = RouteSearch.RideRouteQuery(fromAndTo, 0)
        
        // 异步搜索骑行路线
        routeSearch.calculateRideRouteAsyn(query)
        
        Log.d("DeliveryScreen", "骑行路线规划请求已发送")
    } catch (e: Exception) {
        Log.e("DeliveryScreen", "骑行路线规划异常: ${e.message}")
    }
}

/**
 * 步行路线规划（骑行失败时的备选方案）
 */
private fun planWalkRoute(
    routeSearch: RouteSearch,
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double
) {
    // 验证坐标有效性
    if (startLat <= 0 || startLng <= 0 || endLat <= 0 || endLng <= 0 ||
        startLat > 90 || startLng > 180 || endLat > 90 || endLng > 180) {
        Log.w("DeliveryScreen", "坐标无效，跳过步行路线规划: start=($startLng,$startLat), end=($endLng,$endLat)")
        return
    }
    
    try {
        val startPoint = LatLonPoint(startLat, startLng)
        val endPoint = LatLonPoint(endLat, endLng)
        
        // 创建起终点参数
        val fromAndTo = RouteSearch.FromAndTo(startPoint, endPoint)
        
        // 步行模式：0-推荐路线，1-最少步行，2-最舒适
        val query = RouteSearch.WalkRouteQuery(fromAndTo, 0)
        
        routeSearch.calculateWalkRouteAsyn(query)
        
        Log.d("DeliveryScreen", "步行路线规划请求已发送")
    } catch (e: Exception) {
        Log.e("DeliveryScreen", "步行路线规划异常: ${e.message}")
    }
}

/**
 * 生成模拟曲线路径
 * 当高德地图路线规划失败时使用
 */
private fun generateSimulatedPath(
    merchantLng: Double, merchantLat: Double,
    userLng: Double, userLat: Double
): List<LatLng> {
    val points = mutableListOf<LatLng>()
    
    // 严格验证坐标范围，使用默认值替换无效坐标
    // 中国范围内：经度 73-135，纬度 3-54
    val mLng = if (merchantLng < 73 || merchantLng > 135 || merchantLat < 3 || merchantLat > 54) {
        Log.w("DeliveryScreen", "商家坐标无效，使用默认: $merchantLng, $merchantLat")
        116.478927
    } else merchantLng
    
    val mLat = if (merchantLng < 73 || merchantLng > 135 || merchantLat < 3 || merchantLat > 54) {
        39.907761
    } else merchantLat
    
    val uLng = if (userLng < 73 || userLng > 135 || userLat < 3 || userLat > 54) {
        Log.w("DeliveryScreen", "用户坐标无效，使用默认: $userLng, $userLat")
        116.407400
    } else userLng
    
    val uLat = if (userLng < 73 || userLng > 135 || userLat < 3 || userLat > 54) {
        39.904200
    } else userLat
    
    Log.d("DeliveryScreen", "generateSimulatedPath: merchant=$mLng,$mLat, user=$uLng,$uLat")
    
    val numPoints = 75  // 增加点数，延长配送时间
    
    // 计算中点 - 使用验证后的坐标
    val midLng = (mLng + uLng) / 2
    val midLat = (mLat + uLat) / 2
    
    // 计算垂直偏移方向 - 使用验证后的坐标
    val dx = uLng - mLng
    val dy = uLat - mLat
    val len = kotlin.math.sqrt(dx * dx + dy * dy)
    if (len == 0.0) return points
    
    // 偏移量，使路线弯曲
    val offset = 0.006
    val perpX = -dy / len * offset
    val perpY = dx / len * offset
    
    val curveLng = midLng + perpX
    val curveLat = midLat + perpY
    
    // 使用二次贝塞尔曲线生成平滑路径 - 使用验证后的坐标
    for (i in 0..numPoints) {
        val t = i.toDouble() / numPoints
        val oneMinusT = 1 - t
        val lng = oneMinusT * oneMinusT * mLng + 2 * oneMinusT * t * curveLng + t * t * uLng
        val lat = oneMinusT * oneMinusT * mLat + 2 * oneMinusT * t * curveLat + t * t * uLat
        points.add(LatLng(lat, lng))
    }
    
    return points
}

/**
 * 完成配送
 * 骑手到达目的地后，客户端主动调用此接口通知后端订单已完成
 * 这是整个配送流程的关键闭环操作
 * 
 * @param orderId 订单ID
 * @param context Android 上下文
 * @param onSuccess 成功回调
 */
private fun completeDelivery(orderId: Long, context: android.content.Context, onSuccess: () -> Unit) {
    if (orderId <= 0) {
        Log.w("DeliveryScreen", "无效的订单ID: $orderId")
        return
    }
    
    Log.d("DeliveryScreen", "开始调用完成配送接口，订单ID: $orderId")
    
    // 调用后端完成配送接口
    ApiClient.getInstance().apiService.completeDelivery(orderId).enqueue(object : Callback<com.fooddelivery.user.model.Result<String>> {
        override fun onResponse(
            call: Call<com.fooddelivery.user.model.Result<String>>,
            response: Response<com.fooddelivery.user.model.Result<String>>
        ) {
            val result = response.body()
            if (result != null && result.isSuccess) {
                Log.d("DeliveryScreen", "配送完成接口调用成功: ${result.message}")
                Toast.makeText(context, "配送任务已完成", Toast.LENGTH_SHORT).show()
                onSuccess()
            } else {
                Log.e("DeliveryScreen", "配送完成接口调用失败: ${result?.message}")
                Toast.makeText(context, "配送完成确认失败: ${result?.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        override fun onFailure(call: Call<com.fooddelivery.user.model.Result<String>>, t: Throwable) {
            Log.e("DeliveryScreen", "配送完成接口网络错误: ${t.message}")
            Toast.makeText(context, "网络错误: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    })
}

/**
 * 计算两个经纬度点之间的线性插值位置
 * @param start 起点
 * @param end 终点  
 * @param fraction 插值比例（0-1）
 * @return 插值后的经纬度点
 */
private fun interpolate(start: LatLng, end: LatLng, fraction: Double): LatLng {
    val lat = start.latitude + (end.latitude - start.latitude) * fraction
    val lng = start.longitude + (end.longitude - start.longitude) * fraction
    return LatLng(lat, lng)
}

/**
 * 计算路线总长度（米）
 * 计算路线总长度（米）
 * 使用 Haversine 公式计算两点之间的距离
 */
private fun calculateTotalDistance(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0
    
    var totalDistance = 0.0
    for (i in 0 until points.size - 1) {
        totalDistance += calculateDistance(points[i], points[i + 1])
    }
    return totalDistance
}

/**
 * 使用 Haversine 公式计算两个经纬度点之间的距离（米）
 */
private fun calculateDistance(start: LatLng, end: LatLng): Double {
    val R = 6371000.0  // 地球半径（米）
    val lat1 = Math.toRadians(start.latitude)
    val lat2 = Math.toRadians(end.latitude)
    val deltaLat = Math.toRadians(end.latitude - start.latitude)
    val deltaLng = Math.toRadians(end.longitude - start.longitude)
    
    val a = kotlin.math.sin(deltaLat / 2) * kotlin.math.sin(deltaLat / 2) +
            kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
            kotlin.math.sin(deltaLng / 2) * kotlin.math.sin(deltaLng / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    
    return R * c
}

/**
 * 配送步骤指示器组件
 */
@Composable
fun DeliveryStepIndicator(label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(successGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.LightGray, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            color = if (isActive) successGreen else Color.Gray
        )
    }
}
