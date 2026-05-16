package com.fooddelivery.user.ui.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fooddelivery.user.model.Address
import com.fooddelivery.user.ui.compose.screens.AddAddressScreen
import com.fooddelivery.user.ui.compose.screens.AddressListScreen
import com.fooddelivery.user.ui.compose.theme.FoodDeliveryTheme
import com.fooddelivery.user.ui.compose.viewmodel.MainViewModel

class ComposeAddressListActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private var isAddingAddress by mutableStateOf(false)
    private var isSavingAddress by mutableStateOf(false)
    private var editingAddressId by mutableStateOf<Long?>(null)
    private var locatedAddress by mutableStateOf<String?>(null)
    private var locatedProvince by mutableStateOf("北京市")
    private var locatedCity by mutableStateOf("北京市")
    private var locatedDistrict by mutableStateOf("朝阳区")
    private var locatedLongitude by mutableStateOf<Double?>(null)
    private var locatedLatitude by mutableStateOf<Double?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FoodDeliveryTheme {
                val addresses by viewModel.addresses.collectAsStateWithLifecycle()
                
                LaunchedEffect(Unit) {
                    viewModel.fetchAddresses()
                }
                
                if (isAddingAddress || editingAddressId != null) {
                    AddAddressScreen(
                        isEditing = editingAddressId != null,
                        onSave = { name, phone, address, houseNumber, gender, tag ->
                            if (isSavingAddress) return@AddAddressScreen
                            isSavingAddress = true
                            
                            val newAddress = Address()
                            newAddress.receiverName = "$name ($gender)"
                            newAddress.receiverPhone = phone
                            newAddress.province = locatedProvince
                            newAddress.city = locatedCity
                            newAddress.district = locatedDistrict
                            newAddress.detailAddress = address + " " + houseNumber
                            newAddress.tag = tag
                            newAddress.setDefault(false)
                            
                            val lon = locatedLongitude
                            val lat = locatedLatitude
                            if (lon != null && lat != null) {
                                newAddress.longitude = java.math.BigDecimal(lon)
                                newAddress.latitude = java.math.BigDecimal(lat)
                            }
                            
                            if (editingAddressId != null) {
                                newAddress.id = editingAddressId
                            }
                            
                            viewModel.addAddress(
                                address = newAddress,
                                onSuccess = { message ->
                                    isSavingAddress = false
                                    isAddingAddress = false
                                    editingAddressId = null
                                    locatedAddress = null
                                    android.widget.Toast.makeText(this@ComposeAddressListActivity, message, android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    isSavingAddress = false
                                    android.widget.Toast.makeText(this@ComposeAddressListActivity, error, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onBack = {
                            isAddingAddress = false
                            editingAddressId = null
                            locatedAddress = null
                        },
                        onLocationClick = {},
                        initialAddress = locatedAddress,
                        isLoading = isSavingAddress
                    )
                } else {
                    AddressListScreen(
                        addresses = addresses,
                        onBack = { finish() },
                        onEditAddress = { addressId ->
                            editingAddressId = addressId
                        },
                        onDeleteAddress = { addressId ->
                            viewModel.deleteAddress(
                                addressId = addressId,
                                onSuccess = {},
                                onError = {}
                            )
                        },
                        onAddAddress = {
                            isAddingAddress = true
                        },
                        onSelectAddress = { addressId ->
                            viewModel.setDefaultAddress(
                                addressId = addressId,
                                onSuccess = {
                                    setResult(RESULT_OK)
                                    finish()
                                },
                                onError = { }
                            )
                        }
                    )
                }
            }
        }
    }
}
