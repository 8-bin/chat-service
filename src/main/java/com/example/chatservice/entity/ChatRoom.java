package com.example.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chatrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String ownerEmail; // ✅ 방 소유자 이메일 기록
}
