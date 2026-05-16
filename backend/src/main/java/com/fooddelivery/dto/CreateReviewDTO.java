package com.fooddelivery.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建评价DTO
 */
@Data
public class CreateReviewDTO implements Serializable {

    private Long orderId;

    private Integer rating;

    private Integer tasteRating;

    private Integer portionRating;

    private String content;

    private List<String> images;

    private Integer imageCount;

    private Boolean isAnonymous = false;
}
