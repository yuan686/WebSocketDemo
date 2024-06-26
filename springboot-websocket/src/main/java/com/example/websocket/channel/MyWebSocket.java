package com.example.websocket.channel;

import com.alibaba.fastjson2.JSONObject;
import com.example.websocket.entity.MessageVo;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yuan
 * @description WebSocketServer服务端
 * @date 2024/6/26
 */
@ServerEndpoint(value = "/websocket/{uid}") //接受websocket请求路径
@Component  //注册到spring容器中
public class MyWebSocket {


    /**
     * 存放所有在线的客户端
     */
    private static Map<String, Session> onlineSessionClientMap = new ConcurrentHashMap<>();


    //记录当前在线数目
    private static AtomicInteger onlineSessionClientCount = new AtomicInteger(0);

    //当前连接（每个websocket连入都会创建一个MyWebSocket实例
    private Session session;
    private String uid;

    private Logger log = LoggerFactory.getLogger(this.getClass());
    //处理连接建立
    @OnOpen
    public void onOpen(@PathParam("uid") String uid, Session session) {
        /**
         * session.getId()：当前session会话会自动生成一个id，从0开始累加的。
         */
        log.info("连接建立中 ==> session_id = {}， sid = {}", session.getId(), uid);
        //加入 Map中。将页面的uid和session绑定或者session.getId()与session
        onlineSessionClientMap.put(uid, session);
        onlineSessionClientCount.incrementAndGet();
        this.uid = uid;
        this.session = session;
        log.info("连接建立成功，当前在线数为：{} ==> 开始监听新连接：session_id = {}， sid = {},。", onlineSessionClientCount, session.getId(), uid);
    }

    //接受消息
    @OnMessage
    public void onMessage(String message,Session session){
        log.info("收到客户端{}消息：{}",session.getId(),message);
        log.info("接收到消息-----------------"+message);
        //A发送消息给B，服务端收到A的消息后，从A的消息体中拿到B的uid及携带的手机号。查找B是否在线，如果B在线，则使用B的session发消息给B自己
        MessageVo msgObj = JSONObject.parseObject(message, MessageVo.class);
        String fromUid = msgObj.getFromUid();
        String toUid = msgObj.getToUid();
        String msg = msgObj.getMessage();
        //A给B发送消息，A要知道B的信息，发送消息的时候把B的信息携带过来
        log.info("服务端收到客户端消息 ==> fromSid = {}, toSid = {}, message = {}", uid, toUid, msg);
        //向客户端发送消息内容
        sendToOne(toUid, message);
    }

    //处理连接关闭
    @OnClose
    public void onClose(){
        // 从 Map中移除
        onlineSessionClientMap.remove(uid);
        //在线数减1
        onlineSessionClientCount.decrementAndGet();
        log.info("连接关闭成功，当前在线数为：{} ==> 关闭该连接信息：session_id = {}， sid = {},。", onlineSessionClientCount, session.getId(), uid);
    }

    //群发消息

    //发送消息
    public static void sendMessage(Session s, String message) throws IOException {
        s.getBasicRemote().sendText(message);
    }

    //广播消息
    public static void broadcast(){
        MyWebSocket.onlineSessionClientMap.forEach((k,v)->{
            try{
                sendMessage(v,JSONObject.toJSONString("通知，小元又新增了入库管理Demo笔试题了"));
            }catch (Exception e){
            }
        });
    }

    /**
     * 指定发送消息
     *
     * @param toUid
     * @param message
     */
    private void sendToOne(String toUid, String message) {
        /*
         * 判断发送者是否在线
         */
        Session toSession = onlineSessionClientMap.get(toUid);
        if (toSession == null) {
            log.error("服务端给客户端发送消息 ==> toSid = {} 不存在, message = {}", toUid, message);
            return;
        }
        // 异步发送
        log.info("服务端给客户端发送消息 ==> toSid = {}, message = {}", toUid, message);
        toSession.getAsyncRemote().sendText(message);
    }

    /**
     * 发生错误调用的方法
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，错误信息为：" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 群发消息
     *
     * @param message 消息
     */
    private void sendToAll(String message) {
        // 遍历在线map集合
        onlineSessionClientMap.forEach((onlineSid, toSession) -> {
            // 排除掉自己
            if (!uid.equalsIgnoreCase(onlineSid)) {
                log.info("服务端给客户端群发消息 ==> sid = {}, toSid = {}, message = {}", uid, onlineSid, message);
                toSession.getAsyncRemote().sendText(message);
            }
        });
    }
}
