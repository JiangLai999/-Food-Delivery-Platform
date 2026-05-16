-- 外卖订餐系统初始化数据
-- 数据库: food_delivery
-- 执行: mysql -u root -p food_delivery < init_data.sql

USE food_delivery;

-- ========== 管理员 ==========
INSERT INTO admin (username, password, nickname, email, status) 
VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', '超级管理员', 'admin@fooddelivery.com', 1)
ON DUPLICATE KEY UPDATE nickname = '超级管理员';
-- 密码: admin (MD5: 21232f297a57a5a743894a0e4a801fc3)
-- 如果需要改为88888888，使用: UPDATE admin SET password = '8ddcff3a80f4189ca1c9d4d902c3c909' WHERE username = 'admin';

-- ========== 商家分类 ==========
INSERT INTO merchant_category (category_name, icon, sort_order) VALUES
('美食', 'https://example.com/icons/food.png', 1),
('甜点', 'https://example.com/icons/dessert.png', 2),
('饮品', 'https://example.com/icons/drink.png', 3),
('超市便利', 'https://example.com/icons/supermarket.png', 4),
('生鲜', 'https://example.com/icons/fresh.png', 5)
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

-- ========== 商家 ==========
-- 密码: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)

-- 商家1: 老北京炸酱面馆
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138001', 'e10adc3949ba59abbe56e057f20f883e', '老北京炸酱面馆', '/images/merchant/merchant_1_logo.jpg', NULL, NULL,
 '王老板', '13800138001', '北京市', '北京市', '朝阳区', '建国路88号SOHO现代城B座', 116.4789270, 39.9077610, 
 1, '美食', '地道老北京风味，传承百年手艺', '本店招牌炸酱面每日限量供应', '09:00-21:00', 2.50, 20.00, 1.00, 4.80, 27, 1)
ON DUPLICATE KEY UPDATE merchant_name = '老北京炸酱面馆';

-- 商家2: 川味小厨
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138002', 'e10adc3949ba59abbe56e057f20f883e', '川味小厨', '/images/merchant/merchant_2_logo.jpg', NULL, NULL,
 '李老板', '13800138002', '北京市', '北京市', '朝阳区', '望京街10号望京SOHO', 116.4806390, 40.0002390, 
 1, '美食', '正宗川菜，麻辣鲜香', '本店所有菜品均使用纯正菜籽油', '10:00-22:00', 3.00, 25.00, 1.50, 4.60, 32, 1)
ON DUPLICATE KEY UPDATE merchant_name = '川味小厨';

-- 商家3: 广东烧腊店
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138003', 'e10adc3949ba59abbe56e057f20f883e', '广东烧腊店', '/images/merchant/merchant_3_logo.jpg', NULL, NULL,
 '张老板', '13800138003', '北京市', '北京市', '朝阳区', '三里屯路19号三里屯太古里', 116.4550560, 39.9342990, 
 1, '美食', '广式烧腊，明火烤制', '每日新鲜出炉，限量供应', '10:30-20:30', 2.00, 20.00, 1.00, 4.70, 21, 1)
ON DUPLICATE KEY UPDATE merchant_name = '广东烧腊店';

-- 商家4: 甜心烘焙坊
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138004', 'e10adc3949ba59abbe56e057f20f883e', '甜心烘焙坊', '/images/merchant/merchant_4_logo.jpg', NULL, NULL,
 '赵女士', '13800138004', '北京市', '北京市', '海淀区', '中关村大街15号中关村广场', 116.3157480, 39.9834970, 
 2, '甜点', '法式甜点，现烤现卖', '每日新鲜烘焙，低糖低脂', '08:00-20:00', 1.50, 15.00, 0.50, 4.90, 18, 1)
ON DUPLICATE KEY UPDATE merchant_name = '甜心烘焙坊';

-- 商家5: 芋见甜品
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138005', 'e10adc3949ba59abbe56e057f20f883e', '芋见甜品', '/images/merchant/merchant_5_logo.jpg', NULL, NULL,
 '陈先生', '13800138005', '北京市', '北京市', '海淀区', '五道口购物中心3F', 116.3385830, 39.9926110, 
 2, '甜点', '台式甜品，手工芋圆', '招牌芋圆烧仙草每日现煮', '11:00-22:00', 2.00, 18.00, 1.00, 4.50, 15, 1)
