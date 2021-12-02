package com.hcy.config;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketPool {

    // 在线用户websocket连接池,就是记录有多少个连接、哪些连接
    private static final Map<String, Session> ONLINE_USER_SESSIONS = new ConcurrentHashMap<>();

    /**
     * 新增一则连接
     * @param key 主键
     * @param session 设置session
     */
    public static void add(String key, Session session) {
        if (!key.isEmpty() && session != null){
            ONLINE_USER_SESSIONS.put(key, session); // 添加一个 session
        }
    }

    /**
     * 根据Key删除连接
     * @param key 主键
     */
    public static void remove(String key) {
        if (!key.isEmpty()){
            ONLINE_USER_SESSIONS.remove(key);
        }
    }

    /**
     * 获取在线人数
     * @return 返回在线人数
     */
    public static int count(){
        return ONLINE_USER_SESSIONS.size();
    }

    /**
     * 获取在线session池
     * @return 获取session池
     */
    public static Map<String, Session> sessionMap(){
        return ONLINE_USER_SESSIONS;
    }
}