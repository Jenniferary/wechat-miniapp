package com.example.demo.controller;

import com.example.demo.dto.DishPackageDTO;
import com.example.demo.entity.Dish;
import com.example.demo.entity.DishPackageEntity;
import com.example.demo.repository.DishPackageRepository;
import com.example.demo.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/api/packages")
@CrossOrigin(origins = "http://localhost:8081")
public class DishPackageController {

    @Autowired
    private DishPackageRepository dishPackageRepository;

    @Autowired
    private DishRepository dishRepository;

    // ✅ 获取所有套餐信息
    @GetMapping
    public List<DishPackageDTO> getAllPackages() {
        List<DishPackageEntity> packages = dishPackageRepository.findAll();
        List<DishPackageDTO> result = new ArrayList<>();

        for (DishPackageEntity pack : packages) {
            DishPackageDTO dto = new DishPackageDTO();
            dto.setPackageId(pack.getPackageId());
            dto.setPackageName(pack.getPackageName());
            dto.setPackagePrice(pack.getPackagePrice());

            String[] names = pack.getDishList().split("\\s*,\\s*");
            List<Dish> dishList = dishRepository.findByDishNameIn(Arrays.asList(names));
            dto.setDishList(dishList);

            result.add(dto);
        }

        return result;
    }

    // ✅ 上传套餐

    @PostMapping("/upload")
    public String uploadPackage(@RequestBody Map<String, Object> payload) {
        try {
            // 获取请求数据
            String packageName = (String) payload.get("packageName");
            double discount = Double.parseDouble(payload.get("discount").toString());
            List<String> selectedItems = (List<String>) payload.get("selectedItems");

            // 数据有效性检查
            if (packageName == null || packageName.isEmpty()) {
                return "套餐名称不能为空";
            }

            if (discount <= 0 || discount > 1) {
                return "折扣值必须在 0 到 1 之间";
            }

            if (selectedItems == null || selectedItems.isEmpty()) {
                return "请选择至少一个菜品";
            }

            // 获取所有菜品的价格并计算总价
            List<Dish> dishes = dishRepository.findByDishNameIn(selectedItems);
            if (dishes.isEmpty()) {
                return "未找到所选菜品";
            }
            double totalPrice = dishes.stream().mapToDouble(Dish::getDishPrice).sum();

            // 计算折扣后的价格并确保保留两位小数
            BigDecimal discountedPrice = new BigDecimal(totalPrice * discount).setScale(2, RoundingMode.HALF_UP);

            // 创建并保存套餐
            DishPackageEntity newPackage = new DishPackageEntity();
            newPackage.setPackageName(packageName);
            newPackage.setPackagePrice(discountedPrice.doubleValue());  // 转换为 double 存储
            newPackage.setDishList(String.join(",", selectedItems));

            dishPackageRepository.save(newPackage);
            return "套餐上传成功";
        } catch (Exception e) {
            e.printStackTrace();  // 打印异常堆栈
            return "服务器内部错误：" + e.getMessage();  // 返回详细错误信息
        }
    }




    // ✅ 删除套餐（根据ID）
    @DeleteMapping("/{id}")
    public String deletePackage(@PathVariable Long id) {
        Optional<DishPackageEntity> optional = dishPackageRepository.findById(id);
        if (optional.isPresent()) {
            dishPackageRepository.deleteById(id);
            return "套餐删除成功";
        } else {
            return "未找到套餐，删除失败";
        }
    }
}
