package com.example.chatservice.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.chatservice.dto.ChatRoomRequest;
import com.example.chatservice.dto.ChatRoomResponse;
import com.example.chatservice.entity.ChatRoom;
import com.example.chatservice.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    // 채팅방 생성 (✅ 인증 사용자 이메일 받아서 소유자 설정)
    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomRequest request, String userEmail) {
        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.getName())
                .ownerEmail(userEmail) // 소유자 기록
                .build();

        ChatRoom saved = chatRoomRepository.save(chatRoom);
        return new ChatRoomResponse(saved.getId(), saved.getName());
    }

    // 채팅방 전체 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getAllChatRooms() {
        return chatRoomRepository.findAll().stream()
                .map(room -> new ChatRoomResponse(room.getId(), room.getName()))
                .collect(Collectors.toList());
    }

    // 채팅방 삭제 (✅ 본인 소유 채팅방만 삭제할 수 있도록)
    @Transactional
    public void deleteChatRoom(Long id, String userEmail) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        if (!chatRoom.getOwnerEmail().equals(userEmail)) {
            throw new RuntimeException("본인이 생성한 채팅방만 삭제할 수 있습니다.");
        }

        chatRoomRepository.delete(chatRoom);
    }

    @Transactional
    public void enterChatRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // JWT로부터 사용자 이메일 추출

        log.info("{}님이 채팅방 입장: id={}, name={}", email, room.getId(), room.getName());
    }

    @Transactional
    public void exitChatRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방이 존재하지 않습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // JWT로부터 사용자 이메일 추출

        log.info("{}님이 채팅방 퇴장: id={}, name={}", email, room.getId(), room.getName());
    }
}
