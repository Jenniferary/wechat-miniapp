package com.example.demo.entity;

import java.time.LocalDate;  // 如果用Date，import java.util.Date;

public class User {
    private Long userId;
    private String username;
    private String password;
    private String memberLevel;
    private String captcha;
    private double memberBalance;
    private double memberPoints;
    private String memberPhone;
    private LocalDate memberBirthday;  // 如果用Date改成 Date 类型即可

    // getter 和 setter
    public String getCaptcha() {
        return captcha;
    }
    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMemberLevel() { return memberLevel; }
    public void setMemberLevel(String memberLevel) { this.memberLevel = memberLevel; }

    public double getMemberBalance() { return memberBalance; }
    public void setMemberBalance(double memberBalance) { this.memberBalance = memberBalance; }

    public double getMemberPoints() { return memberPoints; }
    public void setMemberPoints(double memberPoints) { this.memberPoints = memberPoints; }

    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String memberPhone) { this.memberPhone = memberPhone; }

    public LocalDate getMemberBirthday() { return memberBirthday; }
    public void setMemberBirthday(LocalDate memberBirthday) { this.memberBirthday = memberBirthday; }
}
