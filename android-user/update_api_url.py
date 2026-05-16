#!/usr/bin/env python3
"""
更新Android应用API地址的脚本
使用方法：python update_api_url.py [IP地址]
"""

import os
import sys
import re

def update_api_url(ip_address=None):
    """更新API地址"""
    
    # 如果没有提供IP地址，尝试从配置文件读取
    if not ip_address:
        config_file = os.path.join(os.path.dirname(__file__), "server_config.txt")
        if os.path.exists(config_file):
            with open(config_file, 'r', encoding='utf-8') as f:
                content = f.read()
                # 查找SERVER_IP配置
                match = re.search(r'SERVER_IP=([^\s]+)', content)
                if match:
                    ip_address = match.group(1)
                    print(f"从配置文件读取IP地址: {ip_address}")
    
    if not ip_address:
        print("错误：未提供IP地址")
        print("使用方法：python update_api_url.py [IP地址]")
        print("或者修改 server_config.txt 文件中的 SERVER_IP 配置")
        return False
    
    # 验证IP地址格式
    ip_pattern = r'^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$'
    if not re.match(ip_pattern, ip_address):
        print(f"错误：IP地址格式不正确: {ip_address}")
        return False
    
    # 更新strings.xml文件
    strings_file = os.path.join(os.path.dirname(__file__), 
                               "app", "src", "main", "res", "values", "strings.xml")
    
    if not os.path.exists(strings_file):
        print(f"错误：找不到strings.xml文件: {strings_file}")
        return False
    
    # 读取当前内容
    with open(strings_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 更新API地址
    new_api_url = f"http://{ip_address}:8080/api/"
    new_content = re.sub(
        r'<string name="api_base_url"[^>]*>.*?</string>',
        f'<string name="api_base_url" translatable="false">{new_api_url}</string>',
        content
    )
    
    # 写入更新后的内容
    with open(strings_file, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"✅ 已更新API地址为: {new_api_url}")
    print("请重新构建APK：gradlew assembleDebug")
    return True

if __name__ == "__main__":
    if len(sys.argv) > 1:
        ip_address = sys.argv[1]
        update_api_url(ip_address)
    else:
        update_api_url()