ON DUPLICATE KEY UPDATE merchant_name = '芋见甜品';

-- 商家6: 喜茶
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138006', 'e10adc3949ba59abbe56e057f20f883e', '喜茶（模拟店）', '/images/merchant/merchant_7_logo.jpg', NULL, NULL,
 '刘店长', '13800138006', '北京市', '北京市', '朝阳区', '朝阳大悦城1F', 116.5211090, 39.9234840, 
 3, '饮品', '灵感之茶，中国制造', '当季限定水果茶系列', '10:00-22:00', 2.50, 20.00, 1.00, 4.80, 26, 1)
ON DUPLICATE KEY UPDATE merchant_name = '喜茶（模拟店）';

-- 商家7: 瑞幸咖啡
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138007', 'e10adc3949ba59abbe56e057f20f883e', '瑞幸咖啡', '/images/merchant/merchant_6_logo.jpg', NULL, NULL,
 '王经理', '13800138007', '北京市', '北京市', '朝阳区', '建国门外大街1号国贸商城', 116.4592100, 39.9088600, 
 3, '饮品', '专业咖啡新鲜萃取', '新用户首杯免单', '07:00-20:00', 1.00, 15.00, 0.50, 4.70, 32, 1)
ON DUPLICATE KEY UPDATE merchant_name = '瑞幸咖啡';

-- 商家8: 美宜佳
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138008', 'e10adc3949ba59abbe56e057f20f883e', '美宜佳（模拟店）', '/images/merchant/merchant_8_logo.jpg', NULL, NULL,
 '周店长', '13800138008', '北京市', '北京市', '朝阳区', '东三环中路39号建外SOHO', 116.4597300, 39.9046900, 
 4, '超市便利', '24小时便利店', '满29减5，欢迎光临', '00:00-24:00', 2.00, 10.00, 0.00, 4.50, 18, 1)
ON DUPLICATE KEY UPDATE merchant_name = '美宜佳（模拟店）';

-- 商家9: 全家便利店
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138009', 'e10adc3949ba59abbe56e057f20f883e', '全家便利店', '/images/merchant/merchant_9_logo.jpg', NULL, NULL,
 '吴店长', '13800138009', '北京市', '北京市', '朝阳区', '朝阳北路101号朝阳大悦城', 116.5208200, 39.9243000, 
 4, '超市便利', '全家就是你家', '关东煮、便当，三明治', '00:00-24:00', 1.50, 15.00, 0.50, 4.60, 27, 1)
ON DUPLICATE KEY UPDATE merchant_name = '全家便利店';

-- 商家10: 每日优鲜
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138010', 'e10adc3949ba59abbe56e057f20f883e', '每日优鲜（模拟）', '/images/merchant/merchant_10_logo.jpg', NULL, NULL,
 '郑经理', '13800138010', '北京市', '北京市', '朝阳区', '酒仙桥路10号恒通国际创新园', 116.4980200, 39.9692100, 
 5, '生鲜', '1小时新鲜到家', '进口水果、肉禽蛋奶', '08:00-22:00', 3.00, 30.00, 1.00, 4.70, 32, 1)
ON DUPLICATE KEY UPDATE merchant_name = '每日优鲜（模拟）';

-- 商家11: 盒马鲜生
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138011', 'e10adc3949ba59abbe56e057f20f883e', '盒马鲜生（模拟）', '/images/merchant/merchant_11_logo.jpg', NULL, NULL,
 '孙店长', '13800138011', '北京市', '北京市', '朝阳区', '朝阳北路101号朝阳大悦城B1', 116.5215600, 39.9238800, 
 5, '生鲜', '生鲜超市+餐饮体验', '日日鲜、帝皇鲜', '09:00-22:00', 5.00, 35.00, 1.00, 4.90, 52, 1)
ON DUPLICATE KEY UPDATE merchant_name = '盒马鲜生（模拟）';

-- 商家12: 钱大妈
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138012', 'e10adc3949ba59abbe56e057f20f883e', '钱大妈', '/images/merchant/merchant_12_logo.jpg', NULL, NULL,
 '钱老板', '13800138012', '北京市', '北京市', '朝阳区', '双井街道广渠路36号院', 116.4577600, 39.8917500, 
 5, '生鲜', '不卖隔夜肉', '社区生鲜连锁', '07:30-21:00', 2.00, 20.00, 0.50, 4.50, 27, 1)
