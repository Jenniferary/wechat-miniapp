package com.example.demo.controller;

import com.example.demo.dto.RestaurantNearbyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurant")
public class RestaurantController {

    @Autowired
    private JdbcTemplate jdbc;

    @GetMapping("/nearby")
    public Map<String, Object> getNearby(@RequestParam double lat, @RequestParam double lng) {
        String sql = """
            SELECT id, name, latitude, longitude, phone,
              ROUND(
                6371 * 2 * ASIN(SQRT(
                  POW(SIN((? - latitude) * PI() / 180 / 2), 2) +
                  COS(? * PI() / 180) * COS(latitude * PI() / 180) *
                  POW(SIN((? - longitude) * PI() / 180 / 2), 2)
                )), 2
              ) AS distance_km
            FROM restaurant_branches
            ORDER BY distance_km ASC
            LIMIT 10
        """;

        List<RestaurantNearbyDTO> list = jdbc.query(sql, (rs, rowNum) -> mapToDto(rs), lat, lat, lng);

        return Map.of("status", "success", "data", list);
    }

    private RestaurantNearbyDTO mapToDto(ResultSet rs) throws SQLException {
        return new RestaurantNearbyDTO(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getDouble("distance_km"),
                rs.getString("phone")  // 新增电话字段映射
        );
    }
}
