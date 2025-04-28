package com.example.chatservice.controller;

import com.example.chatservice.dto.ChatMessage;
import com.example.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ✅ 클라이언트가 /app/chat/message로 STOMP publish 하면 이 메서드가 호출된다
    @MessageMapping("/chat/message")
    public void sendChatMessage(ChatMessage message) {
        chatService.sendMessage(message);
    }

    // ✅ 클라이언트가 /app/chat/enter로 STOMP publish 하면 이 메서드가 호출된다
    @MessageMapping("/chat/enter")
    public void sendEnterMessage(ChatMessage message) {
        chatService.sendMessage(message);
    }

    // ✅ 클라이언트가 /app/chat/leave로 STOMP publish 하면 이 메서드가 호출된다
    @MessageMapping("/chat/leave")
    public void sendLeaveMessage(ChatMessage message) {
        chatService.sendMessage(message);
    }
}
