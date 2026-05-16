-- 添加商家拒绝原因字段
ALTER TABLE `merchant` ADD COLUMN `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因' AFTER `status`;