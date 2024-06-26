package com.example.websocket.controller;

import com.example.websocket.channel.MyWebSocket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Yuan
 * @description
 * @date 2024/6/26
 */
@RestController
public class HomeController {

    @GetMapping("/broadcast")
    public void broadcast(){
        MyWebSocket.broadcast();
    }

}
