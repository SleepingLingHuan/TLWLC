package com.campus.trade.campustradebackend.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String message;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String avatar;
    }
} 