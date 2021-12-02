package com.hcy.util;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.hcy.config.AlipayConfiguration;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Description：
 * Author: 黄成勇
 * Date:  2021/11/14 0:55
 */
public class AlipayUtil {
    /**
      * @Description : 验签，确定是支付宝发来的信息
      * @Param [params]
      * @return java.lang.String
     **/
    public static String verify(Map<String,String> params, AlipayConfiguration alipayConfiguration){
        boolean validation = false;
        String result = null;
        try {
            validation = AlipaySignature.rsaCheckV1(params, alipayConfiguration.getAlipayPublicKey(), alipayConfiguration.getCharset(),
                    alipayConfiguration.getSignType());
            if (validation) {
                result = "验证通过";
            }else{
                result = "验证失败，请确认“支付宝公钥”是否匹配，注意非商户的公钥是支付宝公钥";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            result = "处理出错，请查看代码进行排查";
        }
        return result;
    }

    public static Map<String, String> getParams(HttpServletRequest request){
        Map<String, String> params = new HashMap();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return params;
    }
}
