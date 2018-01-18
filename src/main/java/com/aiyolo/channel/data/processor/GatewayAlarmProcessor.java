package com.aiyolo.channel.data.processor;

import com.aiyolo.common.SpringUtil;
import com.aiyolo.constant.AlarmStatusEnum;
import com.aiyolo.constant.DeviceOnlineStatusConsts;
import com.aiyolo.entity.Device;
import com.aiyolo.entity.DeviceAlarm;
import com.aiyolo.entity.DeviceAlarmCancel;
import com.aiyolo.repository.DeviceAlarmCancelRepository;
import com.aiyolo.repository.DeviceAlarmRepository;
import com.aiyolo.repository.DeviceRepository;
import com.aiyolo.service.DeviceAlarmService;
import com.aiyolo.service.DeviceStatusService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GatewayAlarmProcessor extends Processor {

    private static Log gatewayLogger = LogFactory.getLog("gatewayLog");

    @Override
    public void run(String message) {
        try {
            init(message);

            DeviceAlarmRepository deviceAlarmRepository = (DeviceAlarmRepository) SpringUtil.getBean("deviceAlarmRepository");
            DeviceAlarmCancelRepository deviceAlarmCancelRepository = (DeviceAlarmCancelRepository) SpringUtil.getBean("deviceAlarmCancelRepository");

            DeviceStatusService deviceStatusService = (DeviceStatusService) SpringUtil.getBean("deviceStatusService");
            DeviceAlarmService deviceAlarmService = (DeviceAlarmService) SpringUtil.getBean("deviceAlarmService");
            DeviceRepository deviceRepository = (DeviceRepository) SpringUtil.getBean("deviceRepository");

            JSONArray deviceAlarms = messageBodyJson.getJSONArray("devs");

            for (int i = 0; i < deviceAlarms.size(); i++) {
                JSONObject alarm = deviceAlarms.getJSONObject(i);
                String imei = alarm.getString("imei");
                Device device = deviceRepository.findFirstByImeiOrderByIdDesc(imei);

                if (device == null){
                    continue;
                }

                DeviceAlarm deviceAlarm = new DeviceAlarm(
                        device.getType(),
                        imei,
                        messageBodyJson.getString("imei"),
                        messageBodyJson.getInt("mid"),
                        alarm.getInt("val"),
                        alarm.getInt("val1"),
                        alarm.getInt("val2"),
                        AlarmStatusEnum.LIFE.getValue());

                if (AlarmStatusEnum.CLEAR.getValue().equals(alarm.getInt("val"))) {
                    // 警报解除记录单独存储
                    deviceAlarmCancelRepository.save(new DeviceAlarmCancel(
                            device.getType(),
                            imei,
                            messageBodyJson.getString("imei"),
                            messageBodyJson.getInt("mid")));

                    // 先查询是否有未解除的报警
                    DeviceAlarm _deviceAlarm = deviceAlarmRepository.findFirstByImeiAndStatusOrderByIdDesc(
                            imei,
                            AlarmStatusEnum.LIFE.getValue());
                    if (_deviceAlarm == null) { // 如果没有未解除的报警，直接返回
                        return;
                    }

                    // 报警解除
                    deviceAlarmRepository.updateStatusByImei(imei, AlarmStatusEnum.CLEAR.getValue());
                } else {
                    // 添加记录
                    deviceAlarmRepository.save(deviceAlarm);
                }

                // 推送设备状态变化
                deviceStatusService.pushDeviceStatus(imei, DeviceOnlineStatusConsts.ONLINE);

                // 推送预报警通知给APP&个推&发送短信
                deviceAlarmService.pushDeviceAlarm(deviceAlarm);

                // 额外处理
                deviceAlarmService.addDispose(deviceAlarm);
            }

            // 写入文件待后续处理
            gatewayLogger.info(message);
        } catch (Exception e) {
            errorLogger.error("GatewayAlarmProcessor异常！message:" + message, e);
        }
    }

}
