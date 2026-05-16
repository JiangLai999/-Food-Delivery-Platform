package com.fooddelivery.controller;

import com.fooddelivery.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据备份Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/backup")
@Tag(name = "数据备份管理")
public class BackupController {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * 执行数据备份
     */
    @GetMapping("/execute")
    @Operation(summary = "执行数据备份")
    public Result<Map<String, Object>> executeBackup() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 提取数据库名
            String dbName = extractDatabaseName(dbUrl);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "backup_" + dbName + "_" + timestamp + ".sql";
            
            // 执行mysqldump命令
            String command = String.format("mysqldump -u%s -p%s %s", 
                    dbUsername, dbPassword, dbName);
            
            Process process = Runtime.getRuntime().exec(command);
            
            // 读取输出
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // 保存到文件
                String backupPath = System.getProperty("user.dir") + "/backups/";
                File dir = new File(backupPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                File backupFile = new File(backupPath + filename);
                try (PrintWriter writer = new PrintWriter(backupFile)) {
                    writer.write(sql.toString());
                }
                
                result.put("filename", filename);
                result.put("path", backupFile.getAbsolutePath());
                result.put("size", backupFile.length());
                result.put("timestamp", timestamp);
                
                log.info("数据备份成功: {}", filename);
                return Result.success(result);
            } else {
                return Result.error("备份失败，exit code: " + exitCode);
            }
        } catch (Exception e) {
            log.error("数据备份失败", e);
            return Result.error("备份失败: " + e.getMessage());
        }
    }

    private String extractDatabaseName(String url) {
        // 从jdbc:mysql://localhost:3306/food_delivery提取数据库名
        int lastSlash = url.lastIndexOf('/');
        int questionMark = url.indexOf('?', lastSlash);
        if (questionMark > 0) {
            return url.substring(lastSlash + 1, questionMark);
        }
        return url.substring(lastSlash + 1);
    }
}
