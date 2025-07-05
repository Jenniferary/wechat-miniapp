package com.example.demo.controller;

import com.example.demo.entity.Admin;
import com.example.demo.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class UserController {

    private final String url = "jdbc:mysql://localhost:3306/order_dish?useSSL=false&serverTimezone=UTC";
    private final String dbUser = "root";
    private final String dbPass = "123456";
    // 添加到 UserController.java 中
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        // 禁止缓存
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        // 使用验证码工具生成验证码
        int width = 120, height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 随机内容
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ123456789";
        StringBuilder captcha = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 4; i++) {
            captcha.append(chars.charAt(r.nextInt(chars.length())));
        }

        // 保存验证码到 session
        session.setAttribute("captcha", captcha.toString().toLowerCase());

        // 绘图
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(captcha.toString(), 20, 28);

        ImageIO.write(image, "JPEG", response.getOutputStream());
    }
    @PostMapping("/manager/login")
    public Map<String, String> managerLogin(@RequestBody Admin admin) {
        Map<String, String> result = new HashMap<>();
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String sql = "SELECT * FROM admin WHERE username = ? AND password = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, admin.getUsername());
            pst.setString(2, admin.getPassword());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                result.put("status", "success");
                result.put("message", "管理员登录成功");
            } else {
                result.put("status", "error");
                result.put("message", "账号或密码错误");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "管理员登录失败：" + e.getMessage());
        }
        return result;
    }
    @GetMapping("/managers")
    public Map<String, Object> getAllManagers() {
        Map<String, Object> result = new HashMap<>();
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String sql = "SELECT id, username FROM admin";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            // 手动构造编号键：data0, data1, ...
            int index = 0;
            while (rs.next()) {
                Map<String, Object> manager = new HashMap<>();
                manager.put("id", rs.getInt("id"));
                manager.put("username", rs.getString("username"));
                result.put("data" + index, manager);
                index++;
            }

            result.put("status", "success");
            result.put("count", index); // 可选：记录数量

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "获取管理员失败：" + e.getMessage());
        }

        return result;
    }
    @PostMapping("/addmanager")
    public Map<String, String> addManager(@RequestBody Admin admin) {
        Map<String, String> result = new HashMap<>();
        try {
            Connection con = DriverManager.getConnection(url, dbUser, dbPass);
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                // 检查用户名是否已存在
                String checkSql = "SELECT * FROM admin WHERE username = ?";
                PreparedStatement checkPst = con.prepareStatement(checkSql);
                checkPst.setString(1, admin.getUsername());
                ResultSet rs = checkPst.executeQuery();

                if (rs.next()) {
                    result.put("success", "false");
                    result.put("message", "该管理员账号已存在");
                } else {
                    // 插入新管理员
                    String insertSql = "INSERT INTO admin (username, password) VALUES (?, ?)";
                    PreparedStatement pst = con.prepareStatement(insertSql);
                    pst.setString(1, admin.getUsername());
                    pst.setString(2, admin.getPassword()); // 若需加密，这里应处理
                    int affected = pst.executeUpdate();
                    if (affected > 0) {
                        result.put("success", "true");
                        result.put("message", "管理员添加成功");
                    } else {
                        result.put("success", "false");
                        result.put("message", "管理员添加失败");
                    }
                }
            } finally {
                if (con != null) con.close();
            }
        } catch (Exception e) {
            result.put("success", "false");
            result.put("message", "添加失败：" + e.getMessage());
        }
        return result;
    }
    // 注册用户
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        System.out.println("registering .............................");
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, user.getUsername());
            pst.setString(2, user.getPassword());
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("success");
                return "注册成功";
            } else {
                System.out.println("false");
                return "注册失败，未插入数据";
            }
        } catch (Exception e) {
            return "注册失败：" + e.getMessage();
        }
    }

    // 用户登录
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody User user, HttpSession session) {
        Map<String, String> result = new HashMap<>();

        // 从 session 中获取之前生成的验证码
        String sessionCaptcha = (String) session.getAttribute("captcha");
        String requestCaptcha = user.getCaptcha(); // 你需要让 User 实体添加 captcha 字段

        if (sessionCaptcha == null || requestCaptcha == null ||
                !sessionCaptcha.equalsIgnoreCase(requestCaptcha.trim())) {
            result.put("status", "error");
            result.put("message", "验证码错误");
            return result;
        }

        // 验证用户名密码
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, user.getUsername());
            pst.setString(2, user.getPassword());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                result.put("status", "success");
                result.put("message", "登录成功");
            } else {
                result.put("status", "error");
                result.put("message", "用户名或密码错误");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "登录失败：" + e.getMessage());
        }
        return result;
    }



    @GetMapping("/user/{username}")
    public Map<String, Object> getUserInfo(@PathVariable String username) {
        Map<String, Object> result = new HashMap<>();
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getLong("user_id"));
                user.setUsername(rs.getString("username"));
                user.setMemberLevel(rs.getString("member_level"));
                user.setMemberBalance(rs.getDouble("member_balance"));
                user.setMemberPoints(rs.getDouble("member_points"));
                user.setMemberPhone(rs.getString("member_phone"));

                // 处理生日，数据库是Date类型，转成LocalDate
                java.sql.Date birthdayDate = rs.getDate("member_birthday");
                if (birthdayDate != null) {
                    user.setMemberBirthday(birthdayDate.toLocalDate());
                } else {
                    user.setMemberBirthday(null);
                }

                result.put("status", "success");
                result.put("user", user);
            } else {
                result.put("status", "error");
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "获取用户信息失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/user/update")
    public Map<String, Object> updateUserInfo(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String sql = "UPDATE users SET member_phone = ?, member_birthday = ? WHERE username = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, user.getMemberPhone());

            // 如果生日是LocalDate，转换成java.sql.Date
            if (user.getMemberBirthday() != null) {
                pst.setDate(2, java.sql.Date.valueOf(user.getMemberBirthday()));
            } else {
                pst.setNull(2, Types.DATE);
            }
            pst.setString(3, user.getUsername());

            int rows = pst.executeUpdate();
            if (rows > 0) {
                result.put("status", "success");
                result.put("message", "用户信息更新成功");
            } else {
                result.put("status", "error");
                result.put("message", "更新失败，用户不存在");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "更新用户信息失败：" + e.getMessage());
        }
        return result;
    }

    // 充值接口
    @PostMapping("/recharge")
    public Map<String, String> recharge(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");

        // 显式地将 amount 转换为 Double 类型
        double amount = 0;
        if (request.get("amount") instanceof Integer) {
            amount = ((Integer) request.get("amount")).doubleValue();  // 如果是 Integer，转换为 Double
        } else if (request.get("amount") instanceof Double) {
            amount = (Double) request.get("amount");  // 如果是 Double，直接获取
        }

        Map<String, String> result = new HashMap<>();

        if (amount <= 0) {
            result.put("status", "error");
            result.put("message", "充值金额无效");
            return result;
        }

        try (Connection con = DriverManager.getConnection(url, dbUser, dbPass)) {
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 获取当前用户信息
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                double currentBalance = rs.getDouble("member_balance");
                double currentPoints = rs.getDouble("member_points");

                // 更新用户余额和积分
                double newBalance = currentBalance + amount;
                double newPoints = currentPoints + amount;

                // 根据余额决定会员等级
                String newMemberLevel = "VIP0";
                if (newBalance >= 2000) {
                    newMemberLevel = "VIP3";
                } else if (newBalance >= 1000) {
                    newMemberLevel = "VIP2";
                } else if (newBalance >= 500) {
                    newMemberLevel = "VIP1";
                }

                // 更新用户余额、积分和会员等级
                String updateSql = "UPDATE users SET member_balance = ?, member_points = ?, member_level = ? WHERE username = ?";
                PreparedStatement updatePst = con.prepareStatement(updateSql);
                updatePst.setDouble(1, newBalance);
                updatePst.setDouble(2, newPoints);
                updatePst.setString(3, newMemberLevel);
                updatePst.setString(4, username);
                int rowsAffected = updatePst.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("status", "success");
                    result.put("message", "充值成功，当前会员等级: " + newMemberLevel);
                } else {
                    result.put("status", "error");
                    result.put("message", "充值失败");
                }
            } else {
                result.put("status", "error");
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "充值失败：" + e.getMessage());
        }

        return result;
    }

}
