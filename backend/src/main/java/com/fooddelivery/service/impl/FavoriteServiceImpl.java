package com.fooddelivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fooddelivery.entity.FoodItem;
import com.fooddelivery.entity.Merchant;
import com.fooddelivery.entity.UserFavorite;
import com.fooddelivery.mapper.FoodItemMapper;
import com.fooddelivery.mapper.MerchantMapper;
import com.fooddelivery.mapper.UserFavoriteMapper;
import com.fooddelivery.service.FavoriteService;
import com.fooddelivery.vo.FavoriteMerchantVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Override
    public List<FavoriteMerchantVO> getUserFavorites(Long userId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        List<UserFavorite> favorites = userFavoriteMapper.selectList(wrapper);

        List<FavoriteMerchantVO> result = new ArrayList<>();
        for (UserFavorite favorite : favorites) {
            if (favorite.getMerchantId() != null) {
                Merchant merchant = merchantMapper.selectById(favorite.getMerchantId());
                if (merchant != null) {
                    FavoriteMerchantVO vo = new FavoriteMerchantVO();
                    vo.setId(favorite.getId());
                    vo.setMerchantId(merchant.getId());
                    vo.setMerchantName(merchant.getMerchantName());
                    vo.setLogo(merchant.getLogo());
                    vo.setRating(merchant.getAvgRating());
                    vo.setSalesVolume(merchant.getSalesVolume());
                    vo.setDeliveryFee(merchant.getDeliveryFee());
                    vo.setMinAmount(merchant.getMinOrderAmount());
                    vo.setAverageDeliveryTime(30);
                    vo.setFavoriteTime(favorite.getCreateTime());
                    vo.setFavoriteType("merchant");
                    result.add(vo);
                }
            } else if (favorite.getFoodId() != null) {
                FoodItem food = foodItemMapper.selectById(favorite.getFoodId());
                if (food != null) {
                    FavoriteMerchantVO vo = new FavoriteMerchantVO();
                    vo.setId(favorite.getId());
                    vo.setFoodId(food.getId());
                    vo.setFoodName(food.getFoodName());
                    vo.setFoodImage(food.getImage());
                    vo.setFoodPrice(food.getPrice());
                    vo.setFavoriteTime(favorite.getCreateTime());
                    vo.setFavoriteType("food");
                    
                    // Also get merchant info for the food
                    if (food.getMerchantId() != null) {
                        Merchant merchant = merchantMapper.selectById(food.getMerchantId());
                        if (merchant != null) {
                            vo.setMerchantId(merchant.getId());
                            vo.setMerchantName(merchant.getMerchantName());
                        }
                    }
                    
                    result.add(vo);
                }
            }
        }
        return result;
    }

    @Override
    public boolean addMerchantFavorite(Long userId, Long merchantId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getMerchantId, merchantId);
        UserFavorite existing = userFavoriteMapper.selectOne(wrapper);
        
        if (existing != null) {
            return true;
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setMerchantId(merchantId);
        favorite.setCreateTime(LocalDateTime.now());
        return userFavoriteMapper.insert(favorite) > 0;
    }

    @Override
    public boolean removeMerchantFavorite(Long userId, Long merchantId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getMerchantId, merchantId);
        return userFavoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean isMerchantFavorite(Long userId, Long merchantId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getMerchantId, merchantId);
        return userFavoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean addFoodFavorite(Long userId, Long foodId) {
        // Check if this specific food is already favorited
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getFoodId, foodId);
        UserFavorite existing = userFavoriteMapper.selectOne(wrapper);
        
        if (existing != null) {
            return true;
        }

        // Get food info to verify food exists
        FoodItem food = foodItemMapper.selectById(foodId);
        if (food == null) {
            return false;
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setFoodId(foodId);
        // Don't set merchantId for food-only favorites - this distinguishes them from merchant favorites
        favorite.setCreateTime(LocalDateTime.now());
        return userFavoriteMapper.insert(favorite) > 0;
    }

    @Override
    public boolean removeFoodFavorite(Long userId, Long foodId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getFoodId, foodId);
        return userFavoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean isFoodFavorite(Long userId, Long foodId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getFoodId, foodId);
        return userFavoriteMapper.selectCount(wrapper) > 0;
    }
}