ON DUPLICATE KEY UPDATE merchant_name = '钱大妈';

-- 商家13: 蜜雪冰城
INSERT INTO merchant (phone, password, merchant_name, logo, banner, contact_person, contact_phone, 
                     province, city, district, detail_address, longitude, latitude, 
                     category_id, category_name, description, notice, business_hours, 
                     delivery_fee, min_order_amount, pack_fee, avg_rating, sales_volume, status) VALUES
('13800138013', 'e10adc3949ba59abbe56e057f20f883e', '蜜雪冰城', '/images/merchant/merchant_13_logo.jpg', NULL, NULL,
 '雪王', '13800138013', '北京市', '北京市', '海淀区', '中关村大街1号', 116.3123450, 39.9834560, 
 3, '饮品', '让全球每个人享受高质平价的美味', '你爱我我爱你，蜜雪冰城甜蜜蜜', '09:00-23:00', 1.00, 6.00, 0.00, 4.60, 88, 1)
ON DUPLICATE KEY UPDATE merchant_name = '蜜雪冰城';

-- ========== 餐品分类 ==========
INSERT INTO food_category (merchant_id, category_name, sort_order) VALUES
(1, '招牌主食', 1), (1, '特色小吃', 2), (1, '饮品', 3),
(2, '招牌热卖', 1), (2, '经典川菜', 2), (2, '汤品', 3),
(4, '人气蛋糕', 1), (4, '面包西点', 2), (4, '饮品', 3)
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name);

-- ========== 餐品 ==========
-- 商家1: 老北京炸酱面馆
INSERT INTO food_item (merchant_id, category_id, category_name, food_name, image, description, price, original_price, stock, sales_volume, status) VALUES
(1, 1, '招牌主食', '招牌炸酱面', '/images/food/105/food_105.jpg', '传统老北京风味，酱香浓郁', 22.50, 25.00, 999, 1890, 1),
(1, 2, '特色小吃', '老北京炖吊子', '/images/food/106/food_106.jpg', '地道小吃，汤浓味美', 38.00, 42.00, 500, 620, 1),
(1, 2, '特色小吃', '炸灌肠', '/images/food/107/food_107.jpg', '酥脆可口', 16.00, 18.00, 800, 1130, 1)
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name);

-- 商家2: 川味小厨
INSERT INTO food_item (merchant_id, category_id, category_name, food_name, image, description, price, original_price, stock, sales_volume, status) VALUES
(2, 4, '招牌热卖', '麻婆豆腐', '/images/food/108/food_108.jpg', '麻辣鲜香，下饭必备', 28.00, 32.00, 600, 2340, 1),
(2, 4, '招牌热卖', '水煮鱼', '/images/food/109/food_109.jpg', '活鱼现杀，口感嫩滑', 68.00, 78.00, 300, 1520, 1)
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name);

-- 商家3: 广东烧腊店
INSERT INTO food_item (merchant_id, category_id, category_name, food_name, image, description, price, original_price, stock, sales_volume, status) VALUES
(3, NULL, NULL, '蜜汁叉烧饭', '/images/food/110/food_110.jpg', '搭配例汤和蔬菜', 32.00, 35.00, 400, 1280, 1),
(3, NULL, NULL, '烧鸭拼盘', '/images/food/111/food_111.jpg', '半只烧鸭，配酸梅酱', 45.00, 48.00, 350, 870, 1)
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name);

-- 商家4: 甜心烘焙坊
INSERT INTO food_item (merchant_id, category_id, category_name, food_name, image, description, price, original_price, stock, sales_volume, status) VALUES
(4, 7, '人气蛋糕', '提拉米苏', '/images/food/112/food_112.jpg', '意大利经典，香醇浓郁', 28.00, 32.00, 200, 1670, 1),
(4, 7, '人气蛋糕', '草莓奶油蛋糕', '/images/food/113/food_113.jpg', '新鲜草莓，动物奶油', 35.00, 38.00, 150, 1420, 1)
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name);

