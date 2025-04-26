package com.example.chatservice.kafka;

import com.example.chatservice.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.example.chatservice.config.KafkaConfig.CHAT_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ JSON 변환기 추가

    public void sendMessage(ChatMessage chatMessage) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessage);
            log.info("Kafka 메시지 발행: {}", jsonMessage);
            kafkaTemplate.send(CHAT_TOPIC, jsonMessage);
        } catch (Exception e) {
            log.error("Kafka 메시지 직렬화 실패", e);
        }
    }
}
