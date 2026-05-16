package com.fooddelivery;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.fooddelivery.mapper")
@EnableScheduling
@Slf4j
public class FoodDeliveryApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(FoodDeliveryApplication.class, args);
        printStartupInfo(context);
        cleanupChatRecords(context);
    }

    private static void printStartupInfo(ConfigurableApplicationContext context) {
        System.out.println("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║                  外卖订餐系统启动成功！                       ║\n" +
                "╚══════════════════════════════════════════════════════════╝\n");

        System.out.println("📡 系统信息：");
        System.out.println("  🔹 服务地址: http://localhost:8080");
        System.out.println("  🔹 API前缀: /api");
        System.out.println("  🔹 运行环境: Java " + System.getProperty("java.version"));

        System.out.println("\n📚 API 文档：");
        System.out.println("  ✅ Swagger UI: http://localhost:8080/swagger-ui/index.html");
        System.out.println("  ✅ API JSON: http://localhost:8080/v3/api-docs");

        System.out.println("\n🧪 测试接口：");
        System.out.println("  🔹 健康检查: http://localhost:8080/api/test/health");
        System.out.println("  🔹 系统信息: http://localhost:8080/api/test/info");
        System.out.println("  🔹 公开接口: http://localhost:8080/api/test/public");

        System.out.println("\n🔧 数据库：");
        System.out.println("  🔸 地址: localhost:3306/food_delivery");
        System.out.println("  🔸 用户: root");

        System.out.println("\n🚀 系统监控：");
        System.out.println("  ✅ WebSocket: 已启动");
        System.out.println("  ✅ 定时任务: 已启动");
        System.out.println("  ✅ 骑手模拟: 已启动");

        System.out.println("\n📱 测试账号：");
        System.out.println("  👤 管理员: admin / 88888888");
        System.out.println("  🏪 商家: 13800138001 / 123456 (需审核)");
        System.out.println("  👤 用户: 13900000001 / 123456");

        System.out.println("\n🔗 快速访问链接：");
        System.out.println("  1. 复制访问Swagger: http://localhost:8080/swagger-ui/index.html");
        System.out.println("  2. 复制测试健康检查: http://localhost:8080/api/test/health");

        System.out.println("\n" +
                "════════════════════════════════════════════════════════════\n" +
                "提示: 如果Swagger无法访问，请尝试 http://localhost:8080/api/swagger-ui/index.html\n" +
                "════════════════════════════════════════════════════════════\n");
    }

    private static void cleanupChatRecords(ConfigurableApplicationContext context) {
        try {
            javax.sql.DataSource dataSource = context.getBean(javax.sql.DataSource.class);
            try (java.sql.Connection conn = dataSource.getConnection();
                 java.sql.Statement stmt = conn.createStatement()) {
                
                int deleted = stmt.executeUpdate("DELETE FROM chat_message WHERE 1=1");
                log.info("启动时清理聊天记录成功，删除了 {} 条记录", deleted);
            }
        } catch (Exception e) {
            log.error("清理聊天记录失败", e);
        }
    }
}