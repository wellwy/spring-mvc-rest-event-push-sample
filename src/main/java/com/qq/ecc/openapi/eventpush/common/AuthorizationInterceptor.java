package com.qq.ecc.openapi.eventpush.common;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.qq.ecc.openapi.eventpush.exception.AuthorizationException;
import com.qq.ecc.openapi.eventpush.util.SignUtils;

/**
 * 鉴权拦截器
 */
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = Logger.getLogger(AuthorizationInterceptor.class);

    @Value("${secretKey}")
    private String              secretKey;

    // before the actual handler will be executed
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // GET请求不需要签名验证。注意，这里注册callbackUrl的时候系统会发送一个GET请求，需要验证签名返回echostr。
        if (request.getMethod().equalsIgnoreCase("GET") && request.getParameter("echostr") == null) {
            return true;
        } else { // POST or GET with echostr
            String signature = request.getParameter("signature");
            Assert.isTrue(signature != null, "signature is required!");
            String nonce = request.getParameter("nonce");
            Assert.isTrue(nonce != null, "nonce is required!");
            String timestamp = request.getParameter("timestamp");
            Assert.isTrue(timestamp != null, "timestamp is required!");

            logger.debug("Receive sign:" + signature);

            List<String> paramsToSign = new ArrayList<String>();
            paramsToSign.add(nonce);
            paramsToSign.add(timestamp);
            paramsToSign.add(secretKey);

            String sign = SignUtils.makeSign(paramsToSign);

            if (sign == null || !sign.equals(signature)) {
                logger.error("参数 sign错误，请检查是否sign的计算算法有误，或者鉴权参数设置有误");
                throw new AuthorizationException(
                                                 "Signature verify failed! Please check your signature calculation algorithm and authentication parameters!");
            }
            return true;
        }
    }
}
