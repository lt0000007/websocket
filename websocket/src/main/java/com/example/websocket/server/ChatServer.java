package com.example.websocket.server;

import com.example.websocket.pojo.Action;
import com.example.websocket.pojo.MsgModel;
import com.example.websocket.utils.RandomName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.RouteMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天服务器类
 * @author shiyanlou
 *
 */
@ServerEndpoint("/websocket")
@Component
public class ChatServer implements HttpSessionListener {
    /**
     * 存房间号
     */
    private static final Object[] room_codes = new Object[65535];
    /**
     *  日期格式化
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * 指定的sid，具有唯一性，暫定為用戶的sessionId
     */
    private String sid = "";
    private String name  = "";
    private static String oldName = "";
    private static String oldsid = "";

    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的socket对象
     */
    private static ConcurrentHashMap<String, MsgModel> webSocketSet = new ConcurrentHashMap<>();

    private static List<Integer> onlineList = new ArrayList<>();
    /**
     * 保存httpsession
     */
    public static ArrayList<String> sids = new ArrayList<>();


    public static String sessionID = "";



    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("创建session");
        sessionID=se.getSession().getId();
        //设置session永久有效
        se.getSession().setMaxInactiveInterval(1800);
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("销毁session");
        String dSessionID=se.getSession().getId();
        ChatServer.sids.remove(dSessionID);
        for (int i = 0; i < room_codes.length; i++) {
            if (room_codes[i] != null) {
                HashMap<String,MsgModel> map = (HashMap)room_codes[i];
                for (String s : map.keySet()) {
                    if (dSessionID == s) {
                        //httpsession消失,退群
                        map.remove(s);
                    }
                }
                if (map.size() == 0) {
                    //群里没有成员群号消失
                    room_codes[i] = null;
                    for (int j = 0; j < onlineList.size(); j++) {
                        if (j == i) {
                            onlineList.remove(j);
                        }
                    }

                }
            }
        }
    }


    @OnOpen
    public void open(Session session) {
        // 添加初始化操作
        System.out.println(session.getId());

        try {

            String httpSessionId = ChatServer.sessionID;
            System.out.println(oldsid);

            //如果集合中没有httpsId那么就添加一个,如果已经存在,表示该用户连接过
            if(!sids.contains(httpSessionId)) {
                sids.add(httpSessionId);
                sid=httpSessionId;
            }else {
                sid=oldsid;
                name=oldName;
            }

            MsgModel msgModel = new MsgModel();
            msgModel.setSession(session);
            //生成昵称
            if (oldsid == null || StringUtils.isEmpty(oldsid) || sid != oldsid) {
                msgModel.setState(Action.RETURN_NAME.name());
                String randomJianHan = RandomName.getRandomJianHan(3);
                msgModel.setNick(randomJianHan);
                webSocketSet.put(sid,msgModel);
                name = randomJianHan;
            } else {
                msgModel.setSession(session);
                msgModel.setNick(oldName);
                webSocketSet.put(sid,msgModel);
            }

            for (Object o : room_codes) {
                boolean flag = false;
                if (o != null) {
                    HashMap<String, MsgModel> map = (HashMap)o;
                    for (String s : map.keySet()) {
                        if (s == sid) {
                            msgModel.setMsg("加入房间成功");
                            map.put(s,msgModel);
                            session.getAsyncRemote().sendText(new ObjectMapper().writeValueAsString(msgModel));
                            flag = true;
                            break;
                        }
                    }
                }
                if (flag) {
                    break;
                }
            }
            msgModel.setMsg("");
            msgModel.setState(Action.RETURN_NAME.name());
            session.getAsyncRemote().sendText(new ObjectMapper().writeValueAsString(msgModel));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接受客户端的消息，并把消息发送给所有连接的会话
     * @param message 客户端发来的消息
     * @param session 客户端的会话
     */
    @OnMessage
    public void getMessage(String message, Session session) {
        ObjectMapper jsonUtil = new ObjectMapper();
        // 把客户端的消息解析为JSON对象
        System.out.println(message);
        System.out.println(session.getId());

        try {
            //解析成消息模型
            MsgModel messageMap = jsonUtil.readValue(message, MsgModel.class);
            // 在消息中添加发送日期
            messageMap.setSendTime(DATE_FORMAT.format(new Date()));
            if (Action.SEND_TO_ROOM.name().equals(messageMap.getState())) {
                //发送群组消息
                sendRoom(messageMap,session);
            } else if (Action.JOIN.name().equals(messageMap.getState())) {
                //加入房间
                joinRoom(messageMap,session);
            } else if (Action.SEND_TO_NICK.name().equals(messageMap.getState())) {
                //发送给对方
                sendNick(messageMap,session);
            }


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        //用户退出,在线人数删除
        for (String s : webSocketSet.keySet()) {
            MsgModel msgModel = webSocketSet.get(s);
            if (msgModel.getSession().getId().equals(session.getId())) {
                webSocketSet.remove(s);
            }
        }

        for (int i = 0; i < room_codes.length; i++) {
            if (room_codes[i] != null) {
                HashMap<String, MsgModel> map = (HashMap)room_codes[i];
                boolean flag = false;
                for (String s : map.keySet()) {

                    if (map.get(s).getSession().getId() == session.getId()) {
                        MsgModel msgModel = map.get(s);
                        //退出后,该房间号用户连接为null
                        msgModel.setSession(null);
                    }
                    flag = map.get(s).getSession() == null;

                }
                //如果都房间里的session都是空房间删除
                if (flag) {
                    room_codes[i] = null;
                    for (int j = 0; j < onlineList.size(); j++) {
                        if (onlineList.get(j) == i) {
                            onlineList.remove(j);
                        }
                    }
                }
            }
        }

        if(!sid.equals("")) {
            oldsid=sid;
            oldName=name;
        }
    }
    @OnError
    public void error(Throwable t) {
        // 添加处理错误的操作
    }

    /**
     * 加入房间
     * @param messageMap
     * @param session
     */
    public void joinRoom(MsgModel messageMap, Session session) {
        ObjectMapper jsonUtil = new ObjectMapper();
        try {
            //每次进来都更新session
            messageMap.setSession(session);
            //加入房间,房间号为下标
            HashMap map = (HashMap)ChatServer.room_codes[messageMap.getRoomCode()];
            if (map != null) {
                //有该房间号码存在
                MsgModel msgModel = (MsgModel)map.get(this.sid);
                for (Object o : room_codes) {
                    //查看在其他房间号是否有号码有则删除
                    if (o != null) {
                        Map<String, MsgModel> modelMap = (HashMap)o;
                        if (modelMap.get(this.sid) != null) {
                            modelMap.remove(this.sid);
                        }
                    }
                }

                map.put(this.sid,messageMap);
                if (msgModel != null) {
                    messageMap.setMsg("已经加入过该房间");
                    messageMap.getSession().getAsyncRemote().sendText(jsonUtil.writeValueAsString(messageMap));

                } else {
                    messageMap.setMsg("加入房间成功");
                    messageMap.getSession().getAsyncRemote().sendText(jsonUtil.writeValueAsString(messageMap));
                }
                ChatServer.room_codes[messageMap.getRoomCode()] = map;
                return;
            } else {
                //没有号码
                for (Object o : room_codes) {
                    //查看在其他房间号是否有号码有则删除
                    if (o != null) {
                        Map<String, MsgModel> modelMap = (HashMap)o;
                        if (modelMap.get(this.sid) != null) {
                            modelMap.remove(this.sid);
                        }
                    }
                }

                Map<String, MsgModel> newMap = new HashMap<>();
                newMap.put(this.sid,messageMap);
                ChatServer.room_codes[messageMap.getRoomCode()] = newMap;
                messageMap.setMsg("创建房间成功");
                onlineList.add(messageMap.getRoomCode());
                messageMap.getSession().getAsyncRemote().sendText(jsonUtil.writeValueAsString(messageMap));
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送群组消息
     * @param messageMap
     * @param session
     */
    public void sendRoom(MsgModel messageMap, Session session) {
        ObjectMapper jsonUtil = new ObjectMapper();
        try {
            //更新session
            messageMap.setSession(session);
            //修改在线用户名
            MsgModel msgModel1 = webSocketSet.get(sid);
            msgModel1.setNick(messageMap.getNick());
            webSocketSet.put(sid,msgModel1);
            name = messageMap.getNick();
            //加入房间,房间号为下标
            HashMap<String, MsgModel> map = (HashMap)ChatServer.room_codes[messageMap.getRoomCode()];
            if (map != null) {
                //更新session
                map.put(this.sid,messageMap);
                //有该房间号码存在,给房间号所有人发送消息
                for (String s : map.keySet()) {

                    MsgModel msgModel = map.get(s);
                    messageMap.setSelf(session.equals(msgModel.getSession()));
                    Session modelSession = msgModel.getSession();
                    if (modelSession != null) {
                        modelSession.getAsyncRemote().sendText(jsonUtil.writeValueAsString(messageMap));
                    }
                }
                ChatServer.room_codes[messageMap.getRoomCode()] = map;
            } else {
                //创建房间
                joinRoom(messageMap, session);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }


    /**
     * 发送到指定用户
     * @param msgModel
     * @param session
     */
    public void sendNick(MsgModel msgModel, Session session) {
        try {
            //修改在线用户名
            MsgModel msgModel1 = webSocketSet.get(sid);
            msgModel1.setNick(msgModel.getNick());
            webSocketSet.put(sid,msgModel1);
            name = msgModel.getNick();

            MsgModel communicationObject = webSocketSet.get(msgModel.getCommunicationObject());
            msgModel.setMyCommunication(sid);
            msgModel.setState(Action.SEND_TO_NICK.name());
            msgModel.setSelf(true);
            String msg = new ObjectMapper().writeValueAsString(msgModel);
            communicationObject.getSession().getAsyncRemote().sendText(msg);
            session.getAsyncRemote().sendText(msg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 定时给所有用户发送消息10秒
     *
     */
    @Scheduled(fixedRate = 10000)
    public void timingSendOnline() {
        try {
            if (webSocketSet.size() > 0) {
                for (String s : webSocketSet.keySet()) {
                    HashMap<String, Object> stringObjectHashMap = new HashMap<>();
                    stringObjectHashMap.put("onlineRooms",onlineList);
                    stringObjectHashMap.put("onlineUsers",webSocketSet);
                    Session session = webSocketSet.get(s).getSession();
                    session.getAsyncRemote().sendText(new ObjectMapper().writeValueAsString(stringObjectHashMap));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }



}