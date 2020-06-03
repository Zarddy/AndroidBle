package club.zarddy.bluetooth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import club.zarddy.bluetooth.adapter.BluetoothDeviceListAdapter;
import club.zarddy.bluetooth.utils.LogUtils;

public class MainActivity extends AppCompatActivity {

    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//    private static final String SPP_UUID = "00001105-0000-1000-8000-00805f9B34FB";

    private TextView txtResult;
    private ListView listView;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;

    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private BaseAdapter bluetoothDeviceListAdapter;

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

        txtResult = (TextView) findViewById(R.id.txt_result);
        listView = (ListView) findViewById(R.id.list_view);
        bluetoothDeviceListAdapter = new BluetoothDeviceListAdapter(this, bluetoothDeviceList);
        listView.setAdapter(bluetoothDeviceListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = bluetoothDeviceList.get(position);
                bluetoothDevice.toString();
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 注册这个 BroadcastReceiver 监听扫描到的蓝牙设备
        IntentFilter filter = new IntentFilter();
        //设备连接状态改变
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //蓝牙设备状态改变
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //发现设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
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
        boolean state = bluetoothAdapter != null;
        String message = "是否支持蓝牙：" + state;

        showMessage(message);
    }

    // 是否已打开蓝牙
    private void isOpenBluetooth() {
        if (bluetoothAdapter == null) {
            showMessage("本设备不支持蓝牙");
            return;
        }

        boolean state = bluetoothAdapter.isEnabled();
        String message = "是否已打开蓝牙：" + state;

        showMessage(message);
    }

    // 打开蓝牙
    private void openBluetooth() {
        if (bluetoothAdapter == null) {
            showMessage("本设备不支持蓝牙");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            boolean state = bluetoothAdapter.enable();
            String message = "蓝牙是否打开成功：" + state;

            showMessage(message);
            return;
        }

        showMessage("蓝牙已打开");
    }

    // 关闭蓝牙
    private void closeBluetooth() {
        if (bluetoothAdapter == null) {
            showMessage("本设备不支持蓝牙");
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            boolean state = bluetoothAdapter.disable();
            String message = "蓝牙是否成功关闭：" + state;

            showMessage(message);
            return;
        }

        showMessage("蓝牙已关闭");
    }

    // 搜索所有
    private void scanAll() {
        if (bluetoothAdapter == null) {
            showMessage("本设备不支持蓝牙");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            showMessage("未打开蓝牙");
            return;
        }

        bluetoothDeviceList.clear();
        bluetoothDeviceListAdapter.notifyDataSetChanged();

        boolean state = bluetoothAdapter.startDiscovery();
        String message = state ? "开始搜索所有设备" : "无法搜索所有设备";
        showMessage(message);
    }

    // 停止搜索
    private void stopScan() {
        if (bluetoothAdapter == null) {
            showMessage("本设备不支持蓝牙");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            showMessage("未打开蓝牙");
            return;
        }

        if (!bluetoothAdapter.isDiscovering()) {
            showMessage("未在搜索设备");
            return;
        }

        boolean state = bluetoothAdapter.cancelDiscovery();
        String message = state ? "已停止搜索所有设备" : "无法停止搜索所有设备";
        showMessage(message);
    }

    // 已匹配设备
    private void showPairDevices() {
        if (bluetoothAdapter == null) {
            showMessage("本设备不支持蓝牙");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            showMessage("未打开蓝牙");
            return;
        }

        pairedDevices = bluetoothAdapter.getBondedDevices();
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

    // TODO 发送消息
    private void sendMessage() {

    }

    // TODO 读取消息
    private void readMessage() {

        for (BluetoothDevice device : pairedDevices) {
            if (device == null) {
                continue;
            }
//            if (device.getAddress().contains("78")) {
            if (device.getAddress().contains("7D")) {
                new ConnectThread(device).start();
            }
        }

//        new AcceptThread().start();

    }

    private class AcceptThread extends Thread {

        private BluetoothServerSocket mServerSocket;
        public AcceptThread(){
            try {
                mServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("APP", UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            BluetoothSocket socket = null;
            while(true){
                try {
                    socket = mServerSocket.accept();
                    if (socket != null) {
                        // 自定义方法
                        manageConnectedSocket(socket);
                        mServerSocket.close();
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        public void cancel(){
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO 作为客户端
    private class ConnectThread extends Thread {
        private BluetoothDevice mDevice;
        private BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device){
            try {
                mDevice = device;
                // 这里的 UUID 需要和服务器的一致
                mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run(){
            // 关闭发现设备
            bluetoothAdapter.cancelDiscovery();
            try{
                mSocket.connect();
                manageConnectedSocket(mSocket);

            }catch(IOException connectException){
                connectException.printStackTrace();

                try{
                    mSocket.close();
                }catch(IOException closeException){
                    closeException.printStackTrace();
                    return;
                }
            }
            // 自定义方法
            manageConnectedSocket(mSocket);
        }

        public void cancel(){
            try{
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket bluetoothSocket) {
        try {
            boolean state = bluetoothSocket.isConnected();

            if (state) {
                showMessage("bluetoothSocket is connected: " + state);

                InputStream inputStream = bluetoothSocket.getInputStream();

                if (inputStream == null) {
                    return;
                }

                try {
                    byte[] buffer = new byte[1024];
                    int len;

                    while ((len = inputStream.read(buffer)) != -1) {
                        String msg = new String(buffer, 0, len);
                        showMessage("数据：" + msg);
                    }

                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
