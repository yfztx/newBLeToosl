package com.longing.beacon.beaconconfig
        ;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.longying.mylibrary.BeaconDeviceBean;
import com.longying.mylibrary.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Administrator on 2016-04-21.
 */
public class BeaconDeviceListAdapter extends BaseAdapter {
    private static final String TAG = "BeaconDeviceBeanListAdapter";
    private ArrayList<BeaconDeviceBean> beaconDeviceBeans;
    private HashMap beaconDeviceBeanIndexes;
    private LayoutInflater mInflater;
    private Activity mContext;


    // private BeaconDeviceBeanRssiComparator rssiComparator;

    public enum SortType {
        BEACON_DEVICE_SORT_NONE,
        BEACON_DEVICE_SORT_BY_RSSI,
        BEACON_DEVICE_SORT_BY_MAJOR_MINOR,
    }

 /*   private class BeaconDeviceBeanRssiComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            BeaconDeviceBean ldev = (BeaconDeviceBean) lhs;
            BeaconDeviceBean rdev = (BeaconDeviceBean) rhs;
            return rdev.rssi - ldev.rssi;
        }
    }*/

    private class BeaconDeviceBeanMajorMinorComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            BeaconDeviceBean ldev = (BeaconDeviceBean) lhs;
            BeaconDeviceBean rdev = (BeaconDeviceBean) rhs;
            return (ldev.major * 65536 + ldev.minor) - (rdev.major * 65536 + rdev.minor);
        }
    }

    private void refreshDeviceHashMap() {
        beaconDeviceBeanIndexes.clear();
        for (BeaconDeviceBean device : beaconDeviceBeans) {
           beaconDeviceBeanIndexes.put(device.mac, beaconDeviceBeans.indexOf(device));
        }
    }

    public BeaconDeviceListAdapter(Activity c) {
        super();
        mContext = c;
        mInflater = c.getLayoutInflater();
        beaconDeviceBeans = new ArrayList<BeaconDeviceBean>();
        beaconDeviceBeanIndexes = new HashMap();
    }

    public void addDevice(BeaconDeviceBean device) {
        int i = beaconDeviceBeans.indexOf(device);
        if (i < 0){
            beaconDeviceBeans.add(device);
        }else {
            beaconDeviceBeans.remove(i);
            beaconDeviceBeans.add(0,device);
        }
       /* if (!beaconDeviceBeans.contains(device)) {
            //Log.i(TAG, "addDEvice:  num ==" + device);
            BeaconDeviceBeans.add(device);
            refreshDeviceHashMap();
        }*/
    }

    /**
     * 对集合数据排序
     */
   /* public void sortIntMethod() {
        if (BeaconDeviceBeans.size() != 0) {
            // Log.i(TAG,"compare   size=" +BeaconDeviceBeans.size());
            final long mCurrentTime = System.currentTimeMillis();
            Collections.sort(BeaconDeviceBeans, new Comparator<BeaconDeviceBean>() {
                @Override
                public int compare(BeaconDeviceBean lhs, BeaconDeviceBean rhs) {
                    if (mCurrentTime - rhs.getmTime() > 500) return 1;
                    int rssi1 = lhs.rssi;
                    int rssi2 = rhs.rssi;
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

        }
    }*/

    public void removeDevice(BeaconDeviceBean device) {
        if (beaconDeviceBeans.contains(device)) {
            beaconDeviceBeans.remove(device);
            refreshDeviceHashMap();
        }
    }

    public void updateDevice(BeaconDeviceBean device) {
        if (!beaconDeviceBeans.contains(device)) {
            beaconDeviceBeans.add(device);
        } else {
            beaconDeviceBeans.set(beaconDeviceBeans.indexOf(device), device);
        }
        refreshDeviceHashMap();
    }

    public BeaconDeviceBean getDevice(int position) {
        return beaconDeviceBeans.get(position);
    }

    public BeaconDeviceBean getDevice(String mac) {
        if (beaconDeviceBeanIndexes.containsKey(mac)) {
            return beaconDeviceBeans.get((int) beaconDeviceBeanIndexes.get(mac));
        }
        return null;
    }

    public void clear() {
        beaconDeviceBeans.clear();
        beaconDeviceBeanIndexes.clear();
    }

    @Override
    public int getCount() {
        //// TODO: 2016/7/11  BeaconDeviceBeans.size()
        return beaconDeviceBeans.size();
    }

    public int getCountByRSSI(int rssi) {
        int count = 0;
        for (int i = 0; i < beaconDeviceBeans.size(); i++) {
            if (beaconDeviceBeans.get(i).mRssi > rssi) {
                count++;
            }
        }

        return count;
    }

    @Override
    public Object getItem(int i) {
        //sortIntMethod(BeaconDeviceBeans);
        return beaconDeviceBeans.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //i =0;
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflater.inflate(R.layout.device_list, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = (TextView) view
                    .findViewById(R.id.text_device_name);
            viewHolder.deviceAddress = (TextView) view
                    .findViewById(R.id.text_mac_addr);
            viewHolder.deviceUUID = (TextView) view.findViewById(R.id.text_uuid);
            viewHolder.deviceMajorMinor = (TextView) view.findViewById(R.id.text_major_minor);
            viewHolder.rssi = (TextView) view.findViewById(R.id.text_rssi);
            viewHolder.distance = (TextView) view.findViewById(R.id.text_distance);
            viewHolder.battery_level = (TextView) view.findViewById(R.id.text_battery_level);
            viewHolder.image_signal_level = (ImageView) view.findViewById(R.id.image_signal_level);
            viewHolder.image_battery_level = (ImageView) view.findViewById(R.id.image_battery_level);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BeaconDeviceBean device = beaconDeviceBeans.get(i);
        final String deviceName = device.device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);

        /* LinearLayout ll = (LinearLayout) viewHolder.deviceName.getParent();
        ViewGroup.LayoutParams parames = ll.getLayoutParams();
        parames.height = 72;
        ll.setLayoutParams(parames); */
        viewHolder.deviceAddress.setText("MAC:" + device.mac);
        if (device.uuid != null) {
            viewHolder.deviceUUID.setText("UUID:" + device.uuid.toString());
        } else {
            viewHolder.deviceUUID.setText("UUID: null");
        }
        viewHolder.deviceMajorMinor.setText("Major: " + device.major + ", Minor: " + device.minor);
        viewHolder.position = i;
        viewHolder.rssi.setText("rssi: " + device.mRssi);
        viewHolder.distance.setText(String.format("%.2fm", Utils.distance_from_rssi(device.mRssi, (double) device.txpower)));
        viewHolder.image_signal_level.setImageAlpha(255);
        if (device.mRssi >= -59) {
            viewHolder.image_signal_level.setImageResource(R.drawable.signal_level_high);
        } else if (device.mRssi >= -70) {
            viewHolder.image_signal_level.setImageResource(R.drawable.signal_level_mid2);
        } else if (device.mRssi >= -80) {
            viewHolder.image_signal_level.setImageResource(R.drawable.signal_level_mid1);
        } else if (device.mRssi >= -90) {
            viewHolder.image_signal_level.setImageResource(R.drawable.signal_level_low);
        } else {
            viewHolder.image_signal_level.setImageAlpha(0);
        }

        if (device.battery_level < 0) {
            viewHolder.battery_level.setText("?");
            viewHolder.image_battery_level.setImageResource(R.drawable.battery_level_2);
        } else {
            viewHolder.battery_level.setText("" + (device.battery_level == -1 ? "?" : device.battery_level) + "%");
            if (device.battery_level < 10) {
                viewHolder.image_battery_level.setImageResource(R.drawable.battery_empty);
            } else if (device.battery_level < 30) {
                viewHolder.image_battery_level.setImageResource(R.drawable.battery_level_1);
            } else if (device.battery_level < 50) {
                viewHolder.image_battery_level.setImageResource(R.drawable.battery_level_2);
            } else if (device.battery_level < 70) {
                viewHolder.image_battery_level.setImageResource(R.drawable.battery_level_3);
            } else if (device.battery_level < 90) {
                viewHolder.image_battery_level.setImageResource(R.drawable.battery_level_4);
            } else {
                viewHolder.image_battery_level.setImageResource(R.drawable.battery_full);
            }
        }
        /*
        viewHolder.connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View itemView = (View) v.getParent().getParent();
                ViewHolder holder = (ViewHolder) itemView.getTag();
                int position = holder.position;
                Log.i(TAG, "connect button " + position + " onClick");
                BeaconDeviceBean bd = BeaconDeviceBeans.get(position);
                bd.connectDevice();
            }
        });
        */

        return view;
    }

    class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceUUID;
        TextView deviceMajorMinor;
        TextView rssi;
        TextView distance;
        TextView battery_level;
        ImageView image_signal_level;
        ImageView image_battery_level;
        int position;
    }

   /* public void sortDevices(SortType type) {
        Comparator comparator;
        switch (type) {
            case BEACON_DEVICE_SORT_NONE:
                break;
            case BEACON_DEVICE_SORT_BY_RSSI:
                comparator = new BeaconDeviceBeanRssiComparator();
                Collections.sort(BeaconDeviceBeans, comparator);
                break;
            case BEACON_DEVICE_SORT_BY_MAJOR_MINOR:
                comparator = new BeaconDeviceBeanMajorMinorComparator();
                Collections.sort(BeaconDeviceBeans, comparator);
                break;
        }

        refreshDeviceHashMap();
    }*/
}
