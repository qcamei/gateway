package com.aiyolo.channel.data.request;

import com.aiyolo.constant.ChannelConsts;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.aiyolo.constant.ProtocolFieldConsts.IMEI;
import static com.aiyolo.constant.ProtocolFieldConsts.PID;
import static com.aiyolo.constant.ProtocolFieldConsts.PIN;

public class GatewayBroaRequest extends GatewayRequest {

    public static final String ACTION = "broa";

    private static GatewayBroaRequest instance;

    public static GatewayBroaRequest getInstance() {
        if (instance == null) {
            instance = new GatewayBroaRequest();
        }
        return instance;
    }

    @Override
    public Map<String, Object> requestBody(Map<String, Object> data) {
        try {
            Map<String, Object> bodyMap = new LinkedHashMap<>();

            bodyMap.put("act", ACTION);
            bodyMap.put(PID, ChannelConsts.PRODUCT_ID);
            //            bodyMap.put(PID, product_id);
            bodyMap.put(IMEI, data.get(IMEI));
            bodyMap.put(PIN, data.get(PIN));
            bodyMap.put("mid", System.currentTimeMillis() / 1000);
            bodyMap.put("binfo", data.get("binfo"));

            return bodyMap;
        } catch (Exception e) {
            errorLogger.error("GatewayBroaRequest异常！", e);
        }
        return null;
    }

}
