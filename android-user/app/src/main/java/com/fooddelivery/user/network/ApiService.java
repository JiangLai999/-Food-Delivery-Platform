package com.fooddelivery.user.network;

import com.fooddelivery.user.model.*;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * API接口定义
 */
public interface ApiService {

    // ========== 用户接口 ==========

    @POST("user/send-code")
    Call<Result<String>> sendVerifyCode(@Query("phone") String phone);

    @POST("user/register")
    Call<Result<String>> register(@Body RegisterRequest request);

    @POST("user/login")
    Call<Result<Map<String, Object>>> login(@Body LoginRequest request);

    @POST("user/reset-password")
    Call<Result<String>> resetPassword(@Body com.fooddelivery.user.network.UserResetPasswordRequest request);

    @GET("user/info")
    Call<Result<User>> getUserInfo();

    @PUT("user/update")
    Call<Result<Void>> updateUser(@Body User user);

    // ========== 地址接口 ==========

    @GET("user/address/list")
    Call<Result<List<Address>>> getAddressList();

    @POST("user/address/add")
    Call<Result<String>> addAddress(@Body Address address);

    @PUT("user/address/update")
    Call<Result<String>> updateAddress(@Body Address address);

    @DELETE("user/address/delete/{addressId}")
    Call<Result<String>> deleteAddress(@Path("addressId") Long addressId);

    @PUT("user/address/default/{addressId}")
    Call<Result<String>> setDefaultAddress(@Path("addressId") Long addressId);

    // ========== 商家接口 ==========

    @GET("merchant/search")
    Call<Result<PageResult<Merchant>>> searchMerchants(
            @Query("keyword") String keyword,
            @Query("categoryId") Long categoryId,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize
    );

    @GET("merchant/hot")
    Call<Result<List<Merchant>>> getHotMerchants(@Query("limit") int limit);

    @GET("merchant/{merchantId}")
    Call<Result<Merchant>> getMerchantDetail(@Path("merchantId") Long merchantId);

    // ========== 餐品接口 ==========

    @GET("food/merchant/{merchantId}")
    Call<Result<List<FoodItem>>> getFoodsByMerchant(@Path("merchantId") Long merchantId);

    @GET("food/search")
    Call<Result<PageResult<FoodItem>>> searchFoods(
            @Query("keyword") String keyword,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize
    );

    @GET("food/filter")
    Call<Result<PageResult<FoodItem>>> filterFoods(
            @Query("keyword") String keyword,
            @Query("merchantId") Long merchantId,
            @Query("categoryId") Long categoryId,
            @Query("sort") String sort,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize
    );

    @GET("food/hot")
    Call<Result<List<FoodItem>>> getHotFoods(@Query("limit") int limit);

    @GET("food/{foodId}")
    Call<Result<FoodItem>> getFoodDetail(@Path("foodId") Long foodId);

    // ========== 商家分类接口 ==========

    @GET("merchant/categories/all")
    Call<Result<List<MerchantCategory>>> getAllMerchantCategories();

    // ========== 推荐接口 ==========

    @GET("recommendation/merchants")
    Call<Result<List<Merchant>>> getRecommendedMerchants(
            @Query("userId") Long userId,
            @Query("limit") int limit);

    @GET("recommendation/foods")
    Call<Result<List<FoodItem>>> getRecommendedFoods(
            @Query("userId") Long userId,
            @Query("limit") int limit);

    // ========== 订单接口 ==========

    @POST("order/create")
    Call<Result<Map<String, Object>>> createOrder(@Body CreateOrderRequest request);

    @POST("order/pay/{orderId}")
    Call<Result<String>> payOrder(@Path("orderId") Long orderId);

    @GET("order/list")
    Call<Result<PageResult<Order>>> getOrderList(
            @Query("status") Integer status,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize
    );

    @GET("order/{orderId}")
    Call<Result<Order>> getOrderDetail(@Path("orderId") Long orderId);

    @POST("order/cancel/{orderId}")
    Call<Result<String>> cancelOrder(
            @Path("orderId") Long orderId,
            @Query("reason") String reason
    );

    @POST("order/confirm/{orderId}")
    Call<Result<Void>> confirmOrder(@Path("orderId") Long orderId);

    // ========== 配送接口 ==========

    @GET("delivery/task/{orderId}")
    Call<Result<DeliveryTask>> getDeliveryTask(@Path("orderId") Long orderId);

    @POST("delivery/task/create")
    Call<Result<DeliveryTask>> createDeliveryTask(@Body DeliveryCreateDTO dto);

    @POST("delivery/{orderId}/start")
    Call<Result<Void>> startDelivery(@Path("orderId") Long orderId);

    @GET("delivery/task/{orderId}/location")
    Call<Result<DeliveryLocation>> getDeliveryLocation(@Path("orderId") Long orderId);

    /**
     * 完成配送接口
     * 骑手到达目的地后，客户端调用此接口通知后端订单已完成
     * @param orderId 订单ID
     * @return 执行结果
     */
    @POST("delivery/task/{orderId}/complete")
    Call<Result<String>> completeDelivery(@Path("orderId") Long orderId);

    @POST("delivery/{orderId}/location")
    Call<Result<Void>> updateDeliveryLocation(@Path("orderId") Long orderId, @Body DeliveryLocation location);

    @GET("rider/location/{riderId}")
    Call<Result<RiderLocation>> getRiderLocation(@Path("riderId") Long riderId);

    // ========== 评价接口 ==========

    @POST("review/create")
    Call<Result<String>> createReview(@Body CreateReviewRequest request);

    @GET("review/merchant/{merchantId}")
    Call<Result<PageResult<Review>>> getMerchantReviews(
            @Path("merchantId") Long merchantId,
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize
    );

    @GET("review/user")
    Call<Result<List<Review>>> getUserReviews();

    @GET("review/order/{orderId}")
    Call<Result<Review>> getReviewByOrderId(@Path("orderId") Long orderId);

    // ========== 系统公告 ==========

    @GET("notice/user")
    Call<Result<List<SystemNotice>>> getUserNotices();

    // ========== 优惠券接口 ==========

    @GET("coupon/user")
    Call<Result<List<Coupon>>> getUserCoupons();

    // ========== 收藏接口 ==========

    @GET("favorite/list")
    Call<Result<List<FavoriteMerchant>>> getFavoriteList();

    @POST("favorite/addMerchant")
    Call<Result<Void>> addMerchantFavorite(@Query("merchantId") Long merchantId);

    @POST("favorite/removeMerchant")
    Call<Result<Void>> removeMerchantFavorite(@Query("merchantId") Long merchantId);

    @GET("favorite/checkMerchant")
    Call<Result<Boolean>> checkMerchantFavorite(@Query("merchantId") Long merchantId);

    @POST("favorite/addFood")
    Call<Result<Void>> addFoodFavorite(@Query("foodId") Long foodId);

    @POST("favorite/removeFood")
    Call<Result<Void>> removeFoodFavorite(@Query("foodId") Long foodId);

    @GET("favorite/checkFood")
    Call<Result<Boolean>> checkFoodFavorite(@Query("foodId") Long foodId);

    // ========== 聊天接口 ==========

    @POST("chat/send")
    Call<Result<String>> sendChatMessage(@Body ChatMessageRequest request);

    @GET("chat/history/{withUserId}")
    Call<Result<List<ChatMessage>>> getChatHistory(
            @Path("withUserId") Long withUserId,
            @Query("withUserType") Integer withUserType
    );
}
