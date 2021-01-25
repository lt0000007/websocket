package com.example.websocket.controller;


import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;


@Controller
public class SocketController {



    @GetMapping("/index")
    public ModelAndView socket(HttpServletRequest request) {
        request.getSession();
        ModelAndView mav=new ModelAndView("index");

        return mav;
    }

    @GetMapping("/socket")
    public ModelAndView getSocket(HttpServletRequest request) {
        request.getSession();
        ModelAndView mav=new ModelAndView("socket");

        return mav;
    }

    @GetMapping("/message")
    public ModelAndView getMessageHtml() {

        ModelAndView mav=new ModelAndView("message");

        return mav;
    }

    @GetMapping("/room_List")
    public ModelAndView getRoomListHtml() {

        ModelAndView mav=new ModelAndView("room_List");

        return mav;
    }


}