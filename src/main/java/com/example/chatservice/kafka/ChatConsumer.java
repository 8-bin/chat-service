package com.example.chatservice.kafka;

import com.example.chatservice.dto.ChatMessage;
import com.example.chatservice.entity.ChatMessageEntity;
import com.example.chatservice.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.chatservice.config.KafkaConfig.CHAT_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatMessageRepository chatMessageRepository; // ✅ 추가

    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @KafkaListener(topics = CHAT_TOPIC, groupId = "chat-group")
    public void consume(String message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            Long roomId = chatMessage.getRoomId();
            String sender = chatMessage.getSender();
            String content = chatMessage.getContent();

            log.info("[Kafka 수신] roomId={}, sender={}, content={}", roomId, sender, content);

            // ✅ 현재 roomId에 매핑된 세션 목록 출력
            Set<WebSocketSession> sessions = roomSessions.getOrDefault(roomId, Collections.emptySet());
            log.info("[roomSessions 상태] roomId={}, 세션 수={}", roomId, sessions.size());

            // ✅ DB 저장
            chatMessageRepository.save(
                    ChatMessageEntity.builder()
                            .roomId(roomId)
                            .sender(sender)
                            .content(content)
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build()
            );

            // ✅ WebSocket으로 브로드캐스트
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    log.info("[메시지 전송] 세션 ID={}", session.getId());
                    session.sendMessage(new TextMessage(content));
                } else {
                    log.warn("[닫힌 세션 발견] 세션 ID={}", session.getId());
                }
            }

        } catch (IOException e) {
            log.error("[Kafka consume 에러] 메시지 역직렬화 실패", e);
        }
    }

    // ✅ 세션 등록 (채팅방 입장할 때)
    public void addSession(Long roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    // ✅ 세션 제거 (채팅방 퇴장할 때)
    public void removeSession(Long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

}

