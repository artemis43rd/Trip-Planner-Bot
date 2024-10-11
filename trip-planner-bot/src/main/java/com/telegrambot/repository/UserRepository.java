package com.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.telegrambot.entity.User;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM app_users WHERE telegram_id = :telegramId", nativeQuery = true)
    User findByTelegramId(@Param("telegramId") long telegramId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO app_users (telegram_id, score) VALUES (:telegramId, :score) ON CONFLICT (telegram_id) DO UPDATE SET score = :score RETURNING *", nativeQuery = true)
    User save(@Param("telegramId") long telegramId, @Param("score") int score);
}
