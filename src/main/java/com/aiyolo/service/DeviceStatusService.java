package com.aiyolo.service;

import com.aiyolo.channel.data.request.AppNoticeDeviceRequest;
import com.aiyolo.constant.AlarmStatusEnum;
import com.aiyolo.constant.AppNoticeTypeConsts;
import com.aiyolo.constant.PushConsts;
import com.aiyolo.entity.Device;
import com.aiyolo.entity.DeviceAlarm;
import com.aiyolo.entity.DeviceStatus;
import com.aiyolo.entity.Gateway;
import com.aiyolo.queue.Sender;
import com.aiyolo.repository.DeviceAlarmRepository;
import com.aiyolo.repository.DeviceRepository;
import com.aiyolo.repository.DeviceStatusRepository;

import com.aiyolo.constant.DeviceTypeConsts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.aiyolo.constant.DeviceOnlineStatusConsts.ONLINE;
import static com.aiyolo.constant.ProtocolFieldConsts.IMEI;
import static com.aiyolo.constant.ProtocolFieldConsts.PID;

@Service
public class DeviceStatusService {

    private static final Log errorLogger = LogFactory.getLog("errorLog");

    @Autowired
    Sender sender;

    @Autowired
    DeviceRepository deviceRepository;
    @Autowired
    DeviceStatusRepository deviceStatusRepository;
    @Autowired
    DeviceAlarmRepository deviceAlarmRepository;

    @Autowired
    GatewayService gatewayService;
    @Autowired
    MessagePushService messagePushService;

    public void pushDeviceStatus(Device device) {
        pushDeviceStatus(device, AppNoticeTypeConsts.MODIFY);
    }

    //    public void pushDeviceStatus(Device device, Integer onlineStatus) {
    //        pushDeviceStatus(device, AppNoticeTypeConsts.MODIFY, onlineStatus);
    //    }

    public void pushDeviceStatus(Device device, Integer noticeType) {
        if (device == null) {
            return;
        }

        try {
            String[] mobileIds = gatewayService.getGatewayUserMobileIds(device.getGlImei());
            if (mobileIds != null && mobileIds.length > 0) {
                // 推送给APP
                Map<String, Object> headerMap = AppNoticeDeviceRequest.getInstance().requestHeader(mobileIds);

                Map<String, Object> queryParamMap = new HashMap<String, Object>();
                queryParamMap.put("imeiGateway", device.getGlImei());
                queryParamMap.put(IMEI, device.getImei());
                queryParamMap.put("notice", noticeType);
                queryParamMap.put("dev", device.getType());
                queryParamMap.put(PID, device.getPid());

                DeviceStatus deviceStatus = deviceStatusRepository.findFirstByImeiOrderByIdDesc(device.getImei());
                if (deviceStatus != null) {
                    queryParamMap.put("online", deviceStatus.getOnline());
                    queryParamMap.put("position", device.getPosition());
                    queryParamMap.put("name", device.getName());
                    queryParamMap.put("rssi", deviceStatus.getRssi());
                    queryParamMap.put("bat", deviceStatus.getBat());
                    queryParamMap.put("check", deviceStatus.getChecked());
                }
                //                if (onlineStatus != null) {
                //                    queryParamMap.put("online", onlineStatus);
                //                }

                DeviceAlarm deviceAlarm = deviceAlarmRepository.findFirstByImeiOrderByIdDesc(device.getImei());
                if (deviceAlarm != null) {
                    queryParamMap.put("val", AlarmStatusEnum.CLEAR.getValue().equals(deviceAlarm.getStatus()) ? AlarmStatusEnum.CLEAR.getValue() : deviceAlarm.getValue());
                }

                Map<String, Object> bodyMap = AppNoticeDeviceRequest.getInstance().requestBody(queryParamMap);

                sender.sendMessage(headerMap, bodyMap);
            }
        } catch (Exception e) {
            errorLogger.error("pushDeviceStatus异常！device:" + device.toString(), e);
        }
    }
    public void pushDeviceStatusDel(String glImei, String imei) {


//        try {
            String[] mobileIds = gatewayService.getGatewayUserMobileIds(glImei);
            if (mobileIds != null && mobileIds.length > 0) {
                // 推送给APP
                Map<String, Object> headerMap = AppNoticeDeviceRequest.getInstance().requestHeader(mobileIds);

                Map<String, Object> queryParamMap = new HashMap<String, Object>();
                queryParamMap.put("imeiGateway", glImei);
                queryParamMap.put(IMEI, imei);
                queryParamMap.put("notice", 3);


                Map<String, Object> bodyMap = AppNoticeDeviceRequest.getInstance().requestBody(queryParamMap);

                sender.sendMessage(headerMap, bodyMap);
            }
//        } catch (Exception e) {
//            errorLogger.error("pushDeviceStatus异常！device:" + device.toString(), e);
//        }
    }

