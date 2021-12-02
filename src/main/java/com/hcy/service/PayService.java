package com.hcy.service;

import com.hcy.dto.PayVo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Description：
 * Author: 黄成勇
 * Date:  2021/11/13 4:29
 */
public interface PayService {
    byte[] payImg(PayVo payVo);  // 获取字节数组，前端直接以 img的src获取并展示

    void actionAfterPaySuccess(HttpServletRequest request);
}
