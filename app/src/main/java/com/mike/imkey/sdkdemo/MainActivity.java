package com.mike.imkey.sdkdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn_start = (Button) findViewById(R.id.btn_start);
        Button btn_search = (Button) findViewById(R.id.btn_search);
        Button btn_stop_search = (Button) findViewById(R.id.btn_stop_search);
        Button btn_send = (Button) findViewById(R.id.btn_send);
        btn_start.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_stop_search.setOnClickListener(this);
        btn_send.setOnClickListener(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 21:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    search();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                openBle();
                break;
            case R.id.btn_search:
                startScan();
//                testConn();
                break;
            case R.id.btn_stop_search:
                stopScan();

                mBluetoothGatt = mDevice.connectGatt(MainActivity.this, false, mGattCallback);
                mBluetoothGatt.connect();
                break;
            case R.id.btn_send:
                send();
                break;
        }
    }

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        //连接状态改变的回调
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 连接成功后启动服务发现
                Log.e(TAG, "启动服务发现:" + mBluetoothGatt.discoverServices());
            }
        };

        //发现服务的回调
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "成功发现服务");
            }else{
                Log.e(TAG, "服务发现失败，错误码为:" + status);
            }
        };

        //写操作的回调
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "写入成功" +characteristic.getValue());
            }
        };

        //读操作的回调
        public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "读取成功" +characteristic.getValue());
            }
        }

        //数据返回的回调（此处接收BLE设备返回数据）
        public void onCharacteristicChanged(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "ttt" +characteristic.getValue());
        };
    };

    BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "testtt";

    private void openBle() {
        if (mAdapter == null) {
            Log.e(TAG, "ble not support");
            return;
        }

        if (!mAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 11);
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    private void startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 21);
            } else {
                search();
            }
        }
    }

    private Handler mHandler = new Handler();

    private void search() {
//        mAdapter.startDiscovery();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, 5000);
        mAdapter.startLeScan(leScanCallback);
    }

    private void stopScan() {
        mAdapter.stopLeScan(leScanCallback);
    }


    private BluetoothDevice mDevice;
    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "发现设备：" + device.getName() + " " + device.getAddress());
            if ((device.getName() != null) && device.getName().contains("JuBiter")) {
                Log.i(TAG, "发现jubiter" + device.getAddress());
                mDevice = device;
            }
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 发现设备
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, device.getName() + "/n" + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "find over");
            }
        }
    };

    private void send(){
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString("46540001-0001-00c3-0001-465453414645"));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("46540002-0001-00c3-0001-465453414645"));
        characteristic.setValue("test..");
        mBluetoothGatt.writeCharacteristic(characteristic);

        BluetoothGattCharacteristic characteristic2 = service.getCharacteristic(UUID.fromString("46540003-0001-00c3-0001-465453414645"));
        mBluetoothGatt.readCharacteristic(characteristic2);
    }

//    static OkHttpClient client = new OkHttpClient();
//
//    static String conn(String url) throws IOException {
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        Response response = client.newCall(request).execute();
//        return response.body().string();
//    }
//    public static void testConn(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String res = conn("http://172.21.75.33:8080/imkey/seSecureCheck");
//                    Log.i(TAG,res);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
}
