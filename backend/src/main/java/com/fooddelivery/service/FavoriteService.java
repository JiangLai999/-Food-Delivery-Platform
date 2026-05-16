package com.fooddelivery.service;

import com.fooddelivery.entity.Merchant;
import com.fooddelivery.vo.FavoriteMerchantVO;

import java.util.List;

public interface FavoriteService {

    List<FavoriteMerchantVO> getUserFavorites(Long userId);

    boolean addMerchantFavorite(Long userId, Long merchantId);

    boolean removeMerchantFavorite(Long userId, Long merchantId);

    boolean isMerchantFavorite(Long userId, Long merchantId);

    boolean addFoodFavorite(Long userId, Long foodId);

    boolean removeFoodFavorite(Long userId, Long foodId);

    boolean isFoodFavorite(Long userId, Long foodId);
}
