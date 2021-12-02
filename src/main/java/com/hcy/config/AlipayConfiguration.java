package com.hcy.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description：
 * Author: 黄成勇
 * Date:  2021/11/13 3:31
 */
@Component
@Data
@ConfigurationProperties(prefix = "pay.alipay")  // 支持松散绑定
public class AlipayConfiguration {
    private String appId;
    private String merchantPrivateKey;  // 商户私钥
    private String alipayPublicKey;  // 支付宝公钥
    private String notifyUrl;
    private String returnUrl;
    private String signType;
    private String charset;
    private String gatewayUrl;
    private String format;

}
