package com.hcy.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.hcy.config.AlipayConfiguration;
import com.hcy.config.DefaultAlipayClientFactory;
import com.hcy.config.SocketPool;
import com.hcy.dto.PayVo;
import com.hcy.handler.SocketHandler;
import com.hcy.service.PayService;
import com.hcy.util.AlipayUtil;
import com.hcy.util.QRCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Description：
 * Author: 黄成勇
 * Date:  2021/11/13 4:29
 */
@Service
public class AlipayService implements PayService {
    @Autowired
    private AlipayConfiguration alipayConfiguration;
    @Autowired DefaultAlipayClientFactory defaultAlipayClientFactory;

    @Override
    public byte[] payImg(PayVo payVo)  {
        AlipayTradePrecreateModel model = genModel(payVo);
        try {
            // 拿到 二维码 以字节数组形式
            String qrCode = getQrCode(model);
            String logoPath = ResourceUtils.getFile("classpath:favicon.ico").getAbsolutePath();
            BufferedImage bufferedImage = QRCodeUtil.encode(qrCode, logoPath, false);
            // 转字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            ImageIO.write(bufferedImage,"JPEG",imageOutputStream);
            imageOutputStream.close();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return FileCopyUtils.copyToByteArray(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void actionAfterPaySuccess(HttpServletRequest request) {
        // 当校验成功，则执行后续操作
        // 1，通过 websocket 通知前端支付成功，前端进行页面跳转；或者前端自己发送定时任务判断订单是否支付成功，都可以
        // 2，执行后续业务逻辑，比如更新订单状态等
        Map<String, String> params = AlipayUtil.getParams(request);
        System.out.println("------以下是notify后续操作：");
        System.out.println(params);
        System.out.println("------");

        String trade_no = params.get("trade_no");  // 支付宝流水号，用户支付成功后会有，支付失败则不会触发notify
        String body = params.get("body");  // 就是获取二维码时的公共参数，原样返回给了我们

        // notify 中做一些后置处理，比如：修改订单状态为 已经支付、回填支付宝交易流水号 等信息

        JSONObject bodyObj = JSONObject.parseObject(body);
        String userId = bodyObj.getString("userId");
        String money = bodyObj.getString("money");
        String sellerId = bodyObj.getString("sellerId");
        String orderId = bodyObj.getString("orderId");
        String username = bodyObj.getString("username");
        // 通过websocket向前端发送消息
        Session session = SocketPool.sessionMap().get(username);
        SocketHandler.sendMessage(session,"PaySuccess");

        System.out.println("支付宝交易号："+trade_no);
        System.out.println("买家id："+userId);
        System.out.println("付款金额："+money);
        System.out.println("商家id："+sellerId);
        System.out.println("订单id："+orderId);
        // 更新订单状态为已经支付、回填支付宝交易号
    }

    private String getQrCode(AlipayTradePrecreateModel model){
        // 请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        // 客户端
        AlipayClient alipayClient = defaultAlipayClientFactory.getAlipayClient();

        request.setBizModel(model);
        request.setNotifyUrl(alipayConfiguration.getNotifyUrl());
        request.setReturnUrl(alipayConfiguration.getReturnUrl());

        AlipayTradePrecreateResponse alipayTradePrecreateResponse = null;
        try {
            alipayTradePrecreateResponse = alipayClient.execute(request);
        }catch (AlipayApiException e){
            e.printStackTrace();
        }
        if (!alipayTradePrecreateResponse.isSuccess()) {
            System.out.println("请求失败！");
        }

        String outTradeNo = alipayTradePrecreateResponse.getOutTradeNo(); // 商户订单号
        System.out.println("AlipayService 81 -> outTradeNo:"+outTradeNo);
        String qrCode = alipayTradePrecreateResponse.getQrCode(); // 二维码字符串，二维码就是由字符串生成的
        System.out.println("AlipayService 83 -> qrCode is :"+qrCode);
        return qrCode;
    }

    private AlipayTradePrecreateModel genModel(PayVo payVo){
        String userId = payVo.getUserId(); // 用户（买家）的id
        String money = payVo.getMoney();  // 支付金额
        String title = payVo.getTitle();  // 买的商品名称
        String orderId = payVo.getOrderId(); // 订单编号
        String sellerId = payVo.getSellerId();
        String username = payVo.getUsername();

        // 构造公共 json 参数
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId",userId);
        jsonObject.put("money",money);
        jsonObject.put("sellerId",sellerId);
        jsonObject.put("orderId",orderId);
        jsonObject.put("username",username);
        String params = jsonObject.toJSONString();

        // 设置请求相关的参数
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        model.setOutTradeNo(orderId);  // 商户的订单id
        model.setTotalAmount(money);  // 支付金额
        model.setSubject(title);  // 产品名
        model.setBody(params);  // 公共参数，当支付宝发起回调时，会原样返回
        model.setTimeoutExpress("30m");  // 订单的超时时间，超时则关闭订单
        model.setSellerId(sellerId);
        return model;
    }

}
