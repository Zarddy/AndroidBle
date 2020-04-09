package club.zarddy.bluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import club.zarddy.bluetooth.R;

public class BluetoothDeviceListAdapter extends BaseAdapter {

    private List<BluetoothDevice> mData;
    private LayoutInflater layoutInflater;

    public BluetoothDeviceListAdapter(Context context, List<BluetoothDevice> data) {
        mData = data;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (mData == null) {
            return 0;
        }
        return mData.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        if (mData == null || getCount() <= position) {
            return null;
        }
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.adapter_bluetooth_device_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.fill(position);
        return convertView;
    }

    class ViewHolder {

        private TextView txtResult;

        ViewHolder(View convertView) {
            txtResult = (TextView) convertView.findViewById(R.id.txt_result);
        }

        public void fill(int position) {
            BluetoothDevice bluetoothDevice = mData.get(position);
            if (bluetoothDevice == null) {
                return;
            }

            String result = appendDeviceInfo(bluetoothDevice);
            txtResult.setText(result);
        }

        private String appendDeviceInfo(BluetoothDevice device) {
            if (device == null) {
                return "";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("name: " + device.getName());
            sb.append("\n");
            sb.append("address: " + device.getAddress());
            sb.append("\n");
            sb.append("bondState: " + device.getBondState());
            sb.append("\n");
            sb.append("type: " + device.getType());
            sb.append("\n");
            sb.append("uuid: " + device.getUuids());
            return sb.toString();
        }
    }
}
