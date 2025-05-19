-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        8.1.0 - MySQL Community Server - GPL
-- 服务器操作系统:                      Win64
-- HeidiSQL 版本:                  12.6.0.6765
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 导出 order_dish 的数据库结构
CREATE DATABASE IF NOT EXISTS `order_dish` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `order_dish`;

-- 导出  表 order_dish.afternoon_tea 结构
CREATE TABLE IF NOT EXISTS `afternoon_tea` (
  `id` int NOT NULL AUTO_INCREMENT,
  `item_name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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

-- 导出  表 order_dish.coupons 结构
CREATE TABLE IF NOT EXISTS `coupons` (
  `coupon_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `start_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `expiry_date` timestamp NOT NULL,
  `min_threshold` decimal(10,2) NOT NULL,
  `discount_amount` decimal(10,2) NOT NULL,
  PRIMARY KEY (`coupon_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `coupons_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 导出  表 order_dish.delivery_persons 结构
CREATE TABLE IF NOT EXISTS `delivery_persons` (
  `delivery_person_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `status` enum('delivering','not_delivering') DEFAULT 'not_delivering',
  PRIMARY KEY (`delivery_person_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  order_dish.delivery_persons 的数据：~3 rows (大约)
INSERT INTO `delivery_persons` (`delivery_person_id`, `name`, `phone_number`, `status`) VALUES
	(1, 'John', '123-456-7890', 'not_delivering'),
	(2, 'Alice', '987-654-3210', 'not_delivering'),
	(3, 'Mike', '555-555-5555', 'not_delivering');

-- 导出  表 order_dish.dishes 结构
CREATE TABLE IF NOT EXISTS `dishes` (
  `dish_id` int NOT NULL AUTO_INCREMENT,
  `dish_name` varchar(255) NOT NULL,
  `dish_price` double NOT NULL,
  PRIMARY KEY (`dish_id`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  order_dish.dishes 的数据：~16 rows (大约)
INSERT INTO `dishes` (`dish_id`, `dish_name`, `dish_price`) VALUES
	(1, 'Braised Pork Belly', 53.5),
	(2, 'Kung Pao Chicken', 43),
	(3, 'Shredded Pork with Garlic Sauce', 40),
	(4, 'Steamed Sea Bass with Ginger and Scallions', 73),
	(5, 'Stir-Fried Tomato and Eggs', 38),
	(6, 'Mapo Tofu', 35),
	(7, 'Stir-Fried Yellow Beef', 55),
	(8, 'Sweet and Sour Pork', 61.18),
	(9, 'Beijing Duck', 128),
	(10, 'Shredded Pork in Beijing Sauce', 68),
	(11, 'Fish with Pickled Vegetables', 68.8),
	(12, 'Hot and Sour Rice Noodles', 18.8),
	(13, 'Wuchang Rice', 3),
	(14, 'Fried Dumplings', 22.8),
	(15, 'Boiled Beef', 56.8),
	(16, 'Roast Mutton Chop', 78);

-- 导出  表 order_dish.dishpackages 结构
CREATE TABLE IF NOT EXISTS `dishpackages` (
  `package_id` int NOT NULL AUTO_INCREMENT,
  `package_name` varchar(255) NOT NULL,
  `package_price` decimal(10,2) DEFAULT NULL,
  `dish_list` varchar(1000) NOT NULL,
  PRIMARY KEY (`package_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  order_dish.dishpackages 的数据：~4 rows (大约)
INSERT INTO `dishpackages` (`package_id`, `package_name`, `package_price`, `dish_list`) VALUES
	(1, 'couples meals', 128.05, 'Sweet and Sour Pork,Mapo Tofu,Kung Pao Chicken'),
	(2, 'Secret Garden Cuisine', 148.05, 'Steamed Sea Bass with Ginger and Scallions,Braised Pork Belly,Stir-Fried Tomato and Eggs'),
	(3, 'Asian Fusion Feast', 146.70, 'Stir-Fried Yellow Beef,Steamed Sea Bass with Ginger and Scallions,Mapo Tofu'),
	(4, 'meal-for-one', 55.62, 'Kung Pao Chicken,Hot and Sour Rice Noodles');

-- 导出  表 order_dish.orders 结构
CREATE TABLE IF NOT EXISTS `orders` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `table_number` varchar(255) DEFAULT NULL,
  `dish_list` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `time_ordered` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `discount_amount` decimal(10,2) DEFAULT NULL,
  `is_coupon_used` tinyint(1) DEFAULT NULL,
  `is_paid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`order_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 导出  表 order_dish.reviews 结构
CREATE TABLE IF NOT EXISTS `reviews` (
  `review_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int DEFAULT NULL,
  `rating` int DEFAULT NULL,
  `comment` text,
  `time_added` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  KEY `order_id` (`order_id`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 导出  表 order_dish.tables 结构
CREATE TABLE IF NOT EXISTS `tables` (
  `id` int NOT NULL AUTO_INCREMENT,
  `table_number` varchar(20) NOT NULL,
  `table_type` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL,
  `capacity` int NOT NULL,
  `user_id` int DEFAULT NULL,
  `reservation_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 正在导出表  order_dish.tables 的数据：~36 rows (大约)
INSERT INTO `tables` (`id`, `table_number`, `table_type`, `status`, `capacity`, `user_id`, `reservation_time`) VALUES
	(1, 'A1', 'small', 'available', 2, NULL, NULL),
	(2, 'A2', 'small', 'available', 2, NULL, NULL),
	(3, 'A3', 'small', 'available', 2, NULL, NULL),
	(4, 'A4', 'small', 'available', 2, NULL, NULL),
	(5, 'A5', 'small', 'available', 2, NULL, NULL),
	(6, 'A6', 'small', 'available', 2, NULL, NULL),
	(7, 'A7', 'small', 'available', 2, NULL, NULL),
	(8, 'A8', 'small', 'available', 2, NULL, NULL),
	(9, 'A9', 'small', 'available', 2, NULL, NULL),
	(10, 'A10', 'small', 'available', 2, NULL, NULL),
	(11, 'B1', 'medium', 'available', 4, NULL, NULL),
	(12, 'B2', 'medium', 'available', 4, NULL, NULL),
	(13, 'B3', 'medium', 'available', 4, NULL, NULL),
	(14, 'B4', 'medium', 'available', 4, NULL, NULL),
	(15, 'B5', 'medium', 'available', 4, NULL, NULL),
	(16, 'B6', 'medium', 'available', 4, NULL, NULL),
	(17, 'B7', 'medium', 'available', 4, NULL, NULL),
	(18, 'B8', 'medium', 'available', 4, NULL, NULL),
	(19, 'B9', 'medium', 'available', 4, NULL, NULL),
	(20, 'B10', 'medium', 'available', 4, NULL, NULL),
	(21, 'C1', 'large', 'available', 8, NULL, NULL),
	(22, 'C2', 'large', 'available', 8, NULL, NULL),
	(23, 'C3', 'large', 'available', 8, NULL, NULL),
	(24, 'C4', 'large', 'available', 8, NULL, NULL),
	(25, 'C5', 'large', 'available', 8, NULL, NULL),
	(26, 'C6', 'large', 'available', 8, NULL, NULL),
	(27, 'C7', 'large', 'available', 8, NULL, NULL),
	(28, 'C8', 'large', 'available', 8, NULL, NULL),
	(29, 'SMALL1', 'small private', 'available', 10, NULL, NULL),
	(30, 'SMALL2', 'small private', 'available', 10, NULL, NULL),
	(31, 'SMALL3', 'small private', 'available', 10, NULL, NULL),
	(32, 'SMALL4', 'small private', 'available', 10, NULL, NULL),
	(33, 'SMALL5', 'small private', 'available', 10, NULL, NULL),
	(34, 'BIG1', 'large private', 'available', 16, NULL, NULL),
	(35, 'BIG2', 'large private', 'available', 16, NULL, NULL),
	(36, 'BIG3', 'large private', 'available', 16, NULL, NULL);

-- 导出  表 order_dish.takeaway_orders 结构
CREATE TABLE IF NOT EXISTS `takeaway_orders` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `delivery_address` varchar(255) DEFAULT NULL,
  `dish_list` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `time_ordered` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `discount_amount` decimal(10,2) DEFAULT NULL,
  `is_coupon_used` tinyint(1) DEFAULT NULL,
  `is_paid` tinyint(1) DEFAULT NULL,
  `delivery_person_id` int DEFAULT NULL,
  `delivery_time` timestamp NULL DEFAULT NULL,
  `delivery_status` enum('pending','out_for_delivery','delivered') DEFAULT 'pending',
  `estimated_delivery_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



-- 导出  表 order_dish.users 结构
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,user_id
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `member_level` varchar(10) DEFAULT 'VIP0',
  `member_balance` decimal(10,2) DEFAULT NULL,
  `member_points` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `restaurant_branches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `latitude` double NOT NULL,  -- 纬度
  `longitude` double NOT NULL,  -- 经度
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
INSERT INTO `restaurant_branches` (`name`, `latitude`, `longitude`) VALUES
('Restaurant A', 39.9042, 116.4074),  -- 北京
('Restaurant B', 31.2304, 121.4737),  -- 上海
('Restaurant C', 22.5431, 114.0579);  -- 深圳


/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
order_dish