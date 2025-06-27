package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dishpackages")
public class DishPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "package_price")
    private Double packagePrice;

    @Column(name = "dish_list") // 用英文逗号分隔的菜品名
    private String dishList;
}
