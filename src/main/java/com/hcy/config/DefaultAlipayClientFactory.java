package com.hcy.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultAlipayClientFactory {

	@Autowired
	private AlipayConfiguration alipayConfiguration;

	private AlipayClient alipayClient = null;

	/**
	 * 封装公共请求参数
	 * 
	 * @return AlipayClient
	 */
	public AlipayClient getAlipayClient() {

		if (this.alipayClient != null) {
				return alipayClient;
			}

		// 网关
		String URL = this.alipayConfiguration.getGatewayUrl();
		// 商户APP_ID
		String APP_ID = this.alipayConfiguration.getAppId();
		// 商户RSA 私钥
		String APP_PRIVATE_KEY = this.alipayConfiguration.getMerchantPrivateKey();
		// 请求方式 json
		String FORMAT = this.alipayConfiguration.getFormat();
		// 编码格式，目前只支持UTF-8
		String CHARSET = this.alipayConfiguration.getCharset();
		// 支付宝公钥
		String ALIPAY_PUBLIC_KEY = this.alipayConfiguration.getAlipayPublicKey();
		// 签名方式
		String SIGN_TYPE = this.alipayConfiguration.getSignType();
		alipayClient = new DefaultAlipayClient(URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
		return alipayClient;
	}
}