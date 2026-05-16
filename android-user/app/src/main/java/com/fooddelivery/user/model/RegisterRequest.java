package com.fooddelivery.user.model;

/**
 * 注册请求模型
 */
public class RegisterRequest {
    private String phone;
    private String password;
    private String code; // 验证码
    private String nickname; // 昵称

    public RegisterRequest() {
    }

    public RegisterRequest(String phone, String password, String code) {
        this.phone = phone;
        this.password = password;
        this.code = code;
    }

    public RegisterRequest(String phone, String password, String code, String nickname) {
        this.phone = phone;
        this.password = password;
        this.code = code;
        this.nickname = nickname;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
