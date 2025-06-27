package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dishes")
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Long id;

    @Column(name = "dish_name")
    private String dishName;

    @Column(name = "dish_price")
    private Double dishPrice;

    @Column(name = "dish_stock")
    private Integer dishStock;
}
