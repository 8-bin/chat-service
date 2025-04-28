package com.example.chatservice.controller;

import com.example.chatservice.dto.ChatRoomRequest;
import com.example.chatservice.dto.ChatRoomResponse;
import com.example.chatservice.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomResponse> createChatRoom(@RequestBody ChatRoomRequest request,
                                                           Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(chatRoomService.createChatRoom(request, email));
    }

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getAllChatRooms() {
        return ResponseEntity.ok(chatRoomService.getAllChatRooms());
    }

    @GetMapping("/{id}")
    public ChatRoomResponse getChatRoom(@PathVariable Long id) {
        return chatRoomService.getChatRoom(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long id,
                                               Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        chatRoomService.deleteChatRoom(id, email);
        return ResponseEntity.noContent().build();
    }

    // ✅ 채팅방 입장
    @PostMapping("/{roomId}/enter")
    public void enterChatRoom(@PathVariable Long roomId) {
        chatRoomService.enterChatRoom(roomId);
    }

    // ✅ 채팅방 나가기
    @PostMapping("/{roomId}/exit")
    public void exitChatRoom(@PathVariable Long roomId) {
        chatRoomService.exitChatRoom(roomId);
    }
}
