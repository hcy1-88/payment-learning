package com.hcy.component;

import com.hcy.config.SocketPool;
import com.hcy.handler.SocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

@Component
// 表明这是一个websocket服务的端点
@ServerEndpoint("/server/websocket/{name}")
public class SocketEndPoint {

    private static final Logger log = LoggerFactory.getLogger(SocketEndPoint.class);



    // 连接建立
    @OnOpen
    public void onOpen(@PathParam("name") String name, Session session) {
        log.info("有新的连接：{}", session);
        SocketPool.add(SocketHandler.createKey(name), session);  // 添加到map
        log.info("在线人数：{}",SocketPool.count());
        SocketPool.sessionMap().keySet().forEach(item -> log.info("在线用户：" + item));
        for (Map.Entry<String, Session> item : SocketPool.sessionMap().entrySet()) {
            // 每一个用户对应一个{name,session},这里获取name，
            // 你可以在 这里把这些 key 也就是name 组成一个集合再 sendMessage() 给前端用户，使他得知哪些人在线
            log.info("12: {}", item.getKey());
        }
    }

    // 发送消息
    @OnMessage
    public void onMessage(@PathParam("name") String name,String message) {
        // 注意区分单聊和多聊，区分方式就是： 要求前端发送消息，必须在结尾附加一个标记，告知服务器是多聊还是单聊，单聊发给谁
        /*
          * 举例：
          *  message："你好呀，张三---单聊@张三
          *  message："大家好好呀---多聊
          *  通过判断---后面的内容确定是什么聊天方式
         **/
        // 获取最后一个 - 号的后面的字符串，一定是从最后截取，不然会与用户的输入冲突
        String substring = message.substring(message.lastIndexOf("-") + 1);
        String realMsg = message.substring(0,message.lastIndexOf("-") - 2);
        String[] chatWays = substring.split("@");
        if ("单聊".equals(chatWays[0])){
            String acceptUser = chatWays[1]; // 接收者
            SocketPool.sessionMap().forEach((k,v) -> {
                if (acceptUser.equals(k)){
                    SocketHandler.sendMessage(v,realMsg);
                }
            });
        }else{
            // 多聊
            SocketHandler.sendMessageAll(realMsg,name);  // 注意，填 name，也就是不给自己发
        }
        log.info("消息：{}",message);
    }

    @OnClose
    public void onClose(@PathParam("name") String name,Session session) {
        log.info("连接关闭： {}", session);
        SocketPool.remove(SocketHandler.createKey(name));
        log.info("在线人数：{}", SocketPool.count());
        SocketPool.sessionMap().keySet().forEach(item -> log.info("在线用户：" + item));
        for (Map.Entry<String, Session> item : SocketPool.sessionMap().entrySet()){
            log.info("12: {}", item.getKey());
        }
        Date date = new Date();
        DateFormat df = DateFormat.getDateTimeInstance();//可以精确到时分秒
        SocketHandler.sendMessageAll("<div style='width: 100%; float: left;'>[" + df.format(date) + "] " + name + "已离开聊天室</div>", name);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        try {
            session.close();
        } catch (IOException e) {
            log.error("退出发生异常： {}", e.getMessage());
        }
        log.info("连接出现异常： {}", throwable.getMessage());
    }
}