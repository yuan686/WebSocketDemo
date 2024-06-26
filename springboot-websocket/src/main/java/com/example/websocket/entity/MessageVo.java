package com.example.websocket.entity;

import lombok.Data;

/**
 * @author Yuan
 * @description
 * @date 2024/6/26
 */
@Data
public class MessageVo {
    private String fromUid;
    private String toUid;
    private String message;
}
