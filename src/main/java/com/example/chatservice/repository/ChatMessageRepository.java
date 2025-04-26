package com.example.chatservice.repository;

import com.example.chatservice.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // ✅ roomId로 메시지 리스트 조회 (시간순 정렬)
    List<ChatMessageEntity> findByRoomIdOrderByTimestampAsc(Long roomId);
}
