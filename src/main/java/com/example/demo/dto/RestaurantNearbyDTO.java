package com.example.demo.dto;

public class RestaurantNearbyDTO {
    private Integer id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private String phone;  // 新增电话字段

    // 无参构造器
    public RestaurantNearbyDTO() {
    }

    // 全参构造器
    public RestaurantNearbyDTO(Integer id, String name, Double latitude, Double longitude, Double distanceKm, String phone) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
        this.phone = phone;
    }

    // Getter 和 Setter

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
