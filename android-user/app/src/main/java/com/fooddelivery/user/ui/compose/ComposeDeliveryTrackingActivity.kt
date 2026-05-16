package com.fooddelivery.user.ui.compose

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amap.api.location.AMapLocation
import com.fooddelivery.user.model.RiderLocation
import com.fooddelivery.user.ui.compose.screens.DeliveryTrackingScreen
import com.fooddelivery.user.ui.compose.screens.DynamicDeliveryTrackingScreen
import com.fooddelivery.user.ui.compose.screens.MapDeliveryTrackingScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel
import com.fooddelivery.user.utils.LocationHelper

private val brandOrange = Color(0xFFFF8C00)

class ComposeDeliveryTrackingActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private var locationHelper: LocationHelper? = null
    private var userLocation: AMapLocation? = null
    private var hasLocationPermission = false
    private var mapView: com.amap.api.maps.MapView? = null
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            startLocation()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkLocationPermission()
        
        setContent {
            FoodDeliveryTheme {
                val context = LocalContext.current
                val order by viewModel.currentOrder.collectAsStateWithLifecycle()
                val riderLocation by viewModel.riderLocation.collectAsStateWithLifecycle()
                val deliveryTask by viewModel.deliveryTask.collectAsStateWithLifecycle()
                
                val orderId = intent.getLongExtra("orderId", -1L)
                
                LaunchedEffect(orderId) {
                    if (orderId > 0) {
                        viewModel.fetchOrderDetail(orderId)
                        viewModel.fetchDeliveryLocation(orderId)
                        viewModel.fetchDeliveryTask(orderId)
                    }
                }
                
                LaunchedEffect(order) {
                    order?.let {
                        viewModel.fetchDeliveryLocation(orderId)
                    }
                }
                
                LaunchedEffect(orderId) {
                    while (this@ComposeDeliveryTrackingActivity.isFinishing.not()) {
                        kotlinx.coroutines.delay(3000)
                        viewModel.fetchDeliveryLocation(orderId)
                    }
                }
                
                val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                viewModel.fetchDeliveryLocation(orderId)
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                
                val orderStatus = order?.status ?: 0
                
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F7F5))) {
                    when (orderStatus) {
                        0 -> WaitingForPaymentScreen(
                            order = order,
                            onBack = { finish() }
                        )
                        1 -> WaitingForMerchantScreen(
                            order = order,
                            onBack = { finish() }
                        )
                        2, 3 -> {
                            // 优先使用动态路线规划页面（基于高德RouteSearch）
                            val useNewMap = remember { mutableStateOf(true) }
                            if (useNewMap.value) {
                                DynamicDeliveryTrackingScreen(
                                    order = order,
                                    deliveryTask = deliveryTask,
                                    userLatitude = order?.receiverLatitude?.toDouble() ?: userLocation?.latitude ?: 0.0,
                                    userLongitude = order?.receiverLongitude?.toDouble() ?: userLocation?.longitude ?: 0.0,
                                    merchantLatitude = order?.merchant?.latitude?.toDouble() ?: 0.0,
                                    merchantLongitude = order?.merchant?.longitude?.toDouble() ?: 0.0,
                                    merchantName = order?.merchant?.name ?: "商家",
                                    onBack = { finish() },
                                    onDeliveryComplete = { finish() }
                                )
                            } else {
                                MapDeliveryTrackingScreen(
                                    order = order,
                                    riderLocation = riderLocation,
                                    deliveryTask = deliveryTask,
                                    userAddress = order?.receiverAddress ?: "",
                                    userLatitude = order?.receiverLatitude?.toDouble() ?: userLocation?.latitude ?: 0.0,
                                    userLongitude = order?.receiverLongitude?.toDouble() ?: userLocation?.longitude ?: 0.0,
                                    merchantLatitude = order?.merchant?.latitude?.toDouble() ?: 0.0,
                                    merchantLongitude = order?.merchant?.longitude?.toDouble() ?: 0.0,
                                    merchantName = order?.merchant?.name ?: "商家",
                                    onBack = { finish() }
                                )
                            }
                        }
                        4 -> OrderCompletedScreen(
                            order = order,
                            onBack = { finish() }
                        )
                        5 -> OrderCancelledScreen(
                            order = order,
                            onBack = { finish() }
                        )
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("未知订单状态")
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun checkLocationPermission() {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasLocationPermission) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            startLocation()
        }
    }
    
    private fun startLocation() {
        locationHelper = LocationHelper(this)
        locationHelper?.setLocationListener(object : LocationHelper.LocationListener {
            override fun onLocationSuccess(location: AMapLocation) {
                userLocation = location
            }
            
            override fun onLocationError(errorCode: Int, errorMsg: String) {
            }
        })
        locationHelper?.startLocation()
    }
    
    override fun onResume() {
        super.onResume()
        locationHelper?.startLocation()
        mapView?.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        locationHelper?.stopLocation()
        mapView?.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            locationHelper?.stopLocation()
            locationHelper?.destroy()
            locationHelper = null
            
            mapView?.onDestroy()
            mapView = null
            
            com.fooddelivery.user.websocket.WebSocketManager.getInstance().disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
private fun WaitingForPaymentScreen(
    order: com.fooddelivery.user.model.Order?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text("订单详情", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = brandOrange
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("等待支付", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("请完成支付后查看订单状态", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun WaitingForMerchantScreen(
    order: com.fooddelivery.user.model.Order?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Text("订单详情", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
        
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp),
                    color = brandOrange,
                    strokeWidth = 8.dp
                )
                Icon(
                    Icons.Filled.Store,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = brandOrange
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "等待商家接单",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "商家正在准备接单，请耐心等待...",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            order?.let { o ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F7F5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("订单号", color = Color.Gray)
                            Text(o.orderNo?.takeLast(8) ?: "---", fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("商家", color = Color.Gray)
                            Text(o.merchantName ?: "商家", fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("配送地址", color = Color.Gray)
                            Text(
                                o.receiverAddress ?: "---",
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("订单金额", color = Color.Gray)
                            val displayPrice = o.finalAmount ?: o.totalAmount ?: java.math.BigDecimal.ZERO
                            android.util.Log.d("OrderPrice", "OrderDetail[${o.id}]: finalAmount=${o.finalAmount}, totalAmount=${o.totalAmount}, displayPrice=$displayPrice")
                            Text("¥$displayPrice", fontWeight = FontWeight.Bold, color = brandOrange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCompletedScreen(
    order: com.fooddelivery.user.model.Order?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text("订单已完成", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF22C55E)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("订单已完成", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("感谢您的使用", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun OrderCancelledScreen(
    order: com.fooddelivery.user.model.Order?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text("订单已取消", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("订单已取消", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                order?.cancelReason?.let {
                    Text("原因: $it", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}
