package com.hcy.dto;

import lombok.Data;

/**
 * Description：
 * Author: 黄成勇
 * Date:  2021/11/13 4:44
 */
// 跟订单相关的一些信息
@Data
public class PayVo {
    private String userId;
    private String money;
    private String title;  // 不可使用特殊字符，如 /，=，& 等
    private String orderId;
    private String sellerId;  // 收款者支付宝账号对应的支付宝唯一用户号。以2088开头的纯16位数字
    private String username;
}
