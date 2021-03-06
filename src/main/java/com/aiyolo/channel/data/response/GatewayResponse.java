package com.aiyolo.channel.data.response;

import com.aiyolo.common.ArrayHelper;
import com.aiyolo.constant.ChannelConsts;
import com.aiyolo.constant.ProtocolCodeConsts;
import com.aiyolo.constant.ProtocolFieldConsts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.aiyolo.constant.ProtocolFieldConsts.CACHE_TIME;
import static com.aiyolo.constant.ProtocolFieldConsts.CODE;
import static com.aiyolo.constant.ProtocolFieldConsts.IMEIS;
import static com.aiyolo.constant.ProtocolFieldConsts.PRODUCT_ID;

//@Component
public abstract class GatewayResponse extends Response {

//        @Value("${gelian.product_id}")
//        protected String product_id;

    public Map<String, Object> responseHeader(String imei) {
        String[] imeis = new String[1];
        imeis[0] = imei;
        return responseHeader(imeis);
    }

    @Override
    public Map<String, Object> responseHeader(String[] imeis) {
        try {
            Map<String, Object> headerMap = new LinkedHashMap<>();
            imeis = (String[]) ArrayHelper.removeNullElement(imeis);

            //            Object[] glIdObjects = new Object[imeis.length];
            //            for (int i = 0; i < imeis.length; i++) {
            //                Map<String, String> glIdMap = new HashMap<String, String>();
            //                glIdMap.put(ProtocolFieldConsts.IMEI, imeis[i]);
            //                glIdObjects[i] = glIdMap;
            //            }

            headerMap.put(CODE, ProtocolCodeConsts.SEND_TO_GATEWAY);
            headerMap.put(IMEIS, imeis);
            headerMap.put(CACHE_TIME, 0);
            //            headerMap.put(PRODUCT_ID, product_id);
            headerMap.put(PRODUCT_ID, ChannelConsts.PRODUCT_ID);


            return headerMap;
        } catch (Exception e) {
            errorLogger.error("GatewayResponseHeader异常！", e);
        }
        return null;
    }

}
