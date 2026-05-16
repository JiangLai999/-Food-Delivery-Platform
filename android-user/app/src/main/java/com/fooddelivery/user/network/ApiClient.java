package com.fooddelivery.user.network;

import android.text.TextUtils;
import android.content.Context;

import com.fooddelivery.user.utils.SPUtils;
import com.fooddelivery.user.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络请求客户端
 */
public class ApiClient {

    // 服务器地址，初始值为模拟器默认，后续可通过 init(context) 切换
    private static String BASE_URL = "http://10.0.2.2:8080/api/";

    private static ApiClient instance;
    private Retrofit retrofit;
    private ApiService apiService;

    private ApiClient() {
        // 日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Token拦截器
        Interceptor tokenInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                String token = SPUtils.getString("token", "");

                if (!TextUtils.isEmpty(token)) {
                    Request request = original.newBuilder()
                            .header("Authorization", token)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                }

                return chain.proceed(original);
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient();
                }
            }
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }

    // 通过上下文初始化 BASE_URL，方便在不同环境下切换
    public static void init(Context context) {
        try {
            BASE_URL = context.getString(R.string.api_base_url);
        } catch (Exception ignore) {
            // keep default if resources not ready
        }
    }

    public static String getWebSocketUrl(Long userId) {
        return com.fooddelivery.user.utils.AppConfig.INSTANCE.getWebSocketUrl(userId);
    }
}
