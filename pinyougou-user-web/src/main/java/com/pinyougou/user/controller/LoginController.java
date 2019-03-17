package com.pinyougou.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取登录的用户名
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map name() {
        Map<String,String> map = new HashMap();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();//获取登录认得账号
        map.put("loginName", name);
        return map;
    }
}
