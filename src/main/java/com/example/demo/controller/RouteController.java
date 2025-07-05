package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RouteController {

    // 匹配除以 /api 开头的所有路径，转发给 index.html
    @RequestMapping(value = {"/{path:[^\\.]*}", "/{path:^(?!api$).*$}/**/{subpath:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
