package com.fooddelivery.user.ui.compose.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fooddelivery.user.model.*
import com.fooddelivery.user.network.ApiClient
import com.fooddelivery.user.network.ChatMessageRequest
import com.fooddelivery.user.utils.RecommendationEngine
import com.fooddelivery.user.utils.SPUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getInstance().apiService
    private val TAG = "MainViewModel"

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _hotMerchants = MutableStateFlow<List<Merchant>>(emptyList())
    val hotMerchants: StateFlow<List<Merchant>> = _hotMerchants.asStateFlow()

    private val _recommendedMerchants = MutableStateFlow<List<Merchant>>(emptyList())
    val recommendedMerchants: StateFlow<List<Merchant>> = _recommendedMerchants.asStateFlow()

    private val _recommendedFoods = MutableStateFlow<List<FoodItem>>(emptyList())
    val recommendedFoods: StateFlow<List<FoodItem>> = _recommendedFoods.asStateFlow()

    private val _merchantCategories = MutableStateFlow<List<MerchantCategory>>(emptyList())
    val merchantCategories: StateFlow<List<MerchantCategory>> = _merchantCategories.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Merchant>>(emptyList())
    val searchResults: StateFlow<List<Merchant>> = _searchResults.asStateFlow()

    private val _searchFoodResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val searchFoodResults: StateFlow<List<FoodItem>> = _searchFoodResults.asStateFlow()

    private val _currentMerchant = MutableStateFlow<Merchant?>(null)
    val currentMerchant: StateFlow<Merchant?> = _currentMerchant.asStateFlow()

    private val _merchantFoods = MutableStateFlow<List<FoodItem>>(emptyList())
    val merchantFoods: StateFlow<List<FoodItem>> = _merchantFoods.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    private val _defaultAddress = MutableStateFlow<Address?>(null)
    val defaultAddress: StateFlow<Address?> = _defaultAddress.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    init {
        SPUtils.init(application)
        checkLoginStatus()
    }

    fun checkLoginStatus() {
        val token = SPUtils.getString("token", "")
        val userId = SPUtils.getLong("userId", -1)
        Log.d(TAG, "checkLoginStatus: token='$token', userId=$userId")
        if (token.isNotEmpty() && userId > 0) {
            _isLoggedIn.value = true
            fetchUserInfo()
        } else {
            Log.d(TAG, "checkLoginStatus: not logged in")
        }
    }

    fun refreshLoginStatus() {
        Log.d(TAG, "refreshLoginStatus called")
        checkLoginStatus()
    }

    fun login(phone: String, password: String, loginType: Int = 0, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Login request: phone=$phone, password=$password, loginType=$loginType")
            apiService.login(LoginRequest(phone, password, loginType)).enqueue(object : Callback<Result<Map<String, Any>>> {
                override fun onResponse(call: Call<Result<Map<String, Any>>>, response: Response<Result<Map<String, Any>>>) {
                    _isLoading.value = false
                    Log.d(TAG, "Login response: code=${response.code()}, body=${response.body()}")
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        val data = result.data
                        val token = data["token"] as? String ?: ""
                        val userId = (data["userId"] as? Number)?.toLong() ?: -1
                        Log.d(TAG, "Login success: token=$token, userId=$userId")
                        SPUtils.putString("token", token)
                        SPUtils.putLong("userId", userId)
                        _isLoggedIn.value = true
                        fetchUserInfo()
                        onSuccess()
                    } else {
                        Log.e(TAG, "Login failed: ${result?.message}")
                        onError(result?.message ?: "登录失败")
                    }
                }

                override fun onFailure(call: Call<Result<Map<String, Any>>>, t: Throwable) {
                    _isLoading.value = false
                    Log.e(TAG, "Login error: ${t.message}", t)
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun sendVerifyCode(phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            Log.d(TAG, "Sending verify code to: $phone")
            apiService.sendVerifyCode(phone).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    Log.d(TAG, "Send code response: ${response.code()}, body: ${response.body()}")
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        Log.d(TAG, "Send code success")
                        onSuccess()
                    } else {
                        Log.e(TAG, "Send code failed: ${result?.message}")
                        onError(result?.message ?: "发送失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    Log.e(TAG, "Send code error: ${t.message}", t)
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun register(phone: String, password: String, nickname: String, code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "Register request: phone=$phone, nickname=$nickname, code=$code")
            apiService.register(RegisterRequest(phone, password, code, nickname)).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    _isLoading.value = false
                    Log.d(TAG, "Register response: code=${response.code()}, body=${response.body()}")
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        Log.d(TAG, "Register success")
                        onSuccess()
                    } else {
                        Log.e(TAG, "Register failed: ${result?.message}")
                        onError(result?.message ?: "注册失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    _isLoading.value = false
                    Log.e(TAG, "Register error: ${t.message}", t)
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun resetPassword(phone: String, code: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = com.fooddelivery.user.network.UserResetPasswordRequest(phone, code, newPassword)
            
            Log.d("MainViewModel", "开始重置密码")
            Log.d("MainViewModel", "手机号: $phone")
            Log.d("MainViewModel", "验证码: $code")
            Log.d("MainViewModel", "新密码长度: ${newPassword.length}")
            
            apiService.resetPassword(request).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    _isLoading.value = false
                    val result = response.body()
                    Log.d("MainViewModel", "重置密码API响应: code=${response.code()}, message=${result?.message}")
                    
                    if (result != null && result.isSuccess) {
                        Log.d("MainViewModel", "重置密码成功")
                        onSuccess()
                    } else {
                        val errorMsg = result?.message ?: "重置密码失败"
                        Log.e("MainViewModel", "重置密码失败: $errorMsg")
                        onError(errorMsg)
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    _isLoading.value = false
                    val errorMsg = "网络错误: ${t.message}"
                    Log.e("MainViewModel", "网络异常: $errorMsg")
                    Log.e("MainViewModel", "异常类型: ${t.javaClass.simpleName}")
                    Log.e("MainViewModel", "异常详情: ${t.stackTraceToString()}")
                    
                    onError(errorMsg)
                }
            })
        }
    }

    fun logout() {
        SPUtils.remove("token")
        SPUtils.remove("userId")
        _isLoggedIn.value = false
        _user.value = null
    }

    fun fetchUserInfo() {
        viewModelScope.launch {
            apiService.getUserInfo().enqueue(object : Callback<Result<User>> {
                override fun onResponse(call: Call<Result<User>>, response: Response<Result<User>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _user.value = result.data
                    }
                }

                override fun onFailure(call: Call<Result<User>>, t: Throwable) {
                    _errorMessage.value = "获取用户信息失败: ${t.message}"
                }
            })
        }
    }

    fun fetchHotMerchants(limit: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.getHotMerchants(limit).enqueue(object : Callback<Result<List<Merchant>>> {
                override fun onResponse(call: Call<Result<List<Merchant>>>, response: Response<Result<List<Merchant>>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _hotMerchants.value = result.data
                    }
                }

                override fun onFailure(call: Call<Result<List<Merchant>>>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "获取热门商家失败: ${t.message}"
                }
            })
        }
    }

    fun fetchMerchantCategories() {
        viewModelScope.launch {
            apiService.getAllMerchantCategories().enqueue(object : Callback<Result<List<MerchantCategory>>> {
                override fun onResponse(call: Call<Result<List<MerchantCategory>>>, response: Response<Result<List<MerchantCategory>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _merchantCategories.value = result.data
                    }
                }

                override fun onFailure(call: Call<Result<List<MerchantCategory>>>, t: Throwable) {
                    _errorMessage.value = "获取分类失败: ${t.message}"
                }
            })
        }
    }

    fun fetchRecommendedFoods(limit: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.getFoodsByMerchant(_currentMerchant.value?.id ?: 0).enqueue(object : Callback<Result<List<FoodItem>>> {
                override fun onResponse(call: Call<Result<List<FoodItem>>>, response: Response<Result<List<FoodItem>>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _recommendedFoods.value = RecommendationEngine.getRecommendedFoods(result.data, _currentMerchant.value?.id ?: 0, limit)
                    }
                }

                override fun onFailure(call: Call<Result<List<FoodItem>>>, t: Throwable) {
                    _isLoading.value = false
                    _recommendedFoods.value = emptyList()
                }
            })
        }
    }
    
    fun fetchRecommendedMerchants(limit: Int = 10) {
        val userId = SPUtils.getLong("userId", -1)
        viewModelScope.launch {
            _isLoading.value = true
            apiService.getRecommendedMerchants(if (userId > 0) userId else null, limit).enqueue(object : Callback<Result<List<Merchant>>> {
                override fun onResponse(call: Call<Result<List<Merchant>>>, response: Response<Result<List<Merchant>>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _recommendedMerchants.value = result.data
                    } else {
                        // 如果后端API失败，使用本地推荐
                        apiService.getHotMerchants(50).enqueue(object : Callback<Result<List<Merchant>>> {
                            override fun onResponse(call: Call<Result<List<Merchant>>>, response: Response<Result<List<Merchant>>>) {
                                val hotResult = response.body()
                                if (hotResult != null && hotResult.isSuccess && hotResult.data != null) {
                                    RecommendationEngine.initializeMerchants(hotResult.data)
                                    RecommendationEngine.initializeCategoryPopularity(hotResult.data)
                                    _recommendedMerchants.value = RecommendationEngine.getRecommendedMerchants(hotResult.data, limit)
                                }
                            }
                            override fun onFailure(call: Call<Result<List<Merchant>>>, t: Throwable) {}
                        })
                    }
                }

                override fun onFailure(call: Call<Result<List<Merchant>>>, t: Throwable) {
                    _isLoading.value = false
                    // 降级使用本地推荐
                    apiService.getHotMerchants(50).enqueue(object : Callback<Result<List<Merchant>>> {
                        override fun onResponse(call: Call<Result<List<Merchant>>>, response: Response<Result<List<Merchant>>>) {
                            val hotResult = response.body()
                            if (hotResult != null && hotResult.isSuccess && hotResult.data != null) {
                                RecommendationEngine.initializeMerchants(hotResult.data)
                                RecommendationEngine.initializeCategoryPopularity(hotResult.data)
                                _recommendedMerchants.value = RecommendationEngine.getRecommendedMerchants(hotResult.data, limit)
                            }
                        }
                        override fun onFailure(call: Call<Result<List<Merchant>>>, t: Throwable) {}
                    })
                }
            })
        }
    }

    fun searchMerchants(keyword: String, categoryId: Long? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.searchMerchants(keyword, categoryId, 1, 20).enqueue(object : Callback<Result<PageResult<Merchant>>> {
                override fun onResponse(call: Call<Result<PageResult<Merchant>>>, response: Response<Result<PageResult<Merchant>>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _searchResults.value = result.data.records ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<Result<PageResult<Merchant>>>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "搜索失败: ${t.message}"
                }
            })
        }
    }

    fun searchFoods(keyword: String) {
        viewModelScope.launch {
            apiService.searchFoods(keyword, 1, 20).enqueue(object : Callback<Result<PageResult<FoodItem>>> {
                override fun onResponse(call: Call<Result<PageResult<FoodItem>>>, response: Response<Result<PageResult<FoodItem>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _searchFoodResults.value = result.data.records ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<Result<PageResult<FoodItem>>>, t: Throwable) {
                    _errorMessage.value = "搜索食品失败: ${t.message}"
                }
            })
        }
    }

    fun filterFoods(
        keyword: String? = null,
        merchantId: Long? = null,
        categoryId: Long? = null,
        sort: String? = null
    ) {
        viewModelScope.launch {
            apiService.filterFoods(keyword, merchantId, categoryId, sort, 1, 20).enqueue(object : Callback<Result<PageResult<FoodItem>>> {
                override fun onResponse(call: Call<Result<PageResult<FoodItem>>>, response: Response<Result<PageResult<FoodItem>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _searchFoodResults.value = result.data.records ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<Result<PageResult<FoodItem>>>, t: Throwable) {
                    _errorMessage.value = "筛选食品失败: ${t.message}"
                }
            })
        }
    }

    fun fetchMerchantDetail(merchantId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.getMerchantDetail(merchantId).enqueue(object : Callback<Result<Merchant>> {
                override fun onResponse(call: Call<Result<Merchant>>, response: Response<Result<Merchant>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _currentMerchant.value = result.data
                        fetchMerchantFoods(merchantId)
                    }
                }

                override fun onFailure(call: Call<Result<Merchant>>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "获取商家详情失败: ${t.message}"
                }
            })
        }
    }

    fun fetchMerchantFoods(merchantId: Long) {
        viewModelScope.launch {
            apiService.getFoodsByMerchant(merchantId).enqueue(object : Callback<Result<List<FoodItem>>> {
                override fun onResponse(call: Call<Result<List<FoodItem>>>, response: Response<Result<List<FoodItem>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _merchantFoods.value = result.data
                    }
                }

                override fun onFailure(call: Call<Result<List<FoodItem>>>, t: Throwable) {
                    _errorMessage.value = "获取菜单失败: ${t.message}"
                }
            })
        }
    }

    fun fetchOrders(status: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.getOrderList(status, 1, 20).enqueue(object : Callback<Result<PageResult<Order>>> {
                override fun onResponse(call: Call<Result<PageResult<Order>>>, response: Response<Result<PageResult<Order>>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _orders.value = result.data.records ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<Result<PageResult<Order>>>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "获取订单列表失败: ${t.message}"
                }
            })
        }
    }

    fun fetchOrderDetail(orderId: Long) {
        viewModelScope.launch {
            apiService.getOrderDetail(orderId).enqueue(object : Callback<Result<Order>> {
                override fun onResponse(call: Call<Result<Order>>, response: Response<Result<Order>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _currentOrder.value = result.data
                    }
                }

                override fun onFailure(call: Call<Result<Order>>, t: Throwable) {
                    _errorMessage.value = "获取订单详情失败: ${t.message}"
                }
            })
        }
    }

    fun createOrder(merchantId: Long, addressId: Long, items: List<CreateOrderRequest.OrderItemRequest>, remark: String, couponDiscount: java.math.BigDecimal = java.math.BigDecimal.ZERO, onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = CreateOrderRequest()
            request.merchantId = merchantId
            request.addressId = addressId
            request.items = items
            request.remark = remark
            request.couponDiscount = couponDiscount
            
            apiService.createOrder(request).enqueue(object : Callback<Result<Map<String, Any>>> {
                override fun onResponse(call: Call<Result<Map<String, Any>>>, response: Response<Result<Map<String, Any>>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        val orderId = (result.data["orderId"] as? Number)?.toLong() ?: -1
                        _orders.value.forEach { order ->
                            RecommendationEngine.recordOrder(order)
                        }
                        onSuccess(orderId)
                    } else {
                        onError(result?.message ?: "创建订单失败")
                    }
                }

                override fun onFailure(call: Call<Result<Map<String, Any>>>, t: Throwable) {
                    _isLoading.value = false
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun payOrder(orderId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.payOrder(orderId).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        onSuccess()
                    } else {
                        onError(result?.message ?: "支付失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    _isLoading.value = false
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun cancelOrder(orderId: Long, reason: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d(TAG, "cancelOrder called: orderId=$orderId, reason=$reason")
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(TAG, "cancelOrder: calling API with orderId=$orderId")
            apiService.cancelOrder(orderId, reason).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    _isLoading.value = false
                    Log.d(TAG, "cancelOrder response: code=${response.code()}, body=${response.body()}")
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        Log.d(TAG, "cancelOrder SUCCESS!")
                        onSuccess()
                    } else {
                        Log.e(TAG, "cancelOrder failed: ${result?.message}")
                        onError(result?.message ?: "取消订单失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    _isLoading.value = false
                    Log.e(TAG, "cancelOrder network error: ${t.message}")
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun confirmOrder(orderId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.confirmOrder(orderId).enqueue(object : Callback<Result<Void>> {
                override fun onResponse(call: Call<Result<Void>>, response: Response<Result<Void>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        onSuccess()
                    } else {
                        onError(result?.message ?: "确认收货失败")
                    }
                }

                override fun onFailure(call: Call<Result<Void>>, t: Throwable) {
                    _isLoading.value = false
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun fetchAddresses() {
        viewModelScope.launch {
            apiService.getAddressList().enqueue(object : Callback<Result<List<Address>>> {
                override fun onResponse(call: Call<Result<List<Address>>>, response: Response<Result<List<Address>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _addresses.value = result.data
                        _defaultAddress.value = result.data.find { it.isDefault() }
                    }
                }

                override fun onFailure(call: Call<Result<List<Address>>>, t: Throwable) {
                    _errorMessage.value = "获取地址列表失败: ${t.message}"
                }
            })
        }
    }

    fun addAddress(address: Address, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.addAddress(address).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        onSuccess(result?.message ?: "添加成功")
                        fetchAddresses()
                    } else {
                        onError(result?.message ?: "添加地址失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    _isLoading.value = false
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun deleteAddress(addressId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            apiService.deleteAddress(addressId).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        fetchAddresses()
                        onSuccess()
                    } else {
                        onError(result?.message ?: "删除地址失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun setDefaultAddress(addressId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            apiService.setDefaultAddress(addressId).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        fetchAddresses()
                        onSuccess()
                    } else {
                        onError(result?.message ?: "设置默认地址失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun createReview(request: CreateReviewRequest, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            apiService.createReview(request).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        onSuccess(result?.message ?: "评价成功")
                    } else {
                        onError(result?.message ?: "评价失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    _isLoading.value = false
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    private val _merchantReviews = MutableStateFlow<List<Review>>(emptyList())
    val merchantReviews: StateFlow<List<Review>> = _merchantReviews.asStateFlow()

    fun fetchMerchantReviews(merchantId: Long) {
        viewModelScope.launch {
            apiService.getMerchantReviews(merchantId, 1, 20).enqueue(object : Callback<Result<PageResult<Review>>> {
                override fun onResponse(call: Call<Result<PageResult<Review>>>, response: Response<Result<PageResult<Review>>>) {
                    _isLoading.value = false
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _merchantReviews.value = result.data.records ?: emptyList()
                    }
                }

                override fun onFailure(call: Call<Result<PageResult<Review>>>, t: Throwable) {
                    Log.e(TAG, "获取商家评价失败: ${t.message}")
                }
            })
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
    
    fun fetchCoupons() {
        viewModelScope.launch {
            apiService.getUserCoupons().enqueue(object : Callback<Result<List<Coupon>>> {
                override fun onResponse(call: Call<Result<List<Coupon>>>, response: Response<Result<List<Coupon>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _coupons.value = result.data
                    }
                }
                
                override fun onFailure(call: Call<Result<List<Coupon>>>, t: Throwable) {
                    _errorMessage.value = "获取优惠券失败: ${t.message}"
                }
            })
        }
    }
    
    private val _systemNotices = MutableStateFlow<List<SystemNotice>>(emptyList())
    val systemNotices: StateFlow<List<SystemNotice>> = _systemNotices.asStateFlow()
    
    fun fetchSystemNotices() {
        viewModelScope.launch {
            apiService.getUserNotices().enqueue(object : Callback<Result<List<SystemNotice>>> {
                override fun onResponse(call: Call<Result<List<SystemNotice>>>, response: Response<Result<List<SystemNotice>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _systemNotices.value = result.data
                    }
                }
                
                override fun onFailure(call: Call<Result<List<SystemNotice>>>, t: Throwable) {
                    _errorMessage.value = "获取公告失败: ${t.message}"
                }
            })
        }
    }
    
    private val _riderLocation = MutableStateFlow<RiderLocation?>(null)
    val riderLocation: StateFlow<RiderLocation?> = _riderLocation.asStateFlow()
    
    fun fetchDeliveryLocation(orderId: Long) {
        viewModelScope.launch {
            apiService.getDeliveryLocation(orderId).enqueue(object : Callback<Result<DeliveryLocation>> {
                override fun onResponse(call: Call<Result<DeliveryLocation>>, response: Response<Result<DeliveryLocation>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        val location = result.data
                        _riderLocation.value = RiderLocation().apply {
                            riderId = location.riderId
                            riderName = location.riderName
                            riderPhone = location.riderPhone
                            longitude = location.longitude
                            latitude = location.latitude
                            estimatedTime = location.estimatedTime
                            phase = location.phase
                            distanceToUser = location.distanceToUser
                            description = location.description
                            updateTime = location.updateTime
                        }
                    }
                }
                
                override fun onFailure(call: Call<Result<DeliveryLocation>>, t: Throwable) {
                    _errorMessage.value = "获取配送位置失败: ${t.message}"
                }
            })
        }
    }
    
    private val _deliveryTask = MutableStateFlow<DeliveryTask?>(null)
    val deliveryTask: StateFlow<DeliveryTask?> = _deliveryTask.asStateFlow()
    
    fun fetchDeliveryTask(orderId: Long) {
        viewModelScope.launch {
            apiService.getDeliveryTask(orderId).enqueue(object : Callback<Result<DeliveryTask>> {
                override fun onResponse(call: Call<Result<DeliveryTask>>, response: Response<Result<DeliveryTask>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _deliveryTask.value = result.data
                        // 调试日志
                        val task = result.data
                        Log.e("DATA_CHECK", "=== API返回的DeliveryTask ===")
                        Log.e("DATA_CHECK", "orderId=${task?.orderId}, status=${task?.status}")
                        Log.e("DATA_CHECK", "pickup: ${task?.pickupLatitude}, ${task?.pickupLongitude}")
                        Log.e("DATA_CHECK", "delivery: ${task?.deliveryLatitude}, ${task?.deliveryLongitude}")
                    } else {
                        Log.e("DATA_CHECK", "获取DeliveryTask失败: ${result?.message}")
                    }
                }
                
                override fun onFailure(call: Call<Result<DeliveryTask>>, t: Throwable) {
                    Log.e(TAG, "获取配送任务失败: ${t.message}")
                    Log.e("DATA_CHECK", "获取配送任务网络失败: ${t.message}")
                }
            })
        }
    }
    
    fun fetchRiderLocation(riderId: Long) {
        viewModelScope.launch {
            apiService.getRiderLocation(riderId).enqueue(object : Callback<Result<RiderLocation>> {
                override fun onResponse(call: Call<Result<RiderLocation>>, response: Response<Result<RiderLocation>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _riderLocation.value = result.data
                    }
                }
                
                override fun onFailure(call: Call<Result<RiderLocation>>, t: Throwable) {
                    _errorMessage.value = "获取骑手位置失败: ${t.message}"
                }
            })
        }
    }
    
    /**
     * 完成配送
     * 当骑手到达目的地后，用户端主动调用此接口通知后端订单已完成
     * @param orderId 订单ID
     * @param onSuccess 成功回调
     * @param onError 失败回调
     */
    fun completeDelivery(orderId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            apiService.completeDelivery(orderId).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        Log.d(TAG, "完成配送成功: ${result.message}")
                        onSuccess()
                    } else {
                        Log.e(TAG, "完成配送失败: ${result?.message}")
                        onError(result?.message ?: "完成配送失败")
                    }
                }
                
                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    Log.e(TAG, "完成配送网络错误: ${t.message}")
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    // ========== 商家聊天 ==========
    
    private val _merchantChatMessages = MutableStateFlow<List<com.fooddelivery.user.model.ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<com.fooddelivery.user.model.ChatMessage>> = _merchantChatMessages.asStateFlow()

    fun fetchChatHistory(merchantId: Long) {
        viewModelScope.launch {
            apiService.getChatHistory(merchantId, 2).enqueue(object : Callback<Result<List<com.fooddelivery.user.model.ChatMessage>>> {
                override fun onResponse(call: Call<Result<List<com.fooddelivery.user.model.ChatMessage>>>, response: Response<Result<List<com.fooddelivery.user.model.ChatMessage>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        // 去重：根据消息ID去重
                        val uniqueMessages = result.data.distinctBy { it.id }
                        _merchantChatMessages.value = uniqueMessages
                    }
                }

                override fun onFailure(call: Call<Result<List<com.fooddelivery.user.model.ChatMessage>>>, t: Throwable) {
                    Log.e(TAG, "获取聊天记录失败: ${t.message}")
                }
            })
        }
    }

    fun sendChatMessage(toUserId: Long, content: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val request = com.fooddelivery.user.network.ChatMessageRequest(toUserId, 2, content)
        viewModelScope.launch {
            apiService.sendChatMessage(request).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        // 发送成功后刷新聊天历史
                        fetchChatHistory(toUserId)
                        onSuccess()
                    } else {
                        onError(result?.message ?: "发送失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    // ========== 系统公告 ==========
    
    private val _notices = MutableStateFlow<List<SystemNotice>>(emptyList())
    val notices: StateFlow<List<SystemNotice>> = _notices.asStateFlow()

    fun fetchUserNotices() {
        viewModelScope.launch {
            apiService.getUserNotices().enqueue(object : Callback<Result<List<SystemNotice>>> {
                override fun onResponse(call: Call<Result<List<SystemNotice>>>, response: Response<Result<List<SystemNotice>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _notices.value = result.data
                    }
                }

                override fun onFailure(call: Call<Result<List<SystemNotice>>>, t: Throwable) {
                    Log.e(TAG, "获取公告失败: ${t.message}")
                }
            })
        }
    }

    // ========== 收藏功能 ==========

    private val _favorites = MutableStateFlow<List<FavoriteMerchant>>(emptyList())
    val favorites: StateFlow<List<FavoriteMerchant>> = _favorites.asStateFlow()

    fun fetchFavorites() {
        viewModelScope.launch {
            apiService.getFavoriteList().enqueue(object : Callback<Result<List<FavoriteMerchant>>> {
                override fun onResponse(call: Call<Result<List<FavoriteMerchant>>>, response: Response<Result<List<FavoriteMerchant>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess && result.data != null) {
                        _favorites.value = result.data
                    }
                }

                override fun onFailure(call: Call<Result<List<FavoriteMerchant>>>, t: Throwable) {
                    Log.e(TAG, "获取收藏列表失败: ${t.message}")
                }
            })
        }
    }

    fun addFavorite(merchantId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d(TAG, "addFavorite called: merchantId=$merchantId")
        viewModelScope.launch {
            apiService.addMerchantFavorite(merchantId).enqueue(object : Callback<Result<Void>> {
                override fun onResponse(call: Call<Result<Void>>, response: Response<Result<Void>>) {
                    val result = response.body()
                    Log.d(TAG, "addFavorite response: code=${response.code()}, result=$result")
                    if (result != null && result.isSuccess) {
                        fetchFavorites()
                        onSuccess()
                    } else {
                        onError(result?.message ?: "收藏失败")
                    }
                }

                override fun onFailure(call: Call<Result<Void>>, t: Throwable) {
                    Log.e(TAG, "addFavorite failed: ${t.message}")
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun removeFavorite(merchantId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d(TAG, "removeFavorite called: merchantId=$merchantId")
        viewModelScope.launch {
            apiService.removeMerchantFavorite(merchantId).enqueue(object : Callback<Result<Void>> {
                override fun onResponse(call: Call<Result<Void>>, response: Response<Result<Void>>) {
                    val result = response.body()
                    Log.d(TAG, "removeFavorite response: code=${response.code()}, result=$result")
                    if (result != null && result.isSuccess) {
                        _favorites.value = _favorites.value.filter { it.merchantId != merchantId }
                        onSuccess()
                    } else {
                        onError(result?.message ?: "取消收藏失败")
                    }
                }

                override fun onFailure(call: Call<Result<Void>>, t: Throwable) {
                    Log.e(TAG, "removeFavorite failed: ${t.message}")
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun addFoodFavorite(foodId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            apiService.addFoodFavorite(foodId).enqueue(object : Callback<Result<Void>> {
                override fun onResponse(call: Call<Result<Void>>, response: Response<Result<Void>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        fetchFavorites()
                        onSuccess()
                    } else {
                        onError(result?.message ?: "收藏失败")
                    }
                }

                override fun onFailure(call: Call<Result<Void>>, t: Throwable) {
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun removeFoodFavorite(foodId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            apiService.removeFoodFavorite(foodId).enqueue(object : Callback<Result<Void>> {
                override fun onResponse(call: Call<Result<Void>>, response: Response<Result<Void>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        _favorites.value = _favorites.value.filter { it.merchantId != foodId }
                        onSuccess()
                    } else {
                        onError(result?.message ?: "取消收藏失败")
                    }
                }

                override fun onFailure(call: Call<Result<Void>>, t: Throwable) {
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun checkFavorite(merchantId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            apiService.checkMerchantFavorite(merchantId).enqueue(object : Callback<Result<Boolean>> {
                override fun onResponse(call: Call<Result<Boolean>>, response: Response<Result<Boolean>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        onResult(result.data ?: false)
                    }
                }

                override fun onFailure(call: Call<Result<Boolean>>, t: Throwable) {
                    Log.e(TAG, "检查收藏状态失败: ${t.message}")
                    onResult(false)
                }
            })
        }
    }

    fun checkFoodFavorite(foodId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            apiService.checkFoodFavorite(foodId).enqueue(object : Callback<Result<Boolean>> {
                override fun onResponse(call: Call<Result<Boolean>>, response: Response<Result<Boolean>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        onResult(result.data ?: false)
                    }
                }

                override fun onFailure(call: Call<Result<Boolean>>, t: Throwable) {
                    Log.e(TAG, "检查菜品收藏状态失败: ${t.message}")
                    onResult(false)
                }
            })
        }
    }

    // ========== 客服聊天相关 ==========

    private val _customerChatMessages = MutableStateFlow<List<com.fooddelivery.user.model.ChatMessage>>(emptyList())
    val customerChatMessages: StateFlow<List<com.fooddelivery.user.model.ChatMessage>> = _customerChatMessages.asStateFlow()

    private var adminUserId: Long = 1L // 客服管理员ID

    fun setAdminUserId(adminId: Long) {
        adminUserId = adminId
    }

    fun sendCustomerServiceMessage(content: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val request = com.fooddelivery.user.network.ChatMessageRequest(adminUserId, 3, content)
            apiService.sendChatMessage(request).enqueue(object : Callback<Result<String>> {
                override fun onResponse(call: Call<Result<String>>, response: Response<Result<String>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        Log.d(TAG, "发送客服消息成功")
                        fetchCustomerServiceChatHistory()
                        onSuccess()
                    } else {
                        Log.e(TAG, "发送客服消息失败: ${result?.message}")
                        onError(result?.message ?: "发送失败")
                    }
                }

                override fun onFailure(call: Call<Result<String>>, t: Throwable) {
                    Log.e(TAG, "发送客服消息网络错误: ${t.message}")
                    onError("网络错误: ${t.message}")
                }
            })
        }
    }

    fun fetchCustomerServiceChatHistory() {
        viewModelScope.launch {
            apiService.getChatHistory(adminUserId, 3).enqueue(object : Callback<Result<List<com.fooddelivery.user.model.ChatMessage>>> {
                override fun onResponse(call: Call<Result<List<com.fooddelivery.user.model.ChatMessage>>>, response: Response<Result<List<com.fooddelivery.user.model.ChatMessage>>>) {
                    val result = response.body()
                    if (result != null && result.isSuccess) {
                        _customerChatMessages.value = result.data ?: emptyList()
                        Log.d(TAG, "获取客服聊天历史成功: ${result.data?.size} 条消息")
                    } else {
                        Log.e(TAG, "获取客服聊天历史失败: ${result?.message}")
                    }
                }

                override fun onFailure(call: Call<Result<List<com.fooddelivery.user.model.ChatMessage>>>, t: Throwable) {
                    Log.e(TAG, "获取客服聊天历史网络错误: ${t.message}")
                }
            })
        }
    }
}
