package com.example.chatservice.kafka;

import com.example.chatservice.dto.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendMessage(ChatMessage chatMessage) {
        try {
            String message = objectMapper.writeValueAsString(chatMessage);
            kafkaTemplate.send(CHAT_TOPIC, message);
            log.info("Kafka 전송 완료 - 메시지: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Kafka 전송 실패 - JSON 직렬화 에러", e);
        }
    }
}
