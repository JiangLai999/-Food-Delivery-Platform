package com.fooddelivery.vo;

import com.fooddelivery.entity.Merchant;
import com.fooddelivery.entity.OrderItem;
import lombok.Data;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单VO
 */
@Data
public class OrderVO implements Serializable {

    private Long id;

    private String orderNo;

    private Long userId;

    private String userPhone;

    private Long merchantId;

    private String merchantName;

    private String merchantLogo;

    private Long riderId;

    private String riderName;

    private BigDecimal totalAmount;

    private BigDecimal deliveryFee;

    private BigDecimal packFee;

    private BigDecimal couponDiscount;

    private BigDecimal finalAmount;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private BigDecimal receiverLongitude;

    private BigDecimal receiverLatitude;

    private String remark;

    private Integer status;

    private String statusText;

    // 商家信息
    private BigDecimal merchantLongitude;
    
    private BigDecimal merchantLatitude;

    private Integer payStatus;

    private LocalDateTime payTime;

    private LocalDateTime acceptTime;

    private LocalDateTime deliveryTime;

    private LocalDateTime completeTime;

    private LocalDateTime createTime;

    private List<OrderItem> items;

    private Boolean hasReviewed;

    // 完整商家对象
    private Merchant merchant;

    /**
     * 获取格式化的地址显示，避免重复的省市前缀
     * 如果地址中已经有"北京市"，则不重复添加
     */
    public String getDisplayAddress() {
        if (receiverAddress == null) return "";
        String addr = receiverAddress.trim();
        if (addr.startsWith("北京市") && addr.contains("北京市")) {
            // 去重：地址有"北京市北京市"的情况
            return addr.replaceFirst("北京市", "").trim();
        }
        return addr;
    }
}
