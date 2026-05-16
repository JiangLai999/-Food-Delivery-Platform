package com.fooddelivery.user;

import android.app.Application;

import com.amap.api.maps.MapsInitializer;
import com.fooddelivery.user.utils.SPUtils;

/**
 * Application类
 */
public class FoodDeliveryApplication extends Application {

    private static FoodDeliveryApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 初始化SharedPreferences工具类
        SPUtils.init(this);
        // 初始化网络请求（动态 BASE_URL 从资源读取）
        com.fooddelivery.user.network.ApiClient.init(this);
        // 初始化应用配置
        com.fooddelivery.user.utils.AppConfig.INSTANCE.init(this);
        
        // 设置高德地图隐私合规（必须在使用地图之前调用）
        try {
            MapsInitializer.updatePrivacyShow(this, true, true);
            MapsInitializer.updatePrivacyAgree(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FoodDeliveryApplication getInstance() {
        return instance;
    }
}
