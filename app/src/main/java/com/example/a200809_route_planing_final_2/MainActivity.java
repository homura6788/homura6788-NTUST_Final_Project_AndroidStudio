package com.example.a200809_route_planing_final_2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //宣告藍芽相關物件
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mBTSocket=null;
    Set<BluetoothDevice> pairedDevices;
    int REQUEST_ENABLE_BT=1;
    //宣告一般物件
    Button btn_on, btn_off, btn_showlist, btn_scan, btn_tsf, btn_cnt;
    Intent btEnablingIntent;
    ListView lv_pairedlist, lv_scanlist;
    //
    Button btn_sd;
    EditText ET_msg;
    //宣告Array
    ArrayAdapter<String> arrayAdapter1;
    ArrayList<String> stringArrayList=new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter2;
    //
    public BluetoothConnectionService mBTCnt=new BluetoothConnectionService(MainActivity.this);

    //設定request的命令種類
    private static final int BT_ENABLE_REQUEST = 10; // This is the code we use for BT Enable
    private static final int SETTINGS = 20;
    //UUID
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private UUID mDeviceUUID = UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");
    private int mBufferSize = 50000; //Default

    //設定狀態
    public static final String DEVICE_EXTRA = "com.example.a200809_route_planing_final_2.SOCKET";
    public static final String DEVICE_UUID = "com.example.a200809_route_planing_final_2.uuid";
    private static final String DEVICE_LIST = "com.example.a200809_route_planing_final_2.devicelist";
    private static final String DEVICE_LIST_SELECTED = "com.example.a200809_route_planing_final_2.devicelistselected";
    public static final String BUFFER_SIZE = "com.example.a200809_route_planing_final_2.buffersize";
    public static final String BT_CONNECTION = "com.example.a200809_route_planing_final_2.btconnection";
    private static final String TAG = "MainActivity";
    private boolean mIsBluetoothConnected = false;
    private boolean mConnectSuccessful = true;
    //
    private final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION =1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //必要

        //透過ID找到物件
        btn_on=findViewById(R.id.button);
        btn_off=findViewById(R.id.button2);
        btn_showlist=findViewById(R.id.button3);
        btn_scan = findViewById(R.id.button4);
        //btn_tsf = findViewById(R.id.button5);
        btn_cnt = findViewById(R.id.button6);


        lv_pairedlist=findViewById(R.id.listview);
        lv_scanlist=findViewById(R.id.listview2);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //藍芽狀態請求
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //


        List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
        //取得掃描裝置放入listview
        arrayAdapter2=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,stringArrayList);
        lv_scanlist.setAdapter(arrayAdapter2);

        //註冊intent filter
        IntentFilter intentFilter1=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver,intentFilter1);
        IntentFilter intentFilter2=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, intentFilter2);


        //呼叫副程式
        bluetoothONMethod();
        bluetoothOFFMethod();
        pairedListMethod();
        arrayListMethod(savedInstanceState);
        connectingToPairedDevice();
        scanButtonMethod();
        //transferMessageWindow();

        //getUUID();
    }

    /*@Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }*/

    //btn_on按下時 確認支不支援藍芽 如果支援藍芽 請求允許開啟藍芽
    private void bluetoothONMethod(){
        btn_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapter==null){
                    showToast("bluetooth not available");
                }
                else{
                    showToast("bluetooth is available");
                    startActivityForResult(btEnablingIntent,REQUEST_ENABLE_BT);
                }
            }
        });
    }

    //請求狀態回饋 1藍芽開啟
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //bluetooth is enabled
                showToast("bluetooth is enable");
                locationPermission();
            }
            else if (resultCode == RESULT_CANCELED) {
                //bluetooth enabling is canceled
                showToast("bluetooth request canceled");
            }
        }
    }

    //按下btn_off 關閉藍芽
    private void bluetoothOFFMethod(){
        btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.disable();
                    showToast("turning bluetooth off");
                    //arrayAdapter2.notifyDataSetChanged();
                    arrayAdapter2.clear();
                    //SD.cancel();
                }
                else{
                    showToast("bluetooth is already off");
                }
            }
        });
    }


    //按下btn_showList 如果支援藍芽 執行SearchDevices
    private void pairedListMethod(){
        btn_showlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果不支援藍芽 回傳找不到藍芽
                if (mBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
                }
                //呼叫search devices尋找已連結裝置(只列出已連結過的)
                else {
                    /*Set<BluetoothDevice> bt;
                    String[] strings;
                    bt = mBluetoothAdapter.getBondedDevices();
                    strings = new String[bt.size()];
                    int index=0;

                    //取得藍芽裝置的名稱並存進strings的陣列 再放進listview裡?
                    if(bt.size()>0){
                        for(BluetoothDevice device:bt){
                            strings[index] = device.getName();
                            index++;
                        }
                        arrayAdapter1=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);
                        lv_pairedlist.setAdapter(arrayAdapter1);
                    }*/
                    System.out.println(mBluetoothAdapter.getBondedDevices());
                    new SearchDevices().execute();
                    showToast("Search Devices Success");
                }
            }
        });
    }
    //觸發初始化list 設定arrayList 列出已配對裝置
    private void arrayListMethod(Bundle savedInstanceState){
        if (savedInstanceState != null) {
            ArrayList<BluetoothDevice> list = savedInstanceState.getParcelableArrayList(DEVICE_LIST);
            if (list != null) {
                initList(list);
                MyAdapter adapter = (MyAdapter) lv_pairedlist.getAdapter();
                int selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED);
                if (selectedIndex != -1) {
                    adapter.setSelectedIndex(selectedIndex);
                    btn_cnt.setEnabled(true);
                }
            }
            else{
                initList(new ArrayList<BluetoothDevice>());
            }
        }
        else{
            initList(new ArrayList<BluetoothDevice>());
        }
    }
    //初始化list Adapter
    //按下listView內的物件時的動作 如果List內的東西被選擇 讓connect按鈕enabled
    private void initList(List<BluetoothDevice> objects) {
        final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
        lv_pairedlist.setAdapter(adapter);
        //當按下listView內的物件時
        lv_pairedlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedIndex(position);
                btn_cnt.setEnabled(true);
            }
        });
    }


    //SearchDevices列出已配對裝置
    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {
        @Override
        protected List<BluetoothDevice> doInBackground(Void... params) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();

            for (BluetoothDevice device : pairedDevices) {
                listDevices.add(device);
            }
            return listDevices;
        }
        //確認list裡面有東西了 把它放進listView裡面
        @Override
        protected void onPostExecute(List<BluetoothDevice> listDevices) {
            super.onPostExecute(listDevices);
            if (listDevices.size() > 0) {
                MyAdapter adapter = (MyAdapter) lv_pairedlist.getAdapter();
                adapter.replaceItems(listDevices);
            }
            else {
                showToast("No paired devices found, please pair your serial BT device and try again");
            }
        }
    }
    /**
     * Custom adapter to show the current devices in the list. This is a bit of an overkill for this
     * project, but I figured it would be good learning
     * Most of the code is lifted from somewhere but I can't find the link anymore
     * @author ryder
     *
     */
    //標準的adapter 給SearchDevice使用
    private class MyAdapter extends ArrayAdapter<BluetoothDevice> {
        private int selectedIndex;
        private Context context;
        private int selectedColor = Color.parseColor("#abcdef");
        private List<BluetoothDevice> myList;

        public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {
            super(ctx, resource, textViewResourceId, objects);
            context = ctx;
            myList = objects;
            selectedIndex = -1;
        }

        public void setSelectedIndex(int position) {
            selectedIndex = position;
            notifyDataSetChanged();
        }

        public BluetoothDevice getSelectedItem() {
            return myList.get(selectedIndex);
        }

        @Override
        public int getCount() {
            return myList.size();
        }

        @Override
        public BluetoothDevice getItem(int position) {
            return myList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView tv;
        }

        public void replaceItems(List<BluetoothDevice> list) {
            myList = list;
            notifyDataSetChanged();
        }

        public List<BluetoothDevice> getEntireList() {
            return myList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            ViewHolder holder;
            if (convertView == null) {
                vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder = new ViewHolder();

                holder.tv = (TextView) vi.findViewById(R.id.lstContent);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            if (selectedIndex != -1 && position == selectedIndex) {
                holder.tv.setBackgroundColor(selectedColor);
            } else {
                holder.tv.setBackgroundColor(Color.WHITE);
            }
            BluetoothDevice device = myList.get(position);
            holder.tv.setText(device.getName() + "\n " + device.getAddress());

            return vi;
        }

    }

    //按下btn_cnt 連結到已配對裝置
    private void connectingToPairedDevice(){
        btn_cnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevice mDevice = ((MyAdapter) (lv_pairedlist.getAdapter())).getSelectedItem();
                try {
                    if (mBTSocket == null || !mIsBluetoothConnected) {
                        final BluetoothDevice device = ((MyAdapter) (lv_pairedlist.getAdapter())).getSelectedItem();
                        Intent intent = new Intent(getApplicationContext(), transfer_msgActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                        intent.putExtra(BUFFER_SIZE, mBufferSize);
                        startActivity(intent);
                        //showToast("success to connect");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    mConnectSuccessful=false;
                    showToast("fail to connect");
                }
                /*if (!mConnectSuccessful) {
                    Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                }
                else {
                    showToast("Connected to device");
                    mIsBluetoothConnected = true;
                }*/
                /*Intent intent = new Intent(getApplicationContext(), ControllingActivity.class);
                intent.putExtra(DEVICE_EXTRA, device);
                intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                intent.putExtra(BUFFER_SIZE, mBufferSize);
                startActivity(intent);*/
            }
        });
    }



    //按下btn_scan 開始掃描可連結裝置
    private void scanButtonMethod(){
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mBluetoothAdapter.isDiscovering()){
                    //showExplanation("Warning", "ask for permission", Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                    //請求確認位置
                    arrayAdapter2.clear();
                    mBluetoothAdapter.startDiscovery();
                    showToast("start scanning...");
                }
                else{
                    mBluetoothAdapter.cancelDiscovery();
                    showToast("cancel scanning...");
                }
            }
        });
    }

    //按下btn_tsf 到傳送訊息視窗
    /*
    private void transferMessageWindow(){
        btn_tsf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(!(mBTSocket == null)){
                        final BluetoothDevice device = ((MyAdapter) (lv_pairedlist.getAdapter())).getSelectedItem();
                        Intent intent = new Intent(getApplicationContext(), transfer_msgActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                        intent.putExtra(BUFFER_SIZE, mBufferSize);

                        startActivity(intent);
                    }
                }
                catch(Exception e){
                    showToast("mBTsocket==null");
                    e.printStackTrace();
                }

            }
        });
    }*/


    //取得UUID
    private void getUUID(){
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);

            if(uuids != null) {
                for (ParcelUuid uuid : uuids) {
                    Log.d(TAG, "UUID: " + uuid.getUuid().toString());
                }
            }
            else{
                Log.d(TAG, "Uuids not found, be sure to enable Bluetooth!");
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    //請求確認位置
    private void locationPermission(){
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }

    //註冊廣播 1列出可連結裝置
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();
            // 1找到可連結的新裝置
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!stringArrayList.contains(device)) {
                    /*try{*/
                        if(!(device.getName()==null)){
                            stringArrayList.add(device.getName());
                            showToast("name:"+ device.getName() + "\r\naddress:" + device.getAddress());
                            //Toast.makeText(MainActivity.this, "name: " + device.getName() + " " + device.getAddress(), Toast.LENGTH_LONG).show();
                            arrayAdapter2.notifyDataSetChanged();
                        }
                    /*}
                    catch(Exception e){
                        e.printStackTrace();
                    }*/
                }
            }
            // 1當掃描結束還未找到裝置
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (stringArrayList == null || stringArrayList.isEmpty()) {
                    showToast("No Devices");
                    //Toast.makeText(MainActivity.this, "No Devices", Toast.LENGTH_LONG).show();
                }
            }
        }
    };


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.homescreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivityForResult(intent, SETTINGS);
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/
    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showExplanation(String title, String message, final String permission, final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                requestPermission(permission, permissionRequestCode);
            }
        });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permissionName}, permissionRequestCode);
    }
    */

    //Toast 的簡化程式
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}


