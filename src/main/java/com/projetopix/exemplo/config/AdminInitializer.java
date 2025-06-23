package com.projetopix.exemplo.config;

import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AdminInitializer {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createAdminUser() {
        String cpfAdmin = "00000000000";
        if (userInfoRepository.findByCpf(cpfAdmin).isEmpty()) {
            UserInfo admin = UserInfo.builder()
                    .cpf(cpfAdmin)
                    .password(passwordEncoder.encode("123456"))
                    .roles("ROLE_ADMIN")
                    .score(5.0)
                    .saldo(1000.0)
                    .seloVerificado("sim")
                    .bloqueado(false)
                    .dataNascimento("2000-01-01") 
                    .build();
            userInfoRepository.save(admin);
        }
    }
}