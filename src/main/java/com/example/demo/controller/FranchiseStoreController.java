package com.example.demo.controller;

import com.example.demo.entity.FranchiseStore;
import com.example.demo.repository.FranchiseStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/franchise/stores")
@CrossOrigin(originPatterns = "http://localhost:*")
public class FranchiseStoreController {
    
    @Autowired
    private FranchiseStoreRepository franchiseStoreRepository;
    
    // 创建加盟店铺
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createStore(@RequestBody FranchiseStore store) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查申请ID是否已存在店铺
            Optional<FranchiseStore> existingStore = franchiseStoreRepository.findByApplicationId(store.getApplicationId());
            if (existingStore.isPresent()) {
                response.put("success", false);
                response.put("message", "该申请已创建店铺");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 设置默认状态和时间
            store.setStatus(FranchiseStore.StoreStatus.筹备中);
            store.setCreatedAt(LocalDateTime.now());
            store.setUpdatedAt(LocalDateTime.now());
            
            FranchiseStore savedStore = franchiseStoreRepository.save(store);
            
            response.put("success", true);
            response.put("message", "店铺创建成功");
            response.put("data", savedStore);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "店铺创建失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取所有店铺
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllStores() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FranchiseStore> stores = franchiseStoreRepository.findAllByOrderByCreatedAtDesc();
            response.put("success", true);
            response.put("data", stores);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取店铺列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 根据状态获取店铺
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getStoresByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            FranchiseStore.StoreStatus storeStatus = FranchiseStore.StoreStatus.valueOf(status);
            List<FranchiseStore> stores = franchiseStoreRepository.findByStatusOrderByCreatedAtDesc(storeStatus);
            response.put("success", true);
            response.put("data", stores);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "无效的状态参数");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取店铺列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 根据ID获取店铺详情
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStoreById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<FranchiseStore> store = franchiseStoreRepository.findById(id);
            if (store.isPresent()) {
                response.put("success", true);
                response.put("data", store.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "店铺不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取店铺详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 更新店铺信息
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStore(
            @PathVariable Integer id,
            @RequestBody FranchiseStore storeData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<FranchiseStore> optionalStore = franchiseStoreRepository.findById(id);
            if (!optionalStore.isPresent()) {
                response.put("success", false);
                response.put("message", "店铺不存在");
                return ResponseEntity.notFound().build();
            }
            
            FranchiseStore store = optionalStore.get();
            
            // 更新店铺信息
            if (storeData.getStoreName() != null) store.setStoreName(storeData.getStoreName());
            if (storeData.getStoreAddress() != null) store.setStoreAddress(storeData.getStoreAddress());
            if (storeData.getLatitude() != null) store.setLatitude(storeData.getLatitude());
            if (storeData.getLongitude() != null) store.setLongitude(storeData.getLongitude());
            if (storeData.getStoreArea() != null) store.setStoreArea(storeData.getStoreArea());
            if (storeData.getFranchiseeName() != null) store.setFranchiseeName(storeData.getFranchiseeName());
            if (storeData.getFranchiseePhone() != null) store.setFranchiseePhone(storeData.getFranchiseePhone());
            if (storeData.getFranchiseeEmail() != null) store.setFranchiseeEmail(storeData.getFranchiseeEmail());
            if (storeData.getInvestmentAmount() != null) store.setInvestmentAmount(storeData.getInvestmentAmount());
            if (storeData.getFranchiseFee() != null) store.setFranchiseFee(storeData.getFranchiseFee());
            if (storeData.getMonthlyRoyalty() != null) store.setMonthlyRoyalty(storeData.getMonthlyRoyalty());
            if (storeData.getContractStartDate() != null) store.setContractStartDate(storeData.getContractStartDate());
            if (storeData.getContractEndDate() != null) store.setContractEndDate(storeData.getContractEndDate());
            if (storeData.getOpeningDate() != null) store.setOpeningDate(storeData.getOpeningDate());
            if (storeData.getStatus() != null) store.setStatus(storeData.getStatus());
            if (storeData.getBusinessLicense() != null) store.setBusinessLicense(storeData.getBusinessLicense());
            if (storeData.getFoodPermit() != null) store.setFoodPermit(storeData.getFoodPermit());
            if (storeData.getManagerName() != null) store.setManagerName(storeData.getManagerName());
            if (storeData.getManagerPhone() != null) store.setManagerPhone(storeData.getManagerPhone());
            if (storeData.getEmployeeCount() != null) store.setEmployeeCount(storeData.getEmployeeCount());
            if (storeData.getSeatingCapacity() != null) store.setSeatingCapacity(storeData.getSeatingCapacity());
            if (storeData.getOperatingHours() != null) store.setOperatingHours(storeData.getOperatingHours());
            if (storeData.getNotes() != null) store.setNotes(storeData.getNotes());
            
            store.setUpdatedAt(LocalDateTime.now());
            
            FranchiseStore updatedStore = franchiseStoreRepository.save(store);
            
            response.put("success", true);
            response.put("message", "店铺信息更新成功");
            response.put("data", updatedStore);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 根据加盟商手机号查询店铺
    @GetMapping("/franchisee/phone/{phone}")
    public ResponseEntity<Map<String, Object>> getStoresByFranchiseePhone(@PathVariable String phone) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FranchiseStore> stores = franchiseStoreRepository.findByFranchiseePhone(phone);
            response.put("success", true);
            response.put("data", stores);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 搜索店铺（根据店铺名称、地址或加盟商姓名）
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStores(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FranchiseStore> storesByName = franchiseStoreRepository.findByStoreNameContaining(keyword);
            List<FranchiseStore> storesByAddress = franchiseStoreRepository.findByStoreAddressContaining(keyword);
            List<FranchiseStore> storesByFranchisee = franchiseStoreRepository.findByFranchiseeNameContaining(keyword);
            
            // 合并结果并去重
            storesByName.addAll(storesByAddress);
            storesByName.addAll(storesByFranchisee);
            List<FranchiseStore> uniqueStores = storesByName.stream().distinct().toList();
            
            response.put("success", true);
            response.put("data", uniqueStores);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取附近的店铺
    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> getNearbyStores(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "0.1") Double radius) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 计算经纬度范围（简单的矩形范围）
            Double minLat = latitude - radius;
            Double maxLat = latitude + radius;
            Double minLng = longitude - radius;
            Double maxLng = longitude + radius;
            
            List<FranchiseStore> stores = franchiseStoreRepository.findByLocationRange(minLat, maxLat, minLng, maxLng);
            
            response.put("success", true);
            response.put("data", stores);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询附近店铺失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取店铺统计信息
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStoreStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Long> statistics = new HashMap<>();
            statistics.put("筹备中", franchiseStoreRepository.countByStatus(FranchiseStore.StoreStatus.筹备中));
            statistics.put("装修中", franchiseStoreRepository.countByStatus(FranchiseStore.StoreStatus.装修中));
            statistics.put("试营业", franchiseStoreRepository.countByStatus(FranchiseStore.StoreStatus.试营业));
            statistics.put("正式营业", franchiseStoreRepository.countByStatus(FranchiseStore.StoreStatus.正式营业));
            statistics.put("暂停营业", franchiseStoreRepository.countByStatus(FranchiseStore.StoreStatus.暂停营业));
            statistics.put("已关闭", franchiseStoreRepository.countByStatus(FranchiseStore.StoreStatus.已关闭));
            statistics.put("总计", franchiseStoreRepository.count());
            
            response.put("success", true);
            response.put("data", statistics);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 获取正在营业的店铺
    @GetMapping("/operating")
    public ResponseEntity<Map<String, Object>> getOperatingStores() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FranchiseStore> stores = franchiseStoreRepository.findByStatus(FranchiseStore.StoreStatus.正式营业);
            response.put("success", true);
            response.put("data", stores);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取营业店铺失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 删除店铺
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStore(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (franchiseStoreRepository.existsById(id)) {
                franchiseStoreRepository.deleteById(id);
                response.put("success", true);
                response.put("message", "店铺删除成功");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "店铺不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}