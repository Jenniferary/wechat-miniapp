package com.example.demo.dto;

import com.example.demo.entity.Dish;
import lombok.Data;

import java.util.List;

@Data
public class DishPackageDTO {
    private Long packageId;
    private String packageName;
    private Double packagePrice;
    private List<Dish> dishList;
}