    public void pushDeviceStatus(String imei) {
        Device device = deviceRepository.findFirstByImeiOrderByIdDesc(imei);
        if (device == null) {
            return;
        }

        pushDeviceStatus(device);
    }

    //    public void pushDeviceStatus(String imei, Integer onlineStatus) {
    //        Device device = deviceRepository.findFirstByImeiOrderByIdDesc(imei);
    //        if (device == null) {
    //            return;
    //        }
    //
    //        pushDeviceStatus(device, onlineStatus);
    //    }


    public void pushChecked(String imei, int mid) {


        Device device = deviceRepository.findFirstByImeiOrderByIdDesc(imei);
        if (device == null) {
            return;
        }
        Gateway gateway = device.getGateway();
        if (gateway == null) {
            return;
        }
        String[] mobileIds = gatewayService.getGatewayUserMobileIds(device.getGlImei());


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 推送给个推
        messagePushService.pushMessage(mobileIds
                , "巡检通知"
                , "智能网关" + gateway.getGlName() + "下的" + deviceTypeToName(device.getType())
                        + "在" + format.format(mid * 1000L) + "完成巡检");


    }


    public void forceOnline(DeviceStatusRepository deviceStatusRepository, String imei, Device device, String glImei, int mid) {
        //强制在线
        DeviceStatus deviceStatus = deviceStatusRepository.findFirstByImeiOrderByIdDesc(imei);
        if (deviceStatus == null) {
            deviceStatus = new DeviceStatus(device.getType(), imei, glImei, mid, ONLINE
                    , 0, 4, 0x64, "", 0);
        }
        deviceStatus.setOnline(ONLINE);
        deviceStatusRepository.save(deviceStatus);
    }

    public static String deviceTypeToName(String type) {
        String name = "";
        switch (type) {
            case DeviceTypeConsts.TYPE_CO_CH4_SWITCH_BAT:
            case DeviceTypeConsts.TYPE_CO_CH4_SWITCH:
                name = DeviceTypeConsts.NAME_CO_CH4;
                break;
            case DeviceTypeConsts.TYPE_CO_SWITCH_BAT:
            case DeviceTypeConsts.TYPE_CO_SWITCH:
                name = DeviceTypeConsts.NAME_CO;
                break;
            case DeviceTypeConsts.TYPE_CH4:
            case DeviceTypeConsts.TYPE_CH4_OLD:
            case DeviceTypeConsts.TYPE_CH4_VALUE:
            case DeviceTypeConsts.TYPE_CH4_SWITCH:
            case DeviceTypeConsts.TYPE_CH4_SWITCH_BAT:
                name = DeviceTypeConsts.NAME_CH4;
                break;
            case DeviceTypeConsts.TYPE_SMOKE:
            case DeviceTypeConsts.TYPE_SMOKE_OLD:
                name = DeviceTypeConsts.NAME_SMOKE;
                break;
            case DeviceTypeConsts.TYPE_SOS:
            case DeviceTypeConsts.TYPE_SOS_OLD:
                name = DeviceTypeConsts.NAME_SOS;
                break;
            case DeviceTypeConsts.TYPE_VALVE:
                name = DeviceTypeConsts.NAME_VALVE;
                break;
            case DeviceTypeConsts.TYPE_ELECTRIC_METER:
                name = DeviceTypeConsts.NAME_ELECTRIC_METER;
                break;
        }
        return name;
    }
}
