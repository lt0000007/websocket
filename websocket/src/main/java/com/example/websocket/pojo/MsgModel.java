package com.example.websocket.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;

import javax.websocket.Session;
import java.util.*;

/**
 * @author lantian
 * 消息类
 */
public class MsgModel {


    private String state;
    private String nick;


    private String msg;
    private String sendTime;
    private int roomCode;
    private String myCommunication;
    private String communicationObject;
    private boolean isSelf;
    /**
     * 在线列表
     */

    private Map<String,MsgModel> onlineMap;

    /**
     * 在线群组
     */
    private List<Integer> onLineList;


    public Map<String, MsgModel> getOnlineMap() {
        return onlineMap;
    }

    public void setOnlineMap(Map<String, MsgModel> onlineMap) {
        this.onlineMap = onlineMap;
    }

    public List<Integer> getOnLineList() {
        return onLineList;
    }

    public void setOnLineList(List<Integer> onLineList) {
        this.onLineList = onLineList;
    }

    @JsonIgnore
    private Session session;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public int getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(int roomCode) {
        this.roomCode = roomCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public String getCommunicationObject() {
        return communicationObject;
    }

    public void setCommunicationObject(String communicationObject) {
        this.communicationObject = communicationObject;
    }

    public String getMyCommunication() {
        return myCommunication;
    }

    public void setMyCommunication(String myCommunication) {
        this.myCommunication = myCommunication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MsgModel)) return false;
        MsgModel msgModel = (MsgModel) o;
        return roomCode == msgModel.roomCode &&
                isSelf == msgModel.isSelf &&
                Objects.equals(state, msgModel.state) &&
                Objects.equals(nick, msgModel.nick) &&
                Objects.equals(msg, msgModel.msg) &&
                Objects.equals(sendTime, msgModel.sendTime) &&
                Objects.equals(myCommunication, msgModel.myCommunication) &&
                Objects.equals(communicationObject, msgModel.communicationObject) &&
                Objects.equals(onlineMap, msgModel.onlineMap) &&
                Objects.equals(onLineList, msgModel.onLineList) &&
                Objects.equals(session, msgModel.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, nick, msg, sendTime, roomCode, myCommunication, communicationObject, isSelf, onlineMap, onLineList, session);
    }
}