-- 商家5: 芋见甜品
INSERT INTO food_item (merchant_id, category_id, category_name, food_name, image, description, price, original_price, stock, sales_volume, status) VALUES
(5, NULL, NULL, '芋圆烧仙草', '/images/food/114/food_114.jpg', '手工芋圆，清凉解暑', 22.00, 25.00, 300, 1130, 1),
(5, NULL, NULL, '杨枝甘露', '/images/food/115/food_115.jpg', '芒果西柚，酸甜可口', 26.00, 28.00, 250, 890, 1)
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name);

-- 商家6: 喜茶
INSERT INTO food_item (merchant_id, category_id, category_name, food_name, image, description, price, original_price, stock, sales_volume, status) VALUES
(6, NULL, NULL, '多肉葡萄', '/images/food/116/food_116.jpg', '手剥葡萄，茶香浓郁', 28.00, 32.00, 500, 3420, 1),
(6, NULL, NULL, '芝芝莓莓', '/images/food/117/food_117.jpg', '草莓+树莓，酸甜清爽', 30.00, 34.00, 500, 2890, 1)
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name);

-- 商家7: 瑞幸咖啡
INSERT INTO food_item (merchant_id, category_id, category_name, food_name, image, description, price, original_price, stock, sales_volume, status) VALUES
(7, NULL, NULL, '生椰拿铁', '/images/food/118/food_118.jpg', '椰香浓郁，冷热皆宜', 22.00, 25.00, 800, 4110, 1),
(7, NULL, NULL, '厚乳拿铁', '/images/food/119/food_119.jpg', '奶香醇厚', 24.00, 27.00, 800, 3270, 1)
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name);

-- 商家13: 蜜雪冰城
INSERT INTO food_item (merchant_id, category_id, category_name, food_name, image, description, price, original_price, stock, sales_volume, status) VALUES
(13, NULL, NULL, '冰鲜柠檬水', '/images/food/130/food_130.jpg', '清爽解渴，4元秒杀', 4.00, 5.00, 1000, 5680, 1),
(13, NULL, NULL, '蜜桃四季春', '/images/food/131/food_131.jpg', '鲜桃果肉，清爽宜人', 6.00, 7.00, 800, 3890, 1),
(13, NULL, NULL, '摇摇奶昔', '/images/food/132/food_132.jpg', '香浓奶昔，摇一摇更好喝', 6.00, 8.00, 600, 2560, 1),
(13, NULL, NULL, '冰淇淋', '/images/food/133/food_133.jpg', '香草冰淇淋，2元一个', 2.00, 3.00, 2000, 12580, 1),
(13, NULL, NULL, '蜜桃圣代', '/images/food/134/food_134.jpg', '桃桃圣代，甜蜜蜜', 5.00, 6.00, 800, 4560, 1)
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name);

-- ========== 用户 ==========
-- 密码: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO user (phone, password, nickname, gender, status) VALUES
('13900000001', 'e10adc3949ba59abbe56e057f20f883e', '测试用户1', 1, 1),
('13900000002', 'e10adc3949ba59abbe56e057f20f883e', '用户0002', 2, 1),
('13900000003', 'e10adc3949ba59abbe56e057f20f883e', '用户0003', 1, 1)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

-- ========== 骑手 ==========
INSERT INTO rider (rider_name, phone, status, current_longitude, current_latitude, total_orders, rating) VALUES
('王师傅', '13900001001', 1, 116.478927, 39.907761, 1520, 4.80),
('李师傅', '13900001002', 1, 116.480639, 40.000239, 2340, 4.90),
('张师傅', '13900001003', 1, 116.455056, 39.934299, 980, 4.70)
ON DUPLICATE KEY UPDATE rider_name = VALUES(rider_name);

-- ========== 优惠券 ==========
INSERT INTO coupon (title, description, amount, min_spend, start_time, end_time, total_count, remain_count, merchant_id, category_ids, status) VALUES
('新人专享券', '无门槛立减5元', 5.00, 0.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1000, 950, NULL, NULL, 1),
('满20减10', '订单满20元减10元', 10.00, 20.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 500, 480, NULL, NULL, 1),
('满38减18', '订单满38元减18元', 18.00, 38.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 300, 280, NULL, NULL, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- ========== 用户优惠券 ==========
INSERT INTO user_coupon (user_id, coupon_id, status) VALUES
(1, 1, 0), (1, 2, 0), (1, 3, 0),
(2, 1, 0), (2, 2, 0), (2, 3, 0);

-- ========== 提交 ==========
COMMIT;

SELECT '数据初始化完成！' AS message;
