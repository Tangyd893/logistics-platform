package com.logistics.system.service;

import com.logistics.common.exception.BusinessException;
import com.logistics.system.domain.dto.LoginRequest;
import com.logistics.system.domain.dto.LoginResponse;
import com.logistics.system.domain.entity.SysUser;
import com.logistics.system.repository.SysUserRepository;
import com.logistics.system.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SysUserRepository userRepository;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       SysUserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(request.getUsername());

            SysUser user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.getRoleCode(),
                    user.getAvatar()
            );

            log.info("用户登录成功: {}", request.getUsername());

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    tokenProvider.getExpirationSeconds(),
                    userInfo
            );
        } catch (BadCredentialsException e) {
            log.warn("用户登录失败: {}", request.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }
    }

    /**
     * 登出
     */
    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("用户登出成功");
    }

    /**
     * 刷新 Token
     */
    public LoginResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(401, "Refresh Token 无效或已过期");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, user.getPassword())
        );

        String newAccessToken = tokenProvider.generateAccessToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRoleCode(),
                user.getAvatar()
        );

        return new LoginResponse(newAccessToken, newRefreshToken, tokenProvider.getExpirationSeconds(), userInfo);
    }
}
