package com.computershop.main.services;

import com.computershop.main.entities.PasswordResetToken;
import com.computershop.main.entities.User;
import com.computershop.main.repositories.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetTokenService {
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    public PasswordResetToken createToken(User user) {
        // Xóa token cũ nếu có
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        
        // Tạo token mới
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        
        return tokenRepository.save(resetToken);
    }
    
    public Optional<PasswordResetToken> getToken(String token) {
        return tokenRepository.findByToken(token);
    }
    
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        
        if (resetTokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = resetTokenOpt.get();
        
        if (resetToken.getUsed()) {
            return false;
        }
        
        if (resetToken.isExpired()) {
            return false;
        }
        
        return true;
    }
    
    @Transactional
    public void markTokenAsUsed(String token) {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        resetTokenOpt.ifPresent(resetToken -> {
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
        });
    }
    
    @Transactional
    public void deleteToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }
    
    public void cleanExpiredTokens() {
        // Có thể tạo scheduled task để xóa token hết hạn
        tokenRepository.findAll().stream()
            .filter(PasswordResetToken::isExpired)
            .forEach(tokenRepository::delete);
    }
}
