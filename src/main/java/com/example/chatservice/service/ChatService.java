package com.example.chatservice.service;

import com.example.chatservice.dto.ChatMessage;
import com.example.chatservice.kafka.ChatProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatProducer chatProducer;

    public void sendMessage(ChatMessage message) {
        chatProducer.sendMessage(message);
    }
}
