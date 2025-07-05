-- 加盟通知记录表结构
-- 创建时间: 2025-01-15
-- 说明: 存储发送给加盟申请人的通知记录

USE `order_dish`;

-- 创建通知记录表
CREATE TABLE IF NOT EXISTS `franchise_notifications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `application_id` int(11) NOT NULL COMMENT '关联的申请ID',
  `applicant_name` varchar(100) NOT NULL COMMENT '申请人姓名',
  `applicant_phone` varchar(20) NOT NULL COMMENT '申请人电话',
  `notification_type` varchar(50) NOT NULL COMMENT '通知类型',
  `content` text NOT NULL COMMENT '通知内容',
  `sent_by` varchar(100) DEFAULT NULL COMMENT '发送人',
  `sent_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `status` enum('已发送','发送失败','已读','未读') DEFAULT '已发送' COMMENT '通知状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_application_id` (`application_id`),
  KEY `idx_applicant_phone` (`applicant_phone`),
  KEY `idx_sent_at` (`sent_at`),
  KEY `idx_notification_type` (`notification_type`),
  CONSTRAINT `fk_notification_application` FOREIGN KEY (`application_id`) REFERENCES `franchise_applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加盟通知记录表';

-- 插入示例数据
INSERT INTO `franchise_notifications` (`application_id`, `applicant_name`, `applicant_phone`, `notification_type`, `content`, `sent_by`, `sent_at`) VALUES
(1, '王小明', '13800138001', 'review_start', '您的加盟申请已进入审核阶段，我们将在3-5个工作日内完成审核。', '系统管理员', '2025-01-16 10:00:00'),
(2, '李美丽', '13900139002', 'approval', '恭喜您！您的加盟申请已通过审核，请联系我们的工作人员进行后续合同签署事宜。', '审核专员', '2025-01-21 15:30:00'),
(3, '张大伟', '13700137003', 'material_request', '您的申请材料需要补充，请提供详细的资金证明和经营计划书。', '审核专员', '2025-02-02 09:45:00');