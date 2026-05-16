package com.fooddelivery.user.utils

import com.fooddelivery.user.model.FoodItem
import com.fooddelivery.user.model.Merchant
import com.fooddelivery.user.model.Order
import java.math.BigDecimal

object RecommendationEngine {
    private val userOrderHistory = mutableListOf<Order>()
    private val merchantCategories = mutableMapOf<Long, String>()
    private val merchantRatings = mutableMapOf<Long, Float>()
    private val categoryPopularity = mutableMapOf<String, Int>()

    fun recordOrder(order: Order) {
        userOrderHistory.add(order)
        order.merchantId?.let { merchantId ->
            merchantCategories[merchantId] = order.merchant?.categoryName ?: ""
        }
    }

    fun initializeMerchants(merchants: List<Merchant>) {
        merchants.forEach { merchant ->
            merchantRatings[merchant.id] = merchant.rating?.toFloat() ?: 0f
        }
    }

    fun initializeCategoryPopularity(merchants: List<Merchant>) {
        merchants.forEach { merchant ->
            val category = merchant.categoryName ?: "其他"
            categoryPopularity[category] = (categoryPopularity[category] ?: 0) + 1
        }
    }

    fun getRecommendedMerchants(
        allMerchants: List<Merchant>,
        limit: Int = 10
    ): List<Merchant> {
        if (allMerchants.isEmpty()) return emptyList()

        val userPreferredCategories = getUserPreferredCategories()
        
        return allMerchants
            .filter { it.status == 1 }
            .map { merchant ->
                val score = calculateMerchantScore(merchant, userPreferredCategories)
                Pair(merchant, score)
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    fun getRecommendedFoods(
        allFoods: List<FoodItem>,
        merchantId: Long,
        limit: Int = 10
    ): List<FoodItem> {
        if (allFoods.isEmpty()) return emptyList()

        val userPreferredCategories = getUserPreferredFoodCategories()

        return allFoods
            .map { food ->
                val score = calculateFoodScore(food, userPreferredCategories)
                Pair(food, score)
            }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    private fun getUserPreferredCategories(): Map<String, Float> {
        if (userOrderHistory.isEmpty()) {
            return categoryPopularity.mapValues { it.value.toFloat() }
        }

        val categoryCount = mutableMapOf<String, Int>()
        userOrderHistory.forEach { order ->
            order.merchant?.categoryName?.let { category ->
                categoryCount[category] = (categoryCount[category] ?: 0) + 1
            }
        }

        val total = userOrderHistory.size.toFloat()
        return categoryCount.mapValues { it.value / total }
    }

    private fun getUserPreferredFoodCategories(): Map<String, Float> {
        val categoryCount = mutableMapOf<String, Int>()
        userOrderHistory.forEach { order ->
            order.items?.forEach { item ->
                item.foodName?.let { foodName ->
                    categoryCount[foodName] = (categoryCount[foodName] ?: 0) + 1
                }
            }
        }

        if (categoryCount.isEmpty()) {
            return emptyMap()
        }

        val total = categoryCount.values.sum().toFloat()
        return categoryCount.mapValues { it.value / total }
    }

    private fun calculateMerchantScore(
        merchant: Merchant,
        preferredCategories: Map<String, Float>
    ): Float {
        var score = 0f

        val categoryMatch = preferredCategories[merchant.categoryName] ?: 0f
        score += categoryMatch * 30f

        val rating = merchant.rating?.toFloat() ?: 0f
        score += (rating / 5f) * 20f

        val salesVolume = merchant.salesVolume ?: 0
        score += (salesVolume / 1000f).coerceAtMost(10f) * 2f

        return score
    }

    private fun calculateFoodScore(
        food: FoodItem,
        preferredCategories: Map<String, Float>
    ): Float {
        var score = 0f

        val categoryMatch = preferredCategories[food.categoryName] ?: 0f
        score += categoryMatch * 40f

        val sales = food.salesVolume ?: 0
        score += (sales / 100f).coerceAtMost(15f) * 2f

        val originalPrice = food.originalPrice
        val price = food.price
        if (originalPrice != null && price != null && originalPrice > price) {
            score += 15f
        }

        return score
    }

    fun getPopularInCategory(
        merchants: List<Merchant>,
        category: String,
        limit: Int = 5
    ): List<Merchant> {
        return merchants
            .filter { it.categoryName == category && it.status == 1 }
            .sortedByDescending { it.salesVolume ?: 0 }
            .take(limit)
    }

    fun getSimilarMerchants(
        merchant: Merchant,
        allMerchants: List<Merchant>,
        limit: Int = 5
    ): List<Merchant> {
        return allMerchants
            .filter { it.id != merchant.id && it.categoryName == merchant.categoryName }
            .sortedByDescending { it.rating?.toFloat() ?: 0f }
            .take(limit)
    }

    fun clearHistory() {
        userOrderHistory.clear()
    }
}
