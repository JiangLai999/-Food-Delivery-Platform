package com.fooddelivery.user.ui.compose.screens

import android.graphics.BitmapFactory
import android.os.Bundle
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
import com.amap.api.maps.model.*
import com.fooddelivery.user.model.Order
import com.fooddelivery.user.model.RiderLocation
import com.fooddelivery.user.model.DeliveryTask
import com.fooddelivery.user.ui.compose.theme.*
import java.math.BigDecimal

private val brandOrange = Color(0xFFFF8C00)
private val successGreen = Color(0xFF22C55E)

private fun BigDecimal?.toDoubleValue(): Double = this?.toDouble() ?: 0.0

@Composable
fun MapDeliveryTrackingScreen(
    order: Order? = null,
    riderLocation: RiderLocation? = null,
    deliveryTask: DeliveryTask? = null,
    userAddress: String = "",
    userLatitude: Double = 0.0,
    userLongitude: Double = 0.0,
    merchantLatitude: Double = 0.0,
    merchantLongitude: Double = 0.0,
    merchantName: String = "商家",
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var aMap by remember { mutableStateOf<AMap?>(null) }
    var riderMarker by remember { mutableStateOf<Marker?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    var merchantMarker by remember { mutableStateOf<Marker?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    var routePoints by remember(deliveryTask) { mutableStateOf<List<LatLng>>(emptyList()) }
    
    LaunchedEffect(deliveryTask, merchantLatitude, merchantLongitude, userLatitude, userLongitude) {
        // 优先使用后端返回的路线数据
        if (deliveryTask?.routeData != null) {
            try {
                val points = parseRouteData(deliveryTask.routeData)
                if (points.isNotEmpty()) {
                    routePoints = points
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        
        // 如果没有后端路线数据，则生成模拟曲线路径
        if (routePoints.isEmpty() && merchantLatitude != 0.0 && merchantLongitude != 0.0 && 
            userLatitude != 0.0 && userLongitude != 0.0) {
            routePoints = generateCurvedPath(merchantLatitude, merchantLongitude, userLatitude, userLongitude)
        }
    }
    
    val estimatedTime = remember(riderLocation) {
        val time = riderLocation?.estimatedTime ?: 15
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MINUTE, time)
        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(calendar.time)
    }
    
    // 计算配送进度
    val currentPhase = riderLocation?.phase ?: deliveryTask?.let { 
        val pos = it.currentPosition ?: 0
        val total = routePoints.size.coerceAtLeast(1)
        val ratio = pos.toFloat() / total.toFloat()
        when {
            ratio < 0.15f -> "going_to_merchant"
            ratio < 0.20f -> "picking_up"
            ratio < 0.95f -> "delivering"
            else -> "arrived"
        }
    } ?: "going_to_merchant"
    
    val deliveryProgress = deliveryTask?.let { task ->
        val pos = task.currentPosition ?: 0
        val total = routePoints.size.coerceAtLeast(1)
        (pos.toFloat() / total.toFloat()).coerceIn(0.05f, 0.95f)
    } ?: 0.1f
    
    val progress = when (currentPhase) {
        "going_to_merchant" -> deliveryProgress.coerceAtMost(0.30f)
        "picking_up" -> 0.35f
        "delivering" -> (0.35f + deliveryProgress * 0.60f).coerceIn(0.35f, 0.95f)
        "arrived" -> 1.0f
        else -> 0.1f
    }

    DisposableEffect(Unit) {
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                com.amap.api.maps.MapView(ctx).apply {
                    onCreate(Bundle())
                    aMap = map.apply {
                        uiSettings.apply {
                            isZoomControlsEnabled = false
                            isCompassEnabled = true
                            isMyLocationButtonEnabled = false
                        }
                        setOnMapLoadedListener {
                            val defaultLat = 39.9042  // Beijing
                            val defaultLng = 116.4074
                            val centerLat = if (userLatitude != 0.0) userLatitude else 
                                           if (merchantLatitude != 0.0) merchantLatitude else defaultLat
                            val centerLng = if (userLongitude != 0.0) userLongitude else
                                           if (merchantLongitude != 0.0) merchantLongitude else defaultLng
                            moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(centerLat, centerLng), 14f))
                        }
                    }
                }
            },
            update = { mapView ->
                val currentMap = mapView.map
                
                // 更新用户位置标记
                if (userLatitude != 0.0 && userLongitude != 0.0) {
                    if (userMarker == null) {
                        userMarker = currentMap.addMarker(MarkerOptions()
                            .position(LatLng(userLatitude, userLongitude))
                            .title("配送地址")
                            .snippet(userAddress)
                            .icon(BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_mylocation)
                            )))
                    } else {
                        userMarker?.position = LatLng(userLatitude, userLongitude)
                    }
                }

                // 更新商家位置标记
                if (merchantLatitude != 0.0 && merchantLongitude != 0.0) {
                    if (merchantMarker == null) {
                        merchantMarker = currentMap.addMarker(MarkerOptions()
                            .position(LatLng(merchantLatitude, merchantLongitude))
                            .title(merchantName)
                            .icon(BitmapDescriptorFactory.fromBitmap(
                                BitmapFactory.decodeResource(context.resources, android.R.drawable.ic_menu_info_details)
                            )))
                    }
                }

                // 绘制配送路线
                if (routePoints.isNotEmpty()) {
                    if (routePolyline == null) {
                        routePolyline = currentMap.addPolyline(PolylineOptions()
                            .addAll(routePoints)
                            .width(12f)
                            .color(android.graphics.Color.parseColor("#FF8C00")))
                    }
                } else {
                    deliveryTask?.routeData?.let { routeData ->
                        if (routeData.isNotEmpty()) {
                            try {
                                val points = parseRouteData(routeData)
                                if (points.isNotEmpty()) {
                                    routePoints = points
                                }
                            } catch (e: Exception) {
                                // 解析路线失败
                            }
                        }
                    }
                }

                // 更新骑手位置标记
                riderLocation?.let { location ->
                    val lat = location.latitude.toDoubleValue()
                    val lng = location.longitude.toDoubleValue()
                    if (lat != 0.0 && lng != 0.0) {
                        if (riderMarker == null) {
                            riderMarker = currentMap.addMarker(MarkerOptions()
                                .position(LatLng(lat, lng))
                                .title(location.riderName ?: "骑手")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                        } else {
                            riderMarker?.position = LatLng(lat, lng)
                        }

                        // 调整地图视野以显示所有标记
                        val boundsBuilder = LatLngBounds.Builder()
                        userMarker?.position?.let { boundsBuilder.include(it) }
                        riderMarker?.position?.let { boundsBuilder.include(it) }
                        try {
                            currentMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
                        } catch (e: Exception) {
                            // 如果无法构建边界，直接移动到骑手位置
                            currentMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
                        }
                    }
                }
            }
        )

        // 顶部状态栏
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }

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

            // 预计送达时间卡片
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
                            estimatedTime,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Surface(
                        color = successGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            when (currentPhase) {
                                "going_to_merchant" -> "前往取餐"
                                "picking_up" -> "已取餐"
                                "delivering" -> "配送中"
                                "arrived" -> "已送达"
                                else -> "配送中"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = successGreen,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 配送进度条
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.95f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("配送进度", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val step1Active = progress >= 0.05f
                        val step2Active = progress >= 0.35f
                        val step3Active = progress >= 0.40f
                        val step4Active = progress >= 0.95f
                        DeliveryStep("已下单", step1Active)
                        DeliveryStep("取餐中", step2Active)
                        DeliveryStep("配送中", step3Active)
                        DeliveryStep("已送达", step4Active)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = successGreen,
                        trackColor = Color.LightGray,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 骑手信息卡片
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
                                riderLocation?.riderName ?: "骑手配送中",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                when (currentPhase) {
                                    "going_to_merchant" -> "正在赶往商家取餐"
                                    "picking_up" -> "正在商家取餐"
                                    "delivering" -> "正在为您配送"
                                    "arrived" -> "已送达"
                                    else -> "正在为您配送"
                                },
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        riderLocation?.estimatedTime?.let { time ->
                            Surface(
                                color = brandOrange.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "约${time}分钟",
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
                        horizontalArrangement = Arrangement.SpaceBetween,
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
                                        riderLocation?.riderName ?: "骑手",
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

private fun parseRouteData(routeData: String): List<LatLng> {
    val points = mutableListOf<LatLng>()
    try {
        val jsonArray = org.json.JSONArray(routeData)
        for (i in 0 until jsonArray.length()) {
            val point = jsonArray.getJSONObject(i)
            val lng = point.getDouble("longitude")
            val lat = point.getDouble("latitude")
            points.add(LatLng(lat, lng))
        }
    } catch (e: Exception) {
        // 解析失败
    }
    return points
}

private fun generateCurvedPath(
    merchantLng: Double, merchantLat: Double,
    userLng: Double, userLat: Double
): List<LatLng> {
    val points = mutableListOf<LatLng>()
    val numPoints = 30
    
    // 计算中点并添加偏移生成曲线路径
    val midLng = (merchantLng + userLng) / 2
    val midLat = (merchantLat + userLat) / 2
    
    // 垂直于直线的偏移方向
    val dx = userLng - merchantLng
    val dy = userLat - merchantLat
    val len = kotlin.math.sqrt(dx * dx + dy * dy)
    if (len == 0.0) return points
    
    // 偏移量 - 使路线弯曲
    val offset = 0.006
    val perpX = -dy / len * offset
    val perpY = dx / len * offset
    
    val curveLng = midLng + perpX
    val curveLat = midLat + perpY
    
    // 使用二次贝塞尔曲线生成平滑路径
    for (i in 0..numPoints) {
        val t = i.toDouble() / numPoints
        // 二次贝塞尔曲线: B(t) = (1-t)²P0 + 2(1-t)tP1 + t²P2
        val oneMinusT = 1 - t
        val lng = oneMinusT * oneMinusT * merchantLng + 2 * oneMinusT * t * curveLng + t * t * userLng
        val lat = oneMinusT * oneMinusT * merchantLat + 2 * oneMinusT * t * curveLat + t * t * userLat
        points.add(LatLng(lat, lng))
    }
    
    return points
}

@Composable
private fun DeliveryStep(label: String, isActive: Boolean) {
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
