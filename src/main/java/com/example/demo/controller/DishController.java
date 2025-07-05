package com.example.demo.controller;

import com.example.demo.entity.Dish;
import com.example.demo.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(originPatterns = "http://localhost:*") // Vue 前端地址
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    // 获取所有菜品
    @GetMapping
    public List<Dish> listDishes() {
        return dishRepository.findAll();
    }
    //上传菜品
    @PostMapping("/upload")
    public String uploadDish(
            @RequestParam String dishName,
            @RequestParam Double dishPrice,
            @RequestParam Integer dishStock,           // 新增库存参数
            @RequestParam("image") MultipartFile image) {

        // 图片保存逻辑不变
        String originalFileName = image.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = dishName + extension;
        String filePath = "C:/Users/张静娴/WeChatProjects/miniprogram-1/images" + fileName;

        try {
            image.transferTo(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return "图片上传失败！";
        }

        // 创建并保存菜品
        Dish newDish = new Dish();
        newDish.setDishName(dishName);
        newDish.setDishPrice(dishPrice);
        newDish.setDishStock(dishStock);          // 设置库存
        dishRepository.save(newDish);

        return "上传成功";
    }

    // ✅ 修改菜品库存
    @PutMapping("/{id}/stock")
    public String updateDishStock(@PathVariable Long id, @RequestParam Integer stock) {
        Dish dish = dishRepository.findById(id).orElse(null);
        if (dish != null) {
            dish.setDishStock(stock);
            dishRepository.save(dish);
            return "库存更新成功";
        } else {
            return "菜品不存在";
        }
    }
    // 删除菜品（根据 ID）
    @DeleteMapping("/{id}")
    public String deleteDishById(@PathVariable Long id) {
        if (dishRepository.existsById(id)) {
            dishRepository.deleteById(id);
            return "删除成功";
        } else {
            return "菜品不存在";
        }
    }
}
