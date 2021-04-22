package com.xsw.controller;

import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.slf4j.Logger;

@Controller
public class SysController {
    private Logger logger = (Logger) LoggerFactory.getLogger(SysController.class);

    @RequestMapping("/")
    public String showHome(){
        Authentication name= SecurityContextHolder.getContext().getAuthentication();
        logger.info("当前登录用户:"+name);

        return "home.html";
    }

    @RequestMapping("/login")
    public String showLogin(){
        return "login.html";
    }

    @RequestMapping("/admin")
    @ResponseBody
    @PreAuthorize("hasRole('Role_ADMIN')")
    public String printAdmin(){
        return "这里代表你有ROLE_ADMIN角色";
    }

    @RequestMapping("/user")
    @ResponseBody
    @PreAuthorize("hasRole('Role_USER')")
    public String printUser(){
        return "这里代表你有ROLE_USER角色";
    }
}
