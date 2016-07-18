package com.longying.mylibrary;

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
 * 提供蓝牙信标扫描SDK的控制接口
 * @author zhang ting xuan
 *
 */
public class BeaconScanner {

    private static final String TAG = "BeaconScanner";
    private static final int MIN_RSSI = -80;
    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private long mTime;
    private boolean adapter_lock;
    private BeaconDeviceManager mBeaconManager;
    private ArrayList<BeaconDeviceBean> mBeacons;
    private BeaconDeviceBean beaconDevice;
    private Timer mScanTimer;
    private TimerTask mScanTimerTask;
    private long SCAN_PERIOD = 80;
    private static final int IS_NAME_REGISTER = 0;
    private static final int IS_UUID_REGISTER = 1;
    private BeaconScannerListener resultListener;
    private int registerFlag;
    private Timer mTimer;
    private TimerTask mSortTask;


    public BeaconScanner() {
    }

    /**
     * 初始化数据
     * @param context 引入上下文环境
     */
    public BeaconScanner(Context context) {
        this.context = context;
        initBeaconDevice();

    }

    /**
     * 信标设备扫描监听器
     * @param bsl 监听接口
     */
    public void setBeaconScannerListenerCallback(BeaconScannerListener bsl){
        resultListener = bsl;
    }
    /**
     * 初始化
     */
    private void initBeaconDevice() {
        Log.i(TAG, "initBeaconDevice");
        mBeaconManager = BeaconDeviceManager.getInstance();
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = bluetoothManager.getAdapter();
        mBeacons = new ArrayList<>();
    }
    /*
     * 排序结果
     */
    private void sortAndResult() {
        mBeaconManager.sortIntMethod();
        if (mBeacons .size() == 0){return;}
        BeaconDeviceBean bean = mBeacons.get(0);
        BeaconDeviceBean device = mBeaconManager.getDevice();
        if (resultListener != null && device != null){
            if (bean.getName() != null){
                resultListener.didUpdateNearestBeaconName(device.getMac(),device.getName());
            }else if (bean.getUuid() != null){
                resultListener.didUpdateNearestBeaconUUID(device.getMac(),device.getUuid(),device.getMajor(),
                        device.getMinor());
            }
        }
    }

    /**
     * 以UUID Major Minor方式注册扫描目标信标
     * 美游时代信标UUID：fa559aa8-345b-49b2-a7dc-b1a9535bc6ca
     * @param uuid  信标UUID
     * @param major 信标Major值
     * @param minor 信标Minor值
     */
    public void registerBeacon(UUID uuid, short major, short minor) {
        Log.i(TAG, "registerBeacon = uuid");
        registerFlag = IS_UUID_REGISTER;
        beaconDevice = new BeaconDeviceBean(uuid, major, minor);
        mBeacons.add(beaconDevice);
    }

    /**
     * 以名称方式注册扫描目标信标
     *
     * @param name 设备名称
     */
    public void registerBeacon(String name) {
        Log.i(TAG, "registerBeacon  = name");
        registerFlag = IS_NAME_REGISTER;
        beaconDevice = new BeaconDeviceBean(name);
        mBeacons.add(beaconDevice);
    }

    /**
     * 开启蓝牙扫描
     */
    public void startBeaconScan() {
        Log.i(TAG, "startBeaconScan");
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


        //定时扫描排序
        if (mTimer ==null) mTimer = new Timer();
        if (mSortTask == null){
            mSortTask = new TimerTask() {
                @Override
                public void run() {

                    sortAndResult();
                }
            };
        }
        mTimer.schedule(mSortTask ,500,500);
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

        mTimer.cancel();
        mTimer = null;
        mSortTask.cancel();
        mSortTask = null;
    }

    private void scanLeDevice  (final boolean enable) {
        if (enable) {
            Log.i(TAG, "startBeaconScan");
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
     *发现信标设备回调
     */
    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.i(TAG, "mLeScanCallback   : onLeScan");
            mTime = System.currentTimeMillis();
            final int rssi_val = rssi;
            if (rssi < MIN_RSSI) return;
            BeaconDeviceBean newBeacon = mBeaconManager.getDevice(device.getAddress());
            if (newBeacon == null) {
                newBeacon = new BeaconDeviceBean();
            }
            if (newBeacon.updateInfo(device, rssi_val, scanRecord,resultListener)) {
                lock_list();
                newBeacon.setRssiTimestamp(mTime);
                if (isEqualDevice(newBeacon)) {
                    mBeaconManager.addDevice(newBeacon);
                  //  Log.i(TAG, "mLeScanCallback ,,,,,,");
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
        while (adapter_lock) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        adapter_lock = true;
    }

    /**
     * 判断设备是否注册过的
     *
     * @param bean 需要扫描的设备
     * @return true 为选定的设备，反之为无效设备
     */
    private boolean isEqualDevice(BeaconDeviceBean bean) {
        for (int i = 0; i < mBeacons.size(); i++) {
            boolean equals = bean.equals(mBeacons.get(i));
            //Log.i(TAG,"  name =="+mBeacons.get(i).getName());
            if (equals) return equals;
        }
        return false;
    }
}
