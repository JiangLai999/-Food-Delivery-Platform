package com.fooddelivery.user.model;

/**
 * 登录请求模型
 */
public class LoginRequest {
    private String phone;
    private String password;
    private Integer loginType;

    public LoginRequest() {
    }

    public LoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }

    public LoginRequest(String phone, String password, Integer loginType) {
        this.phone = phone;
        this.password = password;
        this.loginType = loginType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getLoginType() {
        return loginType;
    }

    public void setLoginType(Integer loginType) {
        this.loginType = loginType;
    }
}
