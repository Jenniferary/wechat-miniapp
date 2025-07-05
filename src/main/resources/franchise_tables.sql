-- 加盟功能相关数据库表结构
-- 创建时间: 2025-01-15
-- 说明: 为餐厅管理系统添加加盟店铺管理功能

USE `order_dish`;

-- 导出  表 order_dish.franchise_applications 结构
CREATE TABLE IF NOT EXISTS `franchise_applications` (
                                                        `id` int(11) NOT NULL AUTO_INCREMENT,
                                                        `applicant_name` varchar(100) NOT NULL COMMENT '申请人姓名',
                                                        `applicant_phone` varchar(20) NOT NULL COMMENT '申请人电话',
                                                        `applicant_email` varchar(100) DEFAULT NULL COMMENT '申请人邮箱',
                                                        `applicant_id_card` varchar(18) NOT NULL COMMENT '申请人身份证号',
                                                        `business_experience` text COMMENT '经营经验描述',
                                                        `investment_amount` decimal(12,2) NOT NULL COMMENT '投资金额',
                                                        `proposed_location` varchar(500) NOT NULL COMMENT '拟开店地址',
                                                        `proposed_store_name` varchar(255) NOT NULL COMMENT '拟开店名称',
                                                        `latitude` double DEFAULT NULL COMMENT '纬度',
                                                        `longitude` double DEFAULT NULL COMMENT '经度',
                                                        `store_area` decimal(8,2) DEFAULT NULL COMMENT '店铺面积（平方米）',
                                                        `expected_opening_date` date DEFAULT NULL COMMENT '预计开业时间',
                                                        `application_reason` text COMMENT '申请理由',
                                                        `status` enum('待审核','审核中','审核通过','审核拒绝','已退回','已开业') DEFAULT '待审核' COMMENT '申请状态',
                                                        `review_comments` text COMMENT '审核意见',
                                                        `reviewed_by` varchar(100) DEFAULT NULL COMMENT '审核人',
                                                        `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
                                                        `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                                                        `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                        PRIMARY KEY (`id`),
                                                        UNIQUE KEY `uk_applicant_phone` (`applicant_phone`),
                                                        UNIQUE KEY `uk_applicant_id_card` (`applicant_id_card`),
                                                        KEY `idx_status` (`status`),
                                                        KEY `idx_location` (`latitude`,`longitude`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='加盟申请表';

-- 正在导出表  order_dish.franchise_applications 的数据：~3 rows (大约)
INSERT INTO `franchise_applications` (`id`, `applicant_name`, `applicant_phone`, `applicant_email`, `applicant_id_card`, `business_experience`, `investment_amount`, `proposed_location`, `proposed_store_name`, `latitude`, `longitude`, `store_area`, `expected_opening_date`, `application_reason`, `status`, `created_at`) VALUES
(1, '王小明', '13800138001', 'wangxiaoming@example.com', '110101199001011234', '有5年餐饮行业经验，曾经营过2家小型餐厅', 500000.00, '北京市朝阳区建国门外大街1号', '食尚阁建国门店', 39.908823, 116.434847, 200.00, '2025-09-01', '看好食尚阁品牌发展前景，希望在商务区开设分店', '待审核', '2025-01-15 10:30:00'),
(2, '李美丽', '13900139002', 'limeili@example.com', '110101199002022345', '餐饮管理专业毕业，有3年连锁餐厅管理经验', 800000.00, '北京市海淀区中关村大街32号', '食尚阁中关村店', 39.983497, 116.318042, 300.00, '2025-10-01', '中关村科技园区人流量大，适合开设高端餐厅', '审核通过', '2025-01-20 14:20:00'),
(3, '张大伟', '13700137003', 'zhangdawei@example.com', '110101199003033456', '有10年餐饮行业经验，目前经营3家餐厅', 1200000.00, '北京市西城区金融街15号', '食尚阁金融街店', 39.915119, 116.366034, 400.00, '2025-08-15', '金融街商务人群消费能力强，市场前景广阔', '待审核', '2025-02-01 09:15:00');

-- 导出  表 order_dish.franchise_stores 结构
CREATE TABLE IF NOT EXISTS `franchise_stores` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT,
                                                  `application_id` int(11) NOT NULL COMMENT '关联的申请ID',
                                                  `store_name` varchar(255) NOT NULL COMMENT '店铺名称',
                                                  `store_code` varchar(50) NOT NULL COMMENT '店铺编码',
                                                  `franchisee_name` varchar(100) NOT NULL COMMENT '加盟商姓名',
                                                  `franchisee_phone` varchar(20) NOT NULL COMMENT '加盟商电话',
                                                  `franchisee_email` varchar(100) DEFAULT NULL COMMENT '加盟商邮箱',
                                                  `store_address` varchar(500) NOT NULL COMMENT '店铺地址',
                                                  `latitude` double NOT NULL COMMENT '纬度',
                                                  `longitude` double NOT NULL COMMENT '经度',
                                                  `store_area` decimal(8,2) NOT NULL COMMENT '店铺面积（平方米）',
                                                  `investment_amount` decimal(12,2) NOT NULL COMMENT '投资金额',
                                                  `franchise_fee` decimal(10,2) NOT NULL COMMENT '加盟费',
                                                  `management_fee_rate` decimal(5,2) DEFAULT '5.00' COMMENT '管理费率（%）',
                                                  `contract_start_date` date NOT NULL COMMENT '合同开始日期',
                                                  `contract_end_date` date NOT NULL COMMENT '合同结束日期',
                                                  `opening_date` date DEFAULT NULL COMMENT '实际开业日期',
                                                  `status` enum('筹备中','已开业','暂停营业','已关闭') DEFAULT '筹备中' COMMENT '店铺状态',
                                                  `monthly_sales` decimal(12,2) DEFAULT '0.00' COMMENT '月销售额',
                                                  `last_inspection_date` date DEFAULT NULL COMMENT '最后检查日期',
                                                  `inspection_score` int(11) DEFAULT NULL COMMENT '检查评分（1-100）',
                                                  `notes` text COMMENT '备注信息',
                                                  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                  PRIMARY KEY (`id`),
                                                  UNIQUE KEY `uk_store_code` (`store_code`),
                                                  UNIQUE KEY `uk_application_id` (`application_id`),
                                                  KEY `idx_franchisee_phone` (`franchisee_phone`),
                                                  KEY `idx_status` (`status`),
                                                  KEY `idx_location` (`latitude`,`longitude`),
                                                  CONSTRAINT `fk_franchise_application` FOREIGN KEY (`application_id`) REFERENCES `franchise_applications` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='加盟店铺表';

-- 正在导出表  order_dish.franchise_stores 的数据：~1 rows (大约)
INSERT INTO `franchise_stores` (`id`, `application_id`, `store_name`, `store_code`, `franchisee_name`, `franchisee_phone`, `franchisee_email`, `store_address`, `latitude`, `longitude`, `store_area`, `investment_amount`, `franchise_fee`, `management_fee_rate`, `contract_start_date`, `contract_end_date`, `opening_date`, `status`, `monthly_sales`, `created_at`) VALUES
(1, 2, '食尚阁中关村店', 'FSG-ZGC-001', '李美丽', '13900139002', 'limeili@example.com', '北京市海淀区中关村大街32号', 39.983497, 116.318042, 300.00, 800000.00, 100000.00, 5.00, '2025-03-01', '2030-02-28', '2025-06-01', '已开业', 280000.00, '2025-02-15 16:45:00');

-- 创建加盟申请实体类对应的Java实体
/*
对应的Java实体类结构：

@Entity
@Table(name = "franchise_applications")
public class FranchiseApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;

    @Column(name = "applicant_phone", nullable = false, length = 20, unique = true)
    private String applicantPhone;

    @Column(name = "applicant_email", length = 100)
    private String applicantEmail;

    @Column(name = "applicant_id_card", nullable = false, length = 18, unique = true)
    private String applicantIdCard;

    @Column(name = "business_experience", columnDefinition = "TEXT")
    private String businessExperience;

    @Column(name = "investment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal investmentAmount;

    @Column(name = "proposed_location", nullable = false, length = 500)
    private String proposedLocation;

    @Column(name = "proposed_store_name", nullable = false, length = 255)
    private String proposedStoreName;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "store_area", precision = 8, scale = 2)
    private BigDecimal storeArea;

    @Column(name = "expected_opening_date")
    private LocalDate expectedOpeningDate;

    @Column(name = "application_reason", columnDefinition = "TEXT")
    private String applicationReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicationStatus status = ApplicationStatus.待审核;

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ApplicationStatus {
        待审核, 审核中, 审核通过, 审核拒绝, 已退回, 已开业
    }
}

@Entity
@Table(name = "franchise_stores")
public class FranchiseStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "application_id", nullable = false)
    private Integer applicationId;
    
    @Column(name = "store_name", nullable = false, length = 255)
    private String storeName;
    
    @Column(name = "store_code", nullable = false, length = 50, unique = true)
    private String storeCode;
    
    @Column(name = "franchisee_name", nullable = false, length = 100)
    private String franchiseeName;
    
    @Column(name = "franchisee_phone", nullable = false, length = 20)
    private String franchiseePhone;
    
    @Column(name = "franchisee_email", length = 100)
    private String franchiseeEmail;
    
    @Column(name = "store_address", nullable = false, length = 500)
    private String storeAddress;
    
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    @Column(name = "store_area", nullable = false, precision = 8, scale = 2)
    private BigDecimal storeArea;
    
    @Column(name = "investment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal investmentAmount;
    
    @Column(name = "franchise_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal franchiseFee;
    
    @Column(name = "management_fee_rate", precision = 5, scale = 2)
    private BigDecimal managementFeeRate = new BigDecimal("5.00");
    
    @Column(name = "contract_start_date", nullable = false)
    private LocalDate contractStartDate;
    
    @Column(name = "contract_end_date", nullable = false)
    private LocalDate contractEndDate;
    
    @Column(name = "opening_date")
    private LocalDate openingDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StoreStatus status = StoreStatus.筹备中;
    
    @Column(name = "monthly_sales", precision = 12, scale = 2)
    private BigDecimal monthlySales = BigDecimal.ZERO;
    
    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate;
    
    @Column(name = "inspection_score")
    private Integer inspectionScore;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum StoreStatus {
        筹备中, 已开业, 暂停营业, 已关闭
    }
}
*/