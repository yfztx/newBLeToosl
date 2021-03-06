package com.longing.beacon.beaconconfig;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.longying.mylibrary.BeaconDeviceBean;
import com.longying.mylibrary.BeaconDeviceManager;
import com.longying.mylibrary.BeaconScanner;
import com.longying.mylibrary.BeaconScannerListener;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String TAG = "BeaconConfigMainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private static final long SCAN_PERIOD = 80;//300
    private BeaconDeviceListAdapter mDevices;
    private Timer mScanTimer = null;
    private TimerTask mScanTimerTask = null;
    private Timer mDeviceKeepAliveTimer = null;
    private TimerTask mDeviceKeepAliveTimerTask = null;
    private MainActivity activity;
    private Toolbar toolbar;
    private boolean adapter_lock;
    private boolean mac;
    private long mTime;
    private BeaconScanner mBeaconScanner;
    private BeaconDeviceManager instance;
    String mName[] = {"MYSD_5437B4", "MYSD_544167", "MYSD_54313F", "MYSD_543DC8", "MYSD_5445A6", "MYSD_5445A5"};


    public synchronized void lock_list() {
        while (adapter_lock) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        adapter_lock = true;
    }

    public synchronized void unlock_list() {
        adapter_lock = false;
        notifyAll();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        adapter_lock = false;
        if (toolbar != null) {
            toolbar.inflateMenu(R.menu.tool_bar_items);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Log.i(TAG, "onMenuItemClickListener.");
                    int menuItemId = item.getItemId();
                    if (menuItemId == R.id.action_sort_by_rssi) {
                        lock_list();
                      //  mDevices.sortDevices(BeaconDeviceListAdapter.SortType.BEACON_DEVICE_SORT_BY_RSSI);
                        mDevices.notifyDataSetChanged();
                        unlock_list();
                    } else if (menuItemId == R.id.action_settings) {

                    } else if (menuItemId == R.id.action_about) {

                    } else if (menuItemId == R.id.action_sort_by_majorminor) {
                        lock_list();
                      //  mDevices.sortDevices(BeaconDeviceListAdapter.SortType.BEACON_DEVICE_SORT_BY_MAJOR_MINOR);
                        mDevices.notifyDataSetChanged();
                        unlock_list();
                    }
                    return false;
                }
            });

            toolbar.setTitle(R.string.app_name);
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            android.util.Log.i("xgx", "current device does not support ble.");
            Toast.makeText(this, "Current phone does not support BLE.", Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Current phone does not support BLE.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "get adapter failed!");
            finish();
        }

        mBluetoothAdapter.enable();
        mBeaconScanner = new BeaconScanner(this);
        //mBeaconScanner.initBeaconDevice();
        //MYSD_5445BE".equals(name) || "MYSD_5444AC"
        mBeaconScanner.registerBeacon("MYSD_5445BE");
        mBeaconScanner.registerBeacon("MYSD_5444AC");

        for (int i=0;i< mName.length;i++){
            mBeaconScanner.registerBeacon(mName[i]);
        }
        mDevices = new BeaconDeviceListAdapter(this);
        instance = BeaconDeviceManager.getInstance();
        mBeaconScanner.setBeaconScannerListenerCallback(new BeaconScannerListener() {
            @Override
            public void didUpdateNearestBeaconName( String deviceID, String name) {
                if (name != null){
                    //BeaconDeviceBean device = mDevices.getDevice(deviceID);
                    final BeaconDeviceBean device = instance.getDevice(deviceID);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDevices.addDevice(device);
                            mDevices.notifyDataSetChanged();
                        }
                    });

                }
            }

            @Override
            public void didUpdateNearestBeaconUUID(String deviceID, UUID uuid, short major, short minor) {
                if (uuid != null){

                     final BeaconDeviceBean device = instance.getDevice(deviceID);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDevices.addDevice(device);
                            mDevices.notifyDataSetChanged();
                        }
                    });

                }
            }

            @Override
            public void didUpdateStatusReport(String deviceID, final UUID uuid, short major, short minor,
                                              int hardwareVersion, int softwareVersion, int batteryLevel,
                                              final String mac, Date applyDate) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                         //Toast.makeText(getApplicationContext(),mac,Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);

        if (listView != null) {
            listView.setAdapter(mDevices);
        /*    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.i(TAG, "onItemClick! position = " + position + " id = " + id);
                    BeaconDeviceBean device = mDevices.getDevice(position);
                    Intent intent = new Intent(activity, DeviceConfigActivity.class);
                    Bundle bundle = new Bundle();
                    // bundle.putString("mac", device.mac);
                    bundle.putParcelable("BeaconDeviceBean", device);
                    intent.putExtra("bundle", bundle);
                    Log.i(TAG, "put BeaconDeviceBean: " + device.toString());
                    startActivityForResult(intent, DeviceConfigActivity.ResultReqestCode);
                }
            });*/
        }

    }

    @Override
    public void onResume() {
        Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName() + "==");
        super.onResume();
        mBeaconScanner.startBeaconScan();
       /* if (!mScanning) {
            scanLeDevice(true);
        }

        if (mScanTimer == null) {
            mScanTimer = new Timer();
        }
        if (mScanTimerTask == null) {
            mScanTimerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scanLeDevice(true);
                        }
                    });
                }
            };
        }
        mScanTimer.schedule(mScanTimerTask, SCAN_PERIOD, SCAN_PERIOD);*/

        /*
        if (mDeviceKeepAliveTimer == null) {
            mDeviceKeepAliveTimer = new Timer();
        }

        if (mDeviceKeepAliveTimerTask == null) {
            mDeviceKeepAliveTimerTask = new TimerTask() {
                @Override
                public void run() {
                    // remove none alive beacon, timeout is 30s
                    Date now = new Date();
                    while (true) {
                        boolean have_timeout_dev = false;
                        int i=0;
                        while (true) {
                            if (i < mDevices.getCount()) {
                                final BeaconDeviceBean device;
                                device = mDevices.getDevice(i);
                                long time_diff = now.getTime() - device.beacon_received_time.getTime();
                                Log.i(TAG, "device " + device.mac + ", time diff = " + time_diff);
                                if (time_diff > 10 * 1000) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            lock_list();
                                            mDevices.removeDevice(device);
                                            mDevices.notifyDataSetChanged();
                                            toolbar.setTitle("BeaconConfig " + mDevices.getCountByRSSI(-70) + " Devices");
                                            unlock_list();

                                        }
                                    });
                                    have_timeout_dev = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                            i ++;
                        }
                        if (!have_timeout_dev) {
                            break;
                        }
                    }
                }
            };
        }

        mDeviceKeepAliveTimer.schedule(mDeviceKeepAliveTimerTask, 1000, 2000);
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private long startTime = 0;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if (isResume) {
                isResume = false;
                startTime = System.currentTimeMillis();
            }
            if (mScanning) {
               // mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }

            mScanning = true;

          //  mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
           // mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

   /* private BeaconDeviceBean.device_timeout_callback device_timeout_cb = new BeaconDeviceBean.device_timeout_callback() {
        @Override
        public void onDeviceTimeout(final BeaconDeviceBean device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lock_list();
                    mDevices.removeDevice(device);
                    mDevices.notifyDataSetChanged();
                    toolbar.setTitle("Total " + mDevices.getCount() + " Devices");
                    unlock_list();
                }
            });
        }
    };*/

    private boolean isResume = true;
    /*private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    mTime = System.currentTimeMillis();
                    final int rssi_val = rssi;
                    // if (rssi < -70) return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Log.i(TAG, "rssi: " + rssi_val);
                        *//*    Log.i(TAG, "LeScanCallback: device name: " + device.getName()
                                    + ", address:" + device.getAddress()
                                    + ", rssi:" + String.valueOf(rssi_val)
                                    + ", scanRecord(" + String.valueOf(scanRecord.length) + "bytes): " + Utils.bytesToHexString(scanRecord));
*//*
                            String mac = device.getAddress();
                            long l = System.currentTimeMillis() - mTime;
                            if (l < 500) {
                                BeaconDeviceBean newBeaconDevice = mDevices.getDevice(device.getAddress());
                                if (newBeaconDevice == null) {
                                    newBeaconDevice = new BeaconDeviceBean();
                                }
                                newBeaconDevice.setmTime(mTime);
                                if (newBeaconDevice.updateInfo(device, rssi_val, scanRecord)) {
                                    newBeaconDevice.setTimeoutCallback(device_timeout_cb);
                                    lock_list();
                                    String name = newBeaconDevice.device.getName();

                                    newBeaconDevice.setmTime(mTime);

                                    if (isName(name)) {
                                        mDevices.addDevice(newBeaconDevice);
                                    }
                                    *//*if("MYSD_5445BE".equals(name) || "MYSD_5444AC".equals(name)){ //测试数据

                                    }*//*
                                    if ( System.currentTimeMillis() - startTime > 500) {
                                        isResume = true;//记录这个状态值
                                       // Log.i(TAG," System.currentTimeMillis()");
                                        mDevices.sortIntMethod();
                                    }
                                    mDevices.notifyDataSetChanged();
                                    toolbar.setTitle("Total " + mDevices.getCount() + " Devices");
                                    unlock_list();

                                }
                            }
                        }
                    });
                }
            };*/

    private int saveData(int[] array, int rssi) {
        int num = 0;
        Log.i(TAG, "saveData === 保存最近的5个Rssi数据    =========");
        for (int i = array.length - 1; i >= 0; i--) {
            if (i == 0) {
                array[0] = rssi;
            } else {
                array[i] = array[i - 1];
            }
            Log.i(TAG, " " + array[i] + "\n");
            num += array[i];
        }

        return num / 5;
    }

    @Override
    public void onPause() {
        Log.i(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        super.onPause();
       /* if (mScanning) {
            scanLeDevice(false);
        }*/

        mBeaconScanner.stopBeaconScan();

       /* mScanTimerTask.cancel();
        mScanTimerTask = null;
        mScanTimer.cancel();
        mScanTimer = null;*/

        /*
        mDeviceKeepAliveTimerTask.cancel();
        mDeviceKeepAliveTimerTask = null;
        mDeviceKeepAliveTimer.cancel();
        mDeviceKeepAliveTimer = null;
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == DeviceConfigActivity.ResultReqestCode) {
            if (resultCode == 0) {
                if (intent != null) {
                    Bundle data = intent.getBundleExtra("result");
                    final BeaconDevice device = data.getParcelable("BeaconDeviceBean");
                    Log.i(TAG, "===onActivityResult=== status=" + device.getDeviceStatus());
                    if (device.getDeviceStatus() == 129) {
                        mBluetoothAdapter.disable();
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mBluetoothAdapter.enable();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "locking list for onActivityResult");
                            lock_list();
                            Log.i(TAG, "list locked for onActivityResult");
                            //mDevices.updateDevice(device);
                            mDevices.notifyDataSetChanged();
                            toolbar.setTitle("Total " + mDevices.getCount() + " Devices");
                            unlock_list();
                            Log.i(TAG, "unlocked list for onActivityResult");
                        }
                    });
                }
            }
        }
    }


    public boolean isName(String name) {
        for (int i = 0; i < mName.length; i++) {
            if (mName[i].equals(name)) {
                return true;
            }
        }
        return false;
    }


}
