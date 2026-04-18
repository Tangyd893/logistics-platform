package com.logistics.system.domain.dto;

/**
 * 登录响应
 */
public class LoginResponse {

    private String token;
    private String refreshToken;
    private long expiresIn;
    private UserInfo user;

    public LoginResponse() {
    }

    public LoginResponse(String token, String refreshToken, long expiresIn, UserInfo user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /**
     * 用户信息
     */
    public static class UserInfo {

        private Long id;
        private String username;
        private String displayName;
        private String roleCode;
        private String avatar;

        public UserInfo() {
        }

        public UserInfo(Long id, String username, String displayName, String roleCode, String avatar) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
            this.roleCode = roleCode;
            this.avatar = avatar;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getRoleCode() {
            return roleCode;
        }

        public void setRoleCode(String roleCode) {
            this.roleCode = roleCode;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}
