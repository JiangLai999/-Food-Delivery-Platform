package com.fooddelivery.user.websocket;

import android.util.Log;

import com.fooddelivery.user.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * WebSocket管理器
 */
public class WebSocketManager {

    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;
    private WebSocket webSocket;
    private OkHttpClient client;
    private Gson gson;
    private WebSocketCallback callback;

    private WebSocketManager() {
        client = new OkHttpClient();
        gson = new Gson();
    }

    public static WebSocketManager getInstance() {
        if (instance == null) {
            synchronized (WebSocketManager.class) {
                if (instance == null) {
                    instance = new WebSocketManager();
                }
            }
        }
        return instance;
    }

    private Long currentUserId;
    private Long currentOrderId;

    /**
     * 连接WebSocket
     */
    public void connect(Long userId, WebSocketCallback callback) {
        this.callback = callback;
        this.currentUserId = userId;

        String url = ApiClient.getWebSocketUrl(userId);
        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket连接成功");
                
                // 如果已设置订单ID，自动订阅
                if (currentOrderId != null) {
                    subscribeOrder(currentOrderId);
                }
                
                if (callback != null) {
                    callback.onConnected();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "收到消息: " + text);
                handleMessage(text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket连接失败", t);
                if (callback != null) {
                    callback.onError(t);
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket连接关闭: " + reason);
                if (callback != null) {
                    callback.onClosed();
                }
            }
        });
    }

    /**
     * 处理消息
     */
    private void handleMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            if (callback != null) {
                switch (type) {
                    case "RIDER_LOCATION":
                        // 骑手位置更新
                        callback.onRiderLocationUpdate(json.getAsJsonObject("data"));
                        break;
                    case "ORDER_STATUS":
                        // 订单状态更新
                        callback.onOrderStatusUpdate(json.get("message").getAsString());
                        break;
                    case "SYSTEM_MESSAGE":
                        // 系统消息
                        callback.onSystemMessage(json.get("message").getAsString());
                        break;
                    case "CHAT_MESSAGE":
                        // 聊天消息
                        JsonObject data = json.has("data") ? json.getAsJsonObject("data") : json;
                        callback.onChatMessage(data);
                        break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理消息失败", e);
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    /**
     * 订阅订单
     */
    public void subscribeOrder(Long orderId) {
        this.currentOrderId = orderId;
        
        if (webSocket != null) {
            JsonObject message = new JsonObject();
            message.addProperty("type", "SUBSCRIBE_ORDER");
            message.addProperty("orderId", orderId);
            webSocket.send(message.toString());
            Log.d(TAG, "订阅订单: " + orderId);
        }
    }

    /**
     * 取消订阅订单
     */
    public void unsubscribeOrder() {
        if (webSocket != null && currentOrderId != null) {
            JsonObject message = new JsonObject();
            message.addProperty("type", "UNSUBSCRIBE_ORDER");
            webSocket.send(message.toString());
            Log.d(TAG, "取消订阅订单: " + currentOrderId);
            currentOrderId = null;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        unsubscribeOrder();
        if (webSocket != null) {
            webSocket.close(1000, "客户端主动断开");
            webSocket = null;
        }
        currentUserId = null;
        currentOrderId = null;
    }

    /**
     * WebSocket回调接口
     */
    public interface WebSocketCallback {
        void onConnected();
        void onRiderLocationUpdate(JsonObject data);
        void onOrderStatusUpdate(String message);
        void onSystemMessage(String message);
        void onChatMessage(JsonObject data);
        void onError(Throwable error);
        void onClosed();
    }
}
