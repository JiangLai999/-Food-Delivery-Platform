package com.fooddelivery.user.ui.compose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.model.CreateOrderRequest
import com.fooddelivery.user.ui.compose.manager.CartManager
import com.fooddelivery.user.ui.compose.screens.CheckoutScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel
import java.math.BigDecimal

class ComposeCheckoutActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private var selectedAddressId by mutableStateOf<Long?>(null)
    
    private val addressSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedAddressId = null
            viewModel.fetchAddresses()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val cartItems by CartManager.cartItems.collectAsStateWithLifecycle()
                val merchantName by CartManager.currentMerchantName.collectAsStateWithLifecycle()
                val deliveryFee by CartManager.deliveryFee.collectAsStateWithLifecycle()
                val addresses by viewModel.addresses.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
                
                val defaultAddress = remember(addresses, selectedAddressId) {
                    if (selectedAddressId != null) {
                        addresses.find { it.id == selectedAddressId }
                    } else {
                        addresses.find { it.isDefault() }
                    }
                }
                val coupons by viewModel.coupons.collectAsStateWithLifecycle()
                var selectedCoupon by remember { mutableStateOf<com.fooddelivery.user.model.Coupon?>(null) }
                
                LaunchedEffect(Unit) {
                    viewModel.fetchAddresses()
                    viewModel.fetchCoupons()
                }
                
                CheckoutScreen(
                    cartItems = cartItems,
                    merchantId = CartManager.currentMerchantId.value ?: 0L,
                    merchantName = merchantName ?: "",
                    address = defaultAddress,
                    deliveryFee = deliveryFee,
                    packFee = BigDecimal("2"),
                    availableCoupons = coupons.filter { it.isAvailable() },
                    selectedCoupon = selectedCoupon,
                    isLoading = isLoading,
                    onBack = { finish() },
                    onSelectAddress = {
                        val intent = Intent(this@ComposeCheckoutActivity, ComposeAddressListActivity::class.java)
                        addressSelectionLauncher.launch(intent)
                    },
                    onSelectCoupon = { coupon ->
                        selectedCoupon = coupon
                    },
                    onPay = { remark ->
                        val address = defaultAddress
                        if (address != null && address.id != null) {
                            val                             merchantId = CartManager.currentMerchantId.value
                            val items = cartItems.map { item ->
                                CreateOrderRequest.OrderItemRequest(item.foodId, item.quantity)
                            }
                            
                            // 计算订单总金额
                            val foodTotal = cartItems.sumOf { (it.price * it.quantity.toBigDecimal()).toDouble() }.toBigDecimal()
                            val packFee = BigDecimal("2")
                            val totalAmount = foodTotal + deliveryFee + packFee
                            
                            // 减去优惠券折扣
                            val couponDiscount = selectedCoupon?.amount ?: BigDecimal.ZERO
                            val finalAmount = totalAmount - couponDiscount
                            
                             viewModel.createOrder(
                                 merchantId = merchantId ?: 0L,
                                 addressId = address.id,
                                 items = items,
                                 remark = remark,
                                 couponDiscount = couponDiscount,
                                 onSuccess = { orderId ->
                                    val orderTotal = finalAmount.toDouble()
                                    viewModel.payOrder(
                                        orderId = orderId,
                                        onSuccess = {
                                            CartManager.clearCart()
                                            val intent = Intent(this@ComposeCheckoutActivity, ComposePaymentSuccessActivity::class.java)
                                            intent.putExtra("orderId", orderId.toString())
                                            intent.putExtra("amount", orderTotal)
                                            startActivity(intent)
                                            finish()
                                        },
                                        onError = { error ->
                                            Toast.makeText(this@ComposeCheckoutActivity, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                onError = { error ->
                                    Toast.makeText(this@ComposeCheckoutActivity, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(this@ComposeCheckoutActivity, "请选择收货地址", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}
