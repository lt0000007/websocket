package com.example.websocket.pojo;

/**
 * @author lantian
 */

public enum Action {
    //连接
    JOIN,
    //发送到房间
    SEND_TO_ROOM,
    //发送到指定对象
    SEND_TO_NICK,
    //返回昵称
    RETURN_NAME,
    //返回在线人
    RETURN_ONLINELIST

}
