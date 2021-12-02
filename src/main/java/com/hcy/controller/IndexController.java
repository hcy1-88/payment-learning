package com.hcy.controller;

import com.hcy.config.AlipayConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Description：
 * Author: 黄成勇
 * Date:  2021/11/13 3:05
 */
@Controller
public class IndexController {

    @RequestMapping("/index")
    public String toIndex() {
        return "index";
    }
}
