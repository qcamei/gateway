package com.aiyolo.channel.data.request;

import java.util.HashMap;
import java.util.Map;

import com.aiyolo.constant.ChannelConsts;
import com.aiyolo.constant.ProtocolCodeConsts;
import com.aiyolo.constant.PushConsts;

import static com.aiyolo.constant.ProtocolFieldConsts.CACHE_TIME;
import static com.aiyolo.constant.ProtocolFieldConsts.CODE;
import static com.aiyolo.constant.ProtocolFieldConsts.IS_TEXT;

public class AppMessagePushRequest extends AppRequest {

    private static AppMessagePushRequest instance;

    public static AppMessagePushRequest getInstance() {
        if (instance == null) {
            instance = new AppMessagePushRequest();
        }
        return instance;
    }

    @Override
    public Map<String, Object> requestHeader(String[] mobileIds) {
        Map<String, Object> headerMap = super.requestHeader(mobileIds);
        if (headerMap != null) {
            headerMap.put(CACHE_TIME, PushConsts.CACHE_TIME);
            headerMap.put(IS_TEXT, 1);
        }
        return headerMap;
    }

    @Override
    public Map<String, Object> requestBody(Map<String, Object> data) {
        try {
            Map<String, Object> bodyMap = new HashMap<String, Object>();

//            bodyMap.put("gl_id", data.get("glId"));
            bodyMap.put("title", data.get("title"));
            bodyMap.put("text", data.get("text"));

            return bodyMap;
        } catch (Exception e) {
            errorLogger.error("AppMessagePushRequestBody异常！", e);
        }
        return null;
    }

}
