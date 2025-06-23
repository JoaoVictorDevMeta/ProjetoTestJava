package com.projetopix.exemplo.repository;

import com.projetopix.exemplo.entity.UserInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    Optional<UserInfo> findByEmail(String email); 
    Optional<UserInfo> findByCpf(String cpf);
}