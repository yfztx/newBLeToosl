package com.longying.beacontools;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * Created by Administrator on 2016/7/12.
 *
 * @author zhang ting xuan
 *         提供蓝牙信标扫描SDK的控制接口
 */
public class BeaconScanner {

    private static final String TAG = "BeaconScanner";
    private static final int MIN_RSSI = -80;
    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private long mTime;
    private boolean adapter_lock ;
    private BeaconDeviceManager mBeaconManager;
    private ArrayList<BeaconDeviceBean> mBeacons;
    private BeaconDeviceBean beaconDeviceBean;
    private Timer mScanTimer;
    private TimerTask mScanTimerTask;
    private long SCAN_PERIOD =80;
    private BeaconScannerDelegate beaconScannerDelegate;
    private static final int IS_NAME_REGISTER = 0;
    private static final int IS_UUID_REGISTER =1;
    private int registerFlag;

    public void setBeaconScannerDlegateCallback(BeaconScannerDelegate bsd){
        beaconScannerDelegate = bsd;
    }

    public BeaconScanner() {
    }
    /*

     */
    public BeaconScanner(Context context) {
        this.context = context;
        //initBeaconDevice();
    }

    /**
     * 初始化
     */
    public void initBeaconDevice() {
        Log.i(TAG,"initBeaconDevice");
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBeaconManager = BeaconDeviceManager.getInstance();
        mBeacons = new ArrayList<>();
    }

    /**
     * 以UUID Major Minor方式注册扫描目标信标
     * 美游时代信标UUID：fa559aa8-345b-49b2-a7dc-b1a9535bc6ca
     * @param uuid  信标UUID
     * @param major 信标Major值
     * @param minor 信标Minor值
     *
     */
    public void registerBeacon(UUID uuid, short major, short minor ) {
        Log.i(TAG,"registerBeacon = uuid");
        registerFlag = IS_UUID_REGISTER;
      beaconDeviceBean = new BeaconDeviceBean(uuid,major,minor);
        mBeacons.add(beaconDeviceBean);
    }

    /** 以名称方式注册扫描目标信标
     * @param name
     */
    public void registerBeacon(String name) {
        Log.i(TAG,"registerBeacon  = name");
        registerFlag = IS_NAME_REGISTER;
        beaconDeviceBean = new BeaconDeviceBean(name);
        mBeacons.add(beaconDeviceBean);
    }

    /**
     * 开启蓝牙扫描
     */
    public void startBeaconScan() {
        Log.i(TAG,"startBeaconScan");
      scanLeDevice(true);

        if (mScanTimer == null) {
            mScanTimer = new Timer();
        }
        if (mScanTimerTask == null) {
            mScanTimerTask = new TimerTask() {
                @Override
                public void run() {
                   scanLeDevice(true);
                }
            };
        }
        mScanTimer.schedule(mScanTimerTask, SCAN_PERIOD, SCAN_PERIOD);
    }

    /**
     * 停止蓝牙扫描
     */
    public void stopBeaconScan() {
       scanLeDevice(false);
        mScanTimerTask.cancel();
        mScanTimerTask = null;
        mScanTimer.cancel();
        mScanTimer = null;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.i(TAG,"startBeaconScan");
            if (mScanning) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }

            mScanning = true;

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    /**
     *
     */
    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG,"mLeScanCallback   : onLeScan");
            mTime = System.currentTimeMillis();
            final int rssi_val = rssi;
            if (rssi < MIN_RSSI) return;
            BeaconDeviceBean newBeacon  = mBeaconManager.getDevice(device.getAddress());
            if (newBeacon == null) {
                newBeacon = new BeaconDeviceBean();
            }
            //newBeaconDevice.setmTime(mTime);
            if (newBeacon.updateInfo(device, rssi_val, scanRecord)) {
                //newBeaconDevice.setTimeoutCallback(device_timeout_cb);
                lock_list();
                //String name = newBeaconDevice.device.getName();
                newBeacon.setRssiTimestamp(mTime);
                if (isEqualDevice(newBeacon)) {
                    mBeaconManager.addDevice(newBeacon);
                    Log.i(TAG,"mLeScanCallback ,,,,,,");
                    if (beaconScannerDelegate !=null ){
                        BeaconDeviceBean mBeacon = mBeaconManager.getDevice();
                        beaconScannerDelegate.didUpdateNearestBeacon(mBeacon);
                    }
                }

                unlock_list();
            }

        }

    };

    private synchronized void unlock_list() {
        adapter_lock = false;
        notifyAll();
    }

    /**
     * 同步锁
     */
    private synchronized void lock_list() {
        while (adapter_lock){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        adapter_lock = true;
    }

    /**
     *  判断设备是否注册过的
     * @param bean 需要扫描的设备
     * @return  true 为选定的设备，反之为无效设备
     */
    public boolean isEqualDevice(BeaconDeviceBean bean) {
       for (int i =0; i<mBeacons.size();i++){
           boolean equals = bean.equals(mBeacons.get(i));
            if (equals) return equals;
       }
        return false;
    }

    private BeaconDeviceBean.device_timeout_callback device_timeout_cb = new BeaconDeviceBean.device_timeout_callback() {
        @Override
        public void onDeviceTimeout(final BeaconDeviceBean device) {

            lock_list();
            mBeaconManager.removeDevice(device);
            unlock_list();
        }


    };
}
