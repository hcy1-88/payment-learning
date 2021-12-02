package com.hcy.controller;

import com.alibaba.fastjson.JSONObject;
import com.hcy.config.AlipayConfiguration;
import com.hcy.dto.PayVo;
import com.hcy.service.PayService;
import com.hcy.service.impl.AlipayService;
import com.hcy.util.AlipayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Description：
 * Author: 黄成勇
 * Date:  2021/11/13 20:44
 */
@Controller
@RequestMapping("/alipay")
public class AlipayController {
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private AlipayConfiguration alipayConfiguration;


    @RequestMapping("/code")
    @ResponseBody
    public byte[] getCode(PayVo payVo) {
        return alipayService.payImg(payVo);
    }

    @GetMapping("/toPayPage")
    public String toPayPage(){
        return "pay";
    }

    @RequestMapping("/notifyUrl")
    @ResponseBody
    public String notifyUrl(HttpServletRequest request) {
        System.out.println("----------进入notify url");
        // 对于异步回调，要返回 success 或 false，不然支付宝不知道你是否收到异步通知，会重复给你发异步通知
        if (alipayCallbackNotify(request)) {
            alipayService.actionAfterPaySuccess(request);
            return "success";
        }
        return "false";
    }


    @RequestMapping("/returnUrl")
    public String returnUrl(HttpServletRequest request) {
        System.out.println("----------进入return url");
        if (alipayCallbackReturn(request)) {
            return "paySuccess";
        }
        return "payFail";
    }

    @RequestMapping("/payOk")
    public String payOk() {
        return "paySuccess";
    }

    @RequestMapping("/payFailure")
    public String payFail() {
        return "payFail";
    }
    private boolean alipayCallbackReturn(HttpServletRequest request){
        Map<String, String> params = AlipayUtil.getParams(request);
        System.out.println("------");
        System.out.println(params);
        System.out.println("------");
        // 验证签名，是不是支付宝发来的通知？
        String verify = AlipayUtil.verify(params, this.alipayConfiguration);
        if (!"验证通过".equals(verify)){
            return false;
        }
        // 验证通过，则检查 支付状态、支付信息是否对的上 等
        return true;
    }

    // 校验
    private boolean alipayCallbackNotify(HttpServletRequest request){
        Map<String, String> params = AlipayUtil.getParams(request);
        String body = params.get("body");  // 就是获取二维码时的公共参数，原样返回给了我们
        JSONObject bodyObj = JSONObject.parseObject(body);
        String orderId = bodyObj.getString("orderId");

        System.out.println("订单id："+orderId);
        // 这里可以做一下校验，比如 查看 订单id 是否存在于数据库订单表
        if (StringUtils.hasText(orderId)){
            return true;
        }
        return false;
    }


    
}
