package com.example.demo.repository;

import com.example.demo.entity.DishPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// 接口继承 JpaRepository，自动获得基本的 CRUD 功能
public interface DishPackageRepository extends JpaRepository<DishPackageEntity, Long> {

    // 声明删除方法，通过套餐名称删除
    void deleteByPackageName(String packageName);
}
