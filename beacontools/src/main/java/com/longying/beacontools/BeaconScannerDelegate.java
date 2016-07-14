package com.longying.beacontools;

/**信标扫描代理接口
 * Created by Administrator on 2016/7/13.
 */
public interface BeaconScannerDelegate {

    void didUpdateNearestBeacon(BeaconDeviceBean bdb);
   /* void didUpdateNearestBeaconName(String name);
    void didUpdateNearestBeaconUUID(UUID uuid,Short major,Short minor);
    void didUpdateStatusReportUUID(UUID uuid, Short major, Short minor, String hardwareVersion,
                                            String softwareVersion, int batteryLevel, String mac, Date applyDate);*/

}
