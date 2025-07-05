-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        5.7.x - MySQL Community Server - GPL
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  12.6.0.6765
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 导出 order_dish 的数据库结构
CREATE DATABASE IF NOT EXISTS `order_dish` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `order_dish`;

-- 导出  表 order_dish.admin 结构
CREATE TABLE IF NOT EXISTS `admin` (
                                       `id` int(11) NOT NULL AUTO_INCREMENT,
                                       `username` varchar(50) NOT NULL,
                                       `password` varchar(50) NOT NULL,
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  order_dish.admin 的数据：~10 rows (大约)
INSERT INTO `admin` (`id`, `username`, `password`, `create_time`) VALUES
(1, 'admin', '123456', '2025-05-21 19:04:20'),
(2, 'Zhang', '111111', '2025-05-21 19:10:29'),
(3, 'counter', '123456', '2025-05-21 20:00:35'),
(4, 'QQ', '2345', '2025-05-21 20:00:55'),
(5, 'QWQQW', '90900N0N6', '2025-06-05 19:53:05'),
(6, 'zjx', '123456', '2025-06-05 20:09:18'),
(7, 'sss', '123456', '2025-06-11 21:56:27'),
(8, 'YEWEI', '123456', '2025-06-12 14:42:50'),
(9, 'guanliyuan', '123456', '2025-06-12 15:03:44'),
(10, 'ruan', '123456', '2025-06-12 15:34:08');

-- 导出  表 order_dish.afternoon_tea 结构
CREATE TABLE IF NOT EXISTS `afternoon_tea` (
                                               `id` int(11) NOT NULL AUTO_INCREMENT,
                                               `item_name` varchar(100) NOT NULL,
                                               PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 正在导出表  order_dish.afternoon_tea 的数据：~19 rows (大约)
INSERT INTO `afternoon_tea` (`id`, `item_name`) VALUES
(1, 'Macaron'),
(2, 'Eclair'),
(3, 'Creme Brulee'),
(4, 'Tarte Tatin'),
(5, 'Profiterole'),
(6, 'Croissant'),
(7, 'Madeleine'),
(8, 'Opera Cake'),
(9, 'Palmier'),
(10, 'Champagne'),
(11, 'Chardonnay'),
(12, 'Merlot'),
(13, 'Sauvignon Blanc'),
(14, 'Pinot Noir'),
(15, 'Rose'),
(16, 'Earl Grey Supreme'),
(17, 'Mocha Frappe'),
(18, 'Hibiscus Delight'),
(19, 'Caramel Macchiato');

-- 导出  表 order_dish.applicants 结构
CREATE TABLE IF NOT EXISTS `applicants` (
                                            `id` int(11) NOT NULL AUTO_INCREMENT,
                                            `username` varchar(50) NOT NULL COMMENT '申请人登录账号',
                                            `password_hash` varchar(255) NOT NULL COMMENT '密码哈希，切勿存明文',
                                            `email` varchar(100) NOT NULL COMMENT '邮箱，用于找回密码等',
                                            `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
                                            `full_name` varchar(100) DEFAULT NULL COMMENT '申请人真实姓名',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `username` (`username`),
                                            UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='申请人账号表';

-- 正在导出表  order_dish.applicants 的数据：~4 rows (大约)
INSERT INTO `applicants` (`id`, `username`, `password_hash`, `email`, `phone`, `full_name`) VALUES
(1, '小王', '$2a$10$mSgWy3gz.s/KAoghIY4XlOOhzN0qPfURoUkc0g582/ghKy5L/CvC2', '2022301010412@cau.edu.cn', '', ''),
(2, '小李', '$2a$10$tm9zuQhy/DNsyX8GvDi.OuFA3AyvGEsMReTyXBI05Vbmge9AZfZ6y', 'A13956261846@hotmail.com', '18956426845', ''),
(3, 'qiusijia', '$2a$10$Dmu97w16UourGug3bGMHJ.npq6b6axhEoZZhHBMAXILNEu4n9Y.EG', 'A13956248945@hotmail.com', '13956248945', '邱思佳'),
(4, 'yexin', '$2a$10$hv/OfjxFq6oIAXhAtuM1GOjbGpp6A8kEr2bAkMudmfx5Z6jk4GxKC', '1584185168@qq.com', '15984652954', '叶昕');

-- 导出  表 order_dish.attendance_records 结构
CREATE TABLE IF NOT EXISTS `attendance_records` (
                                                    `id` int(11) NOT NULL AUTO_INCREMENT,
                                                    `employee_id` int(11) NOT NULL,
                                                    `employee_type` varchar(20) NOT NULL,
                                                    `branch_id` int(11) NOT NULL,
                                                    `check_in_time` datetime DEFAULT NULL,
                                                    `check_in_lat` decimal(10,7) DEFAULT NULL,
                                                    `check_in_lng` decimal(10,7) DEFAULT NULL,
                                                    `check_in_status` varchar(20) DEFAULT NULL,
                                                    `check_out_time` datetime DEFAULT NULL,
                                                    `check_out_lat` decimal(10,7) DEFAULT NULL,
                                                    `check_out_lng` decimal(10,7) DEFAULT NULL,
                                                    `check_out_status` varchar(20) DEFAULT NULL,
                                                    `status` varchar(20) DEFAULT NULL,
                                                    `check_in_date` date DEFAULT NULL,
                                                    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
                                                    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                    PRIMARY KEY (`id`),
                                                    UNIQUE KEY `uk_employee_day` (`employee_id`,`employee_type`,`check_in_date`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 注意：MySQL 5.x不支持生成列，check_in_date字段需要通过触发器或应用程序逻辑来维护
-- 可以使用以下触发器来自动更新check_in_date字段：
/*
DELIMITER //
CREATE TRIGGER tr_attendance_records_insert
    BEFORE INSERT ON attendance_records
    FOR EACH ROW
BEGIN
    IF NEW.check_in_time IS NOT NULL THEN
        SET NEW.check_in_date = DATE(NEW.check_in_time);
    END IF;
END//

CREATE TRIGGER tr_attendance_records_update
    BEFORE UPDATE ON attendance_records
    FOR EACH ROW
BEGIN
    IF NEW.check_in_time IS NOT NULL THEN
        SET NEW.check_in_date = DATE(NEW.check_in_time);
    END IF;
END//
DELIMITER ;
*/

-- 正在导出表  order_dish.attendance_records 的数据：~79 rows (大约)
INSERT INTO `attendance_records` (`id`, `employee_id`, `employee_type`, `branch_id`, `check_in_time`, `check_in_lat`, `check_in_lng`, `check_in_status`, `check_out_time`, `check_out_lat`, `check_out_lng`, `check_out_status`, `status`, `check_in_date`, `created_at`, `updated_at`) VALUES
(1, 1, 'chef', 1, '2025-07-03 17:47:14', 39.9120330, 116.3730380, 'normal', '2025-07-03 17:47:49', 39.9120330, 116.3730380, 'normal', 'normal', '2025-07-03', '2025-07-03 17:47:14', '2025-07-04 23:14:29'),
(2, 1, 'chef', 1, '2025-06-02 07:05:16', 39.9120330, 116.3730380, 'normal', '2025-06-02 21:14:54', 39.9120330, 116.3730380, 'normal', 'normal', '2025-06-02', '2025-07-04 23:11:31', '2025-07-04 23:14:29');

-- 导出  表 order_dish.branch_managers 结构
CREATE TABLE IF NOT EXISTS `branch_managers` (
                                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                                 `branch_id` int(11) NOT NULL COMMENT '关联门店ID，每个门店唯一对应一个店长',
                                                 `name` varchar(100) NOT NULL COMMENT '店长姓名',
                                                 `phone` varchar(20) NOT NULL COMMENT '店长联系电话',
                                                 `username` varchar(50) NOT NULL COMMENT '店长登录账号，唯一',
                                                 `password_hash` varchar(255) NOT NULL COMMENT '店长登录密码的哈希值，安全存储',
                                                 `email` varchar(100) DEFAULT NULL COMMENT '店长邮箱，选填',
                                                 PRIMARY KEY (`id`),
                                                 UNIQUE KEY `branch_id` (`branch_id`),
                                                 UNIQUE KEY `username` (`username`),
                                                 CONSTRAINT `fk_branch` FOREIGN KEY (`branch_id`) REFERENCES `restaurant_branches` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐厅店长表，每个门店对应唯一店长，用于审批及管理权限';

-- 导出  表 order_dish.chefs 结构
CREATE TABLE IF NOT EXISTS `chefs` (
                                       `id` int(11) NOT NULL AUTO_INCREMENT,
                                       `username` varchar(50) NOT NULL,
                                       `password_hash` varchar(255) NOT NULL,
                                       `name` varchar(100) NOT NULL,
                                       `phone` varchar(20) DEFAULT NULL,
                                       `email` varchar(100) DEFAULT NULL,
                                       `branch_id` int(11) DEFAULT NULL,
                                       `hire_date` date DEFAULT NULL,
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `username` (`username`),
                                       KEY `fk_chef_branch` (`branch_id`),
                                       CONSTRAINT `fk_chef_branch` FOREIGN KEY (`branch_id`) REFERENCES `restaurant_branches` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导出  表 order_dish.counters 结构
CREATE TABLE IF NOT EXISTS `counters` (
                                          `id` int(11) NOT NULL AUTO_INCREMENT,
                                          `username` varchar(50) NOT NULL,
                                          `password_hash` varchar(255) NOT NULL,
                                          `name` varchar(100) NOT NULL,
                                          `phone` varchar(20) DEFAULT NULL,
                                          `email` varchar(100) DEFAULT NULL,
                                          `branch_id` int(11) DEFAULT NULL,
                                          `hire_date` date DEFAULT NULL COMMENT '入职日期',
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `username` (`username`),
                                          KEY `fk_receptionist_branch` (`branch_id`),
                                          CONSTRAINT `fk_receptionist_branch` FOREIGN KEY (`branch_id`) REFERENCES `restaurant_branches` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导出  表 order_dish.coupons 结构
CREATE TABLE IF NOT EXISTS `coupons` (
                                         `coupon_id` int(11) NOT NULL AUTO_INCREMENT,
                                         `user_id` int(11) DEFAULT NULL,
                                         `start_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                         `expiry_date` timestamp NOT NULL,
                                         `min_threshold` decimal(10,2) NOT NULL,
                                         `discount_amount` decimal(10,2) NOT NULL,
                                         PRIMARY KEY (`coupon_id`),
                                         KEY `user_id` (`user_id`),
                                         CONSTRAINT `coupons_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=212 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导出  表 order_dish.dishes 结构
CREATE TABLE IF NOT EXISTS `dishes` (
                                        `dish_id` int(11) NOT NULL AUTO_INCREMENT,
                                        `dish_name` varchar(100) NOT NULL,
                                        `price` decimal(10,2) NOT NULL,
                                        `description` text,
                                        `category` varchar(50) DEFAULT NULL,
                                        `image_url` varchar(255) DEFAULT NULL,
                                        `is_available` tinyint(1) DEFAULT '1',
                                        PRIMARY KEY (`dish_id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导出  表 order_dish.hrs 结构
CREATE TABLE IF NOT EXISTS `hrs` (
                                     `id` int(11) NOT NULL AUTO_INCREMENT,
                                     `username` varchar(50) NOT NULL,
                                     `password_hash` varchar(255) NOT NULL,
                                     `name` varchar(100) NOT NULL,
                                     `phone` varchar(20) DEFAULT NULL,
                                     `email` varchar(100) DEFAULT NULL,
                                     `branch_id` int(11) DEFAULT NULL,
                                     `hire_date` date DEFAULT NULL COMMENT '入职日期',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `username` (`username`),
                                     KEY `fk_hr_branch` (`branch_id`),
                                     CONSTRAINT `fk_hr_branch` FOREIGN KEY (`branch_id`) REFERENCES `restaurant_branches` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导出  表 order_dish.orders 结构
CREATE TABLE IF NOT EXISTS `orders` (
                                        `order_id` int(11) NOT NULL AUTO_INCREMENT,
                                        `user_id` int(11) DEFAULT NULL,
                                        `order_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                        `total_amount` decimal(10,2) NOT NULL,
                                        `status` varchar(20) DEFAULT 'pending',
                                        `payment_method` varchar(50) DEFAULT NULL,
                                        `delivery_address` text,
                                        `phone_number` varchar(20) DEFAULT NULL,
                                        `coupon_id` int(11) DEFAULT NULL,
                                        `discount_amount` decimal(10,2) DEFAULT '0.00',
                                        `final_amount` decimal(10,2) NOT NULL,
                                        PRIMARY KEY (`order_id`),
                                        KEY `user_id` (`user_id`),
                                        KEY `coupon_id` (`coupon_id`),
                                        CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
                                        CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`coupon_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导出  表 order_dish.restaurant_branches 结构
CREATE TABLE IF NOT EXISTS `restaurant_branches` (
                                                     `id` int(11) NOT NULL AUTO_INCREMENT,
                                                     `name` varchar(100) NOT NULL COMMENT '门店名称',
                                                     `address` varchar(255) NOT NULL COMMENT '门店地址',
                                                     `phone` varchar(20) DEFAULT NULL COMMENT '门店联系电话',
                                                     `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度，用于地图定位',
                                                     `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度，用于地图定位',
                                                     `opening_hours` varchar(100) DEFAULT NULL COMMENT '营业时间',
                                                     `status` enum('active','inactive') DEFAULT 'active' COMMENT '门店状态：active-营业中，inactive-暂停营业',
                                                     `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                     `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                     PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐厅门店表，存储各个门店的基本信息';

-- 导出  表 order_dish.users 结构
CREATE TABLE IF NOT EXISTS `users` (
                                       `user_id` int(11) NOT NULL AUTO_INCREMENT,
                                       `username` varchar(50) NOT NULL,
                                       `password_hash` varchar(255) NOT NULL,
                                       `email` varchar(100) DEFAULT NULL,
                                       `phone` varchar(20) DEFAULT NULL,
                                       `address` text,
                                       `registration_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                       `last_login` timestamp NULL DEFAULT NULL,
                                       `is_active` tinyint(1) DEFAULT '1',
                                       PRIMARY KEY (`user_id`),
                                       UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 导出  表 order_dish.waiters 结构
CREATE TABLE IF NOT EXISTS `waiters` (
                                         `id` int(11) NOT NULL AUTO_INCREMENT,
                                         `username` varchar(50) NOT NULL,
                                         `password_hash` varchar(255) NOT NULL,
                                         `name` varchar(100) NOT NULL,
                                         `phone` varchar(20) DEFAULT NULL,
                                         `email` varchar(100) DEFAULT NULL,
                                         `branch_id` int(11) DEFAULT NULL,
                                         `hire_date` date DEFAULT NULL COMMENT '入职日期',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `username` (`username`),
                                         KEY `fk_waiter_branch` (`branch_id`),
                                         CONSTRAINT `fk_waiter_branch` FOREIGN KEY (`branch_id`) REFERENCES `restaurant_branches` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 注意：由于MySQL 5.x版本限制，以下功能需要特别处理：
-- 1. 生成列(GENERATED ALWAYS AS)在MySQL 5.7.6+才支持，5.6及以下版本需要使用触发器
-- 2. utf8mb4_0900_ai_ci排序规则改为utf8mb4_unicode_ci以兼容5.x版本
-- 3. DEFAULT ENCRYPTION='N'选项已移除，5.x版本不支持
-- 4. int类型已改为int(11)以兼容旧版本

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;