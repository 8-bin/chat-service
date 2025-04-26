package com.example.chatservice.websocket;

import com.example.chatservice.dto.ChatMessage;
import com.example.chatservice.entity.ChatMessageEntity;
import com.example.chatservice.kafka.ChatConsumer;
import com.example.chatservice.kafka.ChatProducer;
import com.example.chatservice.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatProducer chatProducer;
    private final ChatConsumer chatConsumer;
    private final ChatMessageService chatMessageService; // ✅ 추가

    private final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket 연결 수립: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        log.info("WebSocket 메시지 수신: {}", payload);

        ChatMessage chatMessage = new ObjectMapper().readValue(payload, ChatMessage.class);

        // ✅ 첫 입장 처리
        if (!sessionRoomMap.containsKey(session.getId())) {
            Long roomId = chatMessage.getRoomId();
            chatConsumer.addSession(roomId, session);
            sessionRoomMap.put(session.getId(), roomId);

            // ✅ 과거 메시지 조회 후 전송
            List<ChatMessageEntity> pastMessages = chatMessageService.getMessagesByRoomId(roomId);
            for (ChatMessageEntity pastMessage : pastMessages) {
                ChatMessage oldMessage = new ChatMessage(
                        pastMessage.getRoomId(),
                        pastMessage.getSender(),
                        pastMessage.getContent()
                );
                String json = new ObjectMapper().writeValueAsString(oldMessage);
                session.sendMessage(new TextMessage(json));
            }
        }

        // ✅ 이후에는 실시간 채팅 메시지 Kafka 발행
        chatProducer.sendMessage(chatMessage);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 전송 에러: {}", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info("WebSocket 연결 종료: {}", session.getId());
        Long roomId = sessionRoomMap.remove(session.getId());
        if (roomId != null) {
            chatConsumer.removeSession(roomId, session);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
