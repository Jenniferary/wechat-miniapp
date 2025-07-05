package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "branch_managers")
public class BranchManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // 店长ID

    private Integer branchId;  // 关联门店ID

    private String name;       // 店长姓名
    private String phone;      // 店长联系电话
    private String username;   // 店长登录账号

    @Column(name = "password_hash")  // 映射数据库中的 password_hash 列
    private String password;   // 店长登录密码（这里依然可以使用明文密码，如果不使用加密）

    private String email;      // 店长邮箱

    @Override
    public String toString() {
        return "BranchManager{" +
                "id=" + id +
                ", branchId=" + branchId +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
