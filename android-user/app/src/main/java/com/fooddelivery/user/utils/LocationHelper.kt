package com.fooddelivery.user.utils

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener

class LocationHelper(private val context: Context) {

    private var locationClient: AMapLocationClient? = null
    private var locationListener: LocationListener? = null

    interface LocationListener {
        fun onLocationSuccess(location: AMapLocation)
        fun onLocationError(errorCode: Int, errorMsg: String)
    }

    fun setLocationListener(listener: LocationListener) {
        this.locationListener = listener
    }

    fun startLocation() {
        try {
            AMapLocationClient.updatePrivacyShow(context, true, true)
            AMapLocationClient.updatePrivacyAgree(context, true)
            locationClient = AMapLocationClient(context)

            val option = AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isNeedAddress = true
                isOnceLocation = false
                isLocationCacheEnable = false
                interval = 3000
            }

            locationClient?.apply {
                setLocationOption(option)
                setLocationListener { location ->
                    if (location != null) {
                        if (location.errorCode == 0) {
                            locationListener?.onLocationSuccess(location)
                        } else {
                            locationListener?.onLocationError(
                                location.errorCode,
                                "定位失败: ${location.errorInfo}"
                            )
                        }
                    }
                }
                startLocation()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Start location error", e)
            locationListener?.onLocationError(-1, e.message ?: "未知错误")
        }
    }

    fun stopLocation() {
        locationClient?.stopLocation()
    }

    fun destroy() {
        try {
            locationClient?.stopLocation()
            locationClient?.onDestroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying location client", e)
        }
        locationClient = null
        locationListener = null
    }

    companion object {
        private const val TAG = "LocationHelper"

        fun getLocationOnce(context: Context, callback: (AMapLocation?) -> Unit) {
            try {
                AMapLocationClient.updatePrivacyShow(context, true, true)
                AMapLocationClient.updatePrivacyAgree(context, true)
                val locationClient = AMapLocationClient(context)
                val option = AMapLocationClientOption().apply {
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    isOnceLocation = true
                    isNeedAddress = true
                }

                locationClient.setLocationListener { location ->
                    callback(location)
                    locationClient.stopLocation()
                    locationClient.onDestroy()
                }

                locationClient.setLocationOption(option)
                locationClient.startLocation()
            } catch (e: Exception) {
                Log.e(TAG, "Get location once error", e)
                callback(null)
            }
        }
    }
}
