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

    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 채팅방 ID별 WebSocket 세션 목록
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // ✅ Kafka 수신 처리
    @KafkaListener(topics = CHAT_TOPIC, groupId = "chat-group")
    public void consume(String message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);

            // 디비 저장
            chatMessageRepository.save(
                    ChatMessageEntity.builder()
                            .roomId(chatMessage.getRoomId())
                            .sender(chatMessage.getSender())
                            .content(chatMessage.getContent())
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build()
            );

            log.info("Kafka 수신 및 저장 완료 - roomId: {}, sender: {}, content: {}",
                    chatMessage.getRoomId(), chatMessage.getSender(), chatMessage.getContent());

            // WebSocket 브로드캐스트
            broadcast(chatMessage);
        } catch (IOException e) {
            log.error("Kafka 메시지 역직렬화 실패", e);
        }
    }

    // ✅ 세션 등록
    public void addSession(Long roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    // ✅ 세션 제거
    public void removeSession(Long roomId, WebSocketSession session) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

    // ✅ 메시지 브로드캐스트
    private void broadcast(ChatMessage chatMessage) throws IOException {
        Set<WebSocketSession> sessions = roomSessions.getOrDefault(chatMessage.getRoomId(), Collections.emptySet());
        String payload = objectMapper.writeValueAsString(chatMessage);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        }
    }
}
