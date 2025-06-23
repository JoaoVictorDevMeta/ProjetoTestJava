package com.projetopix.exemplo.repository;

import com.projetopix.exemplo.entity.Notification;
import com.projetopix.exemplo.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsuario(UserInfo usuario);
}