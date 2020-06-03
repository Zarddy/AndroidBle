package club.zarddy.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import club.zarddy.bluetooth.adapter.BluetoothDeviceListAdapter;
import club.zarddy.bluetooth.controller.BluetoothController;
import club.zarddy.bluetooth.utils.LogUtils;

public class MainActivity extends AppCompatActivity {

    private TextView txtResult;
    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private BaseAdapter bluetoothDeviceListAdapter;
    private BluetoothController mBluetoothController;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){

        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            // 当 Discovery 发现了一个设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从 Intent 中获取发现的 BluetoothDevice
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 将名字和地址放入要显示的适配器中
                bluetoothDeviceList.add(device);
                bluetoothDeviceListAdapter.notifyDataSetChanged();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showMessage("搜索完成");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 请求所有权限
        requestAllPermissions(this);

        mBluetoothController = BluetoothController.getInstance();

        txtResult = (TextView) findViewById(R.id.txt_result);

        bluetoothDeviceListAdapter = new BluetoothDeviceListAdapter(this, bluetoothDeviceList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(bluetoothDeviceListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mBluetoothController.connectBluetoothDevice(bluetoothDeviceList.get(position));
            }
        });

        // 注册这个 BroadcastReceiver 监听扫描到的蓝牙设备
        IntentFilter filter = new IntentFilter();
        // 设备连接状态改变
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        // 蓝牙设备状态改变
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 发现设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        // 搜索完成
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    // 是否支持蓝牙
    private void isSupportBluetooth() {
        String message = "是否支持蓝牙：" + mBluetoothController.isSupportBluetooth();
        showMessage(message);
    }

    // 是否已打开蓝牙
    private void isOpenBluetooth() {
        String message = "是否已打开蓝牙：" + mBluetoothController.isOpen();
        showMessage(message);
    }

    // 打开蓝牙
    private void openBluetooth() {
        boolean state = mBluetoothController.openBluetooth();
        String message = "蓝牙是否打开成功：" + state;
        showMessage(message);
    }

    // 关闭蓝牙
    private void closeBluetooth() {
        boolean state = mBluetoothController.closeBluetooth();
        String message = "蓝牙是否成功关闭：" + state;
        showMessage(message);
    }

    // 搜索所有
    private void scanAll() {
        bluetoothDeviceList.clear();
        bluetoothDeviceListAdapter.notifyDataSetChanged();

        boolean state = mBluetoothController.startDiscovery(this);
        String message = state ? "开始搜索所有设备" : "无法搜索所有设备";
        showMessage(message);
    }

    // 停止搜索
    private void stopScan() {
        boolean state = mBluetoothController.cancelDiscovery();
        String message = state ? "已停止搜索所有设备" : "无法停止搜索所有设备";
        showMessage(message);
    }

    // 已匹配设备
    private void showPairDevices() {
        bluetoothDeviceList.clear();
        bluetoothDeviceListAdapter.notifyDataSetChanged();

        Set<BluetoothDevice> pairedDevices = mBluetoothController.getBondedDevices();
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            showMessage("未找到已配对设备");
            return;
        }

        showMessage("--------------- 已配对设备 -------------");

        bluetoothDeviceList.clear();
        for (BluetoothDevice device : pairedDevices) {
            if (device == null) {
                continue;
            }
            bluetoothDeviceList.add(device);
        }
        bluetoothDeviceListAdapter.notifyDataSetChanged();
    }

    // 发送消息
    private void sendMessage() {
        mBluetoothController.sendMessage("Send Message!!!");
    }

    // 读取消息
    private void readMessage() {
        mBluetoothController.readMessage();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_is_support_ble: // 是否支持蓝牙
                isSupportBluetooth();
                break;

            case R.id.button_is_open: // 是否已打开蓝牙
                isOpenBluetooth();
                break;

            case R.id.button_open: // 打开蓝牙
                openBluetooth();
                break;

            case R.id.button_close: // 关闭蓝牙
                closeBluetooth();
                break;

            case R.id.button_scan_all: // 搜索所有设备
                scanAll();
                break;

            case R.id.button_stop_scan: // 停止搜索
                stopScan();
                break;

            case R.id.button_pair_devices: // 已配对设备
                showPairDevices();
                break;

            case R.id.button_send_data: // 发送消息
                sendMessage();
                break;

            case R.id.button_read_data: // 读取消息
                readMessage();
                break;
        }
    }

    private void showMessage(String message) {
        txtResult.setText(message);
        LogUtils.i(message);
    }

    /**
     * 请求所有权限
     */
    public void requestAllPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }
}
