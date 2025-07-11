package com.projetopix.exemplo.controller;

import lombok.RequiredArgsConstructor;
import com.projetopix.exemplo.entity.AuthRequest;
import com.projetopix.exemplo.entity.UserInfo;
import com.projetopix.exemplo.service.JwtService;
import com.projetopix.exemplo.service.UserInfoService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserInfoService service;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @PostMapping("/addNewUser")
    public String addNewUser(@RequestBody UserInfo userInfo) {
        if (userInfo.getCpf() == null || userInfo.getCpf().isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório!");
        }
        if (userInfo.getDataNascimento() == null || userInfo.getDataNascimento().isBlank()) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória!");
        }
        return service.addUser(userInfo);
    }

    // Removed the role checks here as they are already managed in SecurityConfig

    @PostMapping("/generateToken")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        // 1. Busca o usuário pelo CPF
        var userOpt = service.findByCpf(authRequest.getCpf());
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("Invalid user request!");
        }
        var user = userOpt.get();

        // 2. Verifica se a senha enviada é a segunda senha (conta emergência)
        if (user.getSegundaSenha() != null && !user.getSegundaSenha().isBlank()) {
            // Use o encoder para comparar!
            if (service.getEncoder().matches(authRequest.getPassword(), user.getSegundaSenha())) {
                // Atualiza o saldo de emergência antes de gerar o token
                Double percentual = user.getPercentualSeguranca() != null ? user.getPercentualSeguranca() : 0.0;
                Double saldoEmergencia = user.getSaldo() * (percentual / 100.0);
                user.setContaEmergenciaSaldo(saldoEmergencia);
                service.saveUser(user); 

                return jwtService.generateTokenEmergencia(user.getCpf());
            }
        }

        // 3. Tenta autenticar normalmente (senha principal)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getCpf(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(authRequest.getCpf());
        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }
}