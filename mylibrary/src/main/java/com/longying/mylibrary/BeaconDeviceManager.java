package com.longying.mylibrary;

import android.app.Activity;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 *  信标设备管理类
 * Created by Administrator on 2016/7/13.
 */
public class BeaconDeviceManager {
    private static BeaconDeviceManager mBeaconManager;
    private static final String TAG = "BeaconDeviceListAdapter";
    private ArrayList<BeaconDeviceBean> beaconDevices;
    private HashMap beaconDeviceIndexes;
    private LayoutInflater mInflater;
    private Activity mContext;
    //private BeaconDeviceRssiComparator rssiComparator;
    private Timer mTimer;
    private TimerTask mSortTask;
    private ScannerResultNameListener scannerName;
    private ScannerResultUUIDListener scannerUUID;

   /* public enum SortType { //枚举 排序
        BEACON_DEVICE_SORT_NONE,
        BEACON_DEVICE_SORT_BY_RSSI,
        BEACON_DEVICE_SORT_BY_MAJOR_MINOR,
    }
*/

    private interface ScannerResultUUIDListener{
        void didUpdateNearestBeaconUUID(UUID uuid, Short major, Short minor);
    }

    private interface ScannerResultNameListener{
        void didUpdateNearestBeaconName(String name);
    }


    public void setBeaconScannerResultCallback(ScannerResultUUIDListener sul){
        this.scannerUUID = sul;
    }
    public void setBeaconScannerResultCallback(ScannerResultNameListener bsd){
        this.scannerName = bsd;
    }

    private BeaconDeviceManager (){
        beaconDevices = new ArrayList<BeaconDeviceBean>();
        beaconDeviceIndexes = new HashMap();

    }



    public static synchronized BeaconDeviceManager getInstance(){
        if (mBeaconManager == null){
            mBeaconManager = new BeaconDeviceManager();
        }
        return mBeaconManager;
    }

    /**
     * 把扫描到的信标添加到集合
     * @param device
     */
    public void addDevice(BeaconDeviceBean device) {
        if (!beaconDevices.contains(device)) {
            //Log.i(TAG, "addDEvice:  num ==" + device);
            beaconDevices.add(device);
            refreshDeviceHashMap();
        }
    }


    /**
     * 根据集合脚标获取信标
     * @param
     * @return
     */
    public BeaconDeviceBean getDevice() {
        if (beaconDevices.size() !=0) return beaconDevices.get(0);
        return null;
    }

    /**
     *  根据MAC 获取信标
     * @param mac
     * @return
     */
    public BeaconDeviceBean getDevice(String mac) {
        if (beaconDeviceIndexes.containsKey(mac)) {
            return beaconDevices.get((int) beaconDeviceIndexes.get(mac));
        }
        return null;
    }

    /**
     * 移出信标
     * @param device
     */
    public void removeDevice(BeaconDeviceBean device) {
        if (beaconDevices.contains(device)) {
            beaconDevices.remove(device);
            refreshDeviceHashMap();
        }
    }

    private void refreshDeviceHashMap() {
        beaconDeviceIndexes.clear();
        for (BeaconDeviceBean device : beaconDevices) {
            beaconDeviceIndexes.put(device.getMac(), beaconDevices.indexOf(device));
        }
    }

    /*private class BeaconDeviceRssiComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            BeaconDeviceBean ldev = (BeaconDeviceBean) lhs;
            BeaconDeviceBean rdev = (BeaconDeviceBean) rhs;
            return rdev.rssi - ldev.rssi;
        }
    }*/

    /*private class BeaconDeviceMajorMinorComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            BeaconDeviceBean ldev = (BeaconDeviceBean) lhs;
            BeaconDeviceBean rdev = (BeaconDeviceBean) rhs;
            return (ldev.major * 65536 + ldev.minor) - (rdev.major * 65536 + rdev.minor);
        }
    }*/

/*    *//**
     *  排序
     * @param type
     *//*
    public void sortDevices(SortType type) {
        Comparator comparator;
        switch (type) {
            case BEACON_DEVICE_SORT_NONE:
                break;
            case BEACON_DEVICE_SORT_BY_RSSI:
                comparator = new BeaconDeviceRssiComparator();
                Collections.sort(beaconDevices, comparator);
                break;
            case BEACON_DEVICE_SORT_BY_MAJOR_MINOR:
                comparator = new BeaconDeviceMajorMinorComparator();
                Collections.sort(beaconDevices, comparator);
                break;
        }

        refreshDeviceHashMap();
    }*/

    /**
     * 找出最近的信标放于顶部
     */
    public void sortIntMethod() {
        if (beaconDevices.size() != 0) {
            // Log.i(TAG,"compare   size=" +beaconDevices.size());
            final long mCurrentTime = System.currentTimeMillis();
            Collections.sort(beaconDevices, new Comparator<BeaconDeviceBean>() {
                @Override
                public int compare(BeaconDeviceBean lhs, BeaconDeviceBean rhs) {
                    if (mCurrentTime - rhs.getRssiTimestamp() > 500) return 1;
                    int rssi1 = lhs.getmRssi();
                    int rssi2 = rhs.getmRssi();
                    if (rssi1 - rssi2 > 5) {
                        //Log.i(TAG,"compare    rssi2 = "+ rssi2 +"  -" + "  rssi1 "+ rssi1);
                        return -1;
                    } else if (rssi1 - rssi2 < 5) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            refreshDeviceHashMap();
          /*  BeaconDeviceBean beaconDeviceBean = beaconDevices.get(0);
            if (scannerName !=null ){
                scannerName.didUpdateNearestBeaconName(beaconDeviceBean.getName());
            }
            if (scannerUUID != null){
                scannerUUID.didUpdateNearestBeaconUUID(beaconDeviceBean.getUuid(),beaconDeviceBean.getMajor()
                        ,beaconDeviceBean.getMinor());
            }*/

        }
    }
}
