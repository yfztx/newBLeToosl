package com.longying.mylibrary;

import java.util.UUID;

/**
 * 信标扫描监听接口
 */
public interface BeaconScannerListener {
    /**
     *以设备名称方式告知已获取到最近信标
     * @param deviceID 设备ID  为BLE 的mac
     * @param name 最近信标的设备名称
     */
    void didUpdateNearestBeaconName(String deviceID, String name);

    /**
     * 以UUID Major Minor方式告知已获取到最近信标
     * @param deviceID 设备ID
     * @param uuid  最近信标UUID
     * @param major 最近信标Major
     * @param minor 最近信标Minor
     */
    void didUpdateNearestBeaconUUID(String deviceID, UUID uuid, short major, short minor);

    /*
     * 更新自巡检状态信息
     * @param deviceID  设备ID
     * @param uuid 信标UUID
     * @param major 信标Major
     * @param minor 信标Minor
     * @param hardwareVersion 信标硬件版本
     * @param softwareVersion 信标软件版本
     * @param batteryLevel 信标电池剩余电量（%）
     * @param mac 信标MAC地址（设备MAC地址，非广播MAC地址）
     * @param applyDate 信标启用日期
     */
    /*void didUpdateStatusReport(String deviceID,UUID uuid, short major, short minor, int hardwareVersion , int softwareVersion , int batteryLevel,
                               String mac, Date applyDate);*/
}
