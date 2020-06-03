package club.zarddy.bluetooth.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothController {

    //该UUID表示串口服务
    //请参考文章<a href="http://wiley.iteye.com/blog/1179417">http://wiley.iteye.com/blog/1179417</a>
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//    private static final String SPP_UUID = "00001105-0000-1000-8000-00805f9B34FB";

    private static BluetoothController mController = new BluetoothController();
    private BluetoothAdapter mBluetoothAdapter;

    public static BluetoothController getInstance() {
        return mController;
    }

    private BluetoothController() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * 是否支持蓝牙功能
     */
    public boolean isSupportBluetooth() {
        return mBluetoothAdapter != null;
    }

    /**
     * 是否已打开蓝牙
     */
    public boolean isOpen() {
        if (mBluetoothAdapter == null) {
            return false;
        }

        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     */
    public boolean openBluetooth() {
        // 不支持蓝牙
        if (!isSupportBluetooth()) {
            return false;
        }

        if (isOpen()) { // 本来已经打开蓝牙
            return true;
        }

        return mBluetoothAdapter.enable();
    }

    /**
     * 关闭蓝牙
     */
    public boolean closeBluetooth() {
        // 不支持蓝牙
        if (!isSupportBluetooth()) {
            return false;
        }

        if (!isOpen()) { // 本来已经关闭蓝牙
            return false;
        }

        return mBluetoothAdapter.disable();
    }

    /**
     * 获取已匹配的设备
     */
    public Set<BluetoothDevice> getBondedDevices() {
        if (isSupportBluetooth() && isOpen()) {
            return mBluetoothAdapter.getBondedDevices();
        } else {
            return null;
        }
    }

    /**
     * 开始搜索设备
     */
    public boolean startDiscovery(Context context) {
         // 如果不支持蓝牙功能
        if (!isSupportBluetooth()) {
            return false;
        }

        if (!isOpen()) { // 如果没有打开蓝牙
            return false;
        }

        // 判断GPS是否可用
        if (isGpsEnable(context)) {
            return mBluetoothAdapter.startDiscovery();
        } else {
            //跳转到gps设置页
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
            return false;
        }
    }

    /**
     * 停止搜索设备
     */
    public boolean cancelDiscovery() {
        if (isSupportBluetooth() && isOpen() && mBluetoothAdapter.isDiscovering()) {
            return mBluetoothAdapter.cancelDiscovery();
        } else {
            return false;
        }
    }

    /**
     * GPS是否可用
     */
    public static final boolean isGpsEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    /**
     * 默认情况下，设备将变为可检测到并持续 120 秒钟。
     * 应用可以设置的最大持续时间为 3600 秒，值为 0 则表示设备始终可检测到。
     * 任何小于 0 或大于 3600 的值都会自动设为 120 秒。
     */
    public void setVisible(Activity activity, int time) {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time);
            activity.startActivity(discoverableIntent);
        }
    }

    /**
     * TODO 发送消息
     */
    public void sendMessage(String message) {

    }

    /**
     * TODO 读取消息
     */
    public void readMessage() {
        ;
    }

    /**
     * TODO 连接设备
     */
    public void connectBluetoothDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) {
            return;
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
//            bluetoothAdapter.cancelDiscovery();
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
//                showMessage("bluetoothSocket is connected: " + state);

                InputStream inputStream = bluetoothSocket.getInputStream();

                if (inputStream == null) {
                    return;
                }

                try {
                    byte[] buffer = new byte[1024];
                    int len;

                    while ((len = inputStream.read(buffer)) != -1) {
                        String msg = new String(buffer, 0, len);
//                        showMessage("数据：" + msg);
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
}
