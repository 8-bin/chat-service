package com.example.chatservice.controller;

import com.example.chatservice.entity.ChatMessageEntity;
import com.example.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // ✅ 채팅방별 채팅 내역 조회
    @GetMapping("/{roomId}/messages")
    public List<ChatMessageEntity> getChatMessages(@PathVariable Long roomId) {
        return chatMessageService.getMessagesByRoomId(roomId);
    }
}
