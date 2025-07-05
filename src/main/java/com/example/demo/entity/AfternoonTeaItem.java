package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "afternoon_tea")
public class AfternoonTeaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name")
    private String itemName;

    // 构造函数
    public AfternoonTeaItem() {}

    public AfternoonTeaItem(String itemName) {
        this.itemName = itemName;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
