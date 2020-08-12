package com.example.a200809_route_planing_final_2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class transfer_msgActivity extends AppCompatActivity {
    //
    Button btn_sd, btn_dr, btn_sw;
    Button btn_lt1, btn_lt2, btn_lt3, btn_lt4, btn_lt5, btn_lt6, btn_lt7, btn_lt8, btn_lt9, btn_lt10, btn_lt11, btn_lt12, btn_lt13, btn_lt14, btn_lt15;
    EditText ET_msg;
    TextView TV_msg;
    private static final String TAG = "BluetoothConnectionServ";
    //
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mBSocket;
    BluetoothDevice mDevice;
    UUID mDeviceUUID;
    int inistart=0;
    public int btn_selected_num=1;
    public int btn_start_point=0;
    public int btn_goal_point=0;
    //z//
    int rt_count_out=0, rt_pt_num=0, card_now=0, card_past=0, ifright=0;
    int sw=1;
    String[] rt1={""};
    String[] rt2={""};
    String[] rt3={""};
    String[] rt4={""};
    int[] routePlanningFL={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    String regString1=("");
    String regString2=("");
    String regcontent=("");
    int checkc=2;
    int[][] regtrCoordinate= new int[][]{{0,0,0,0},{2,13,0,16},{3,1,0,0},{4,2,0,0},{5,3,0,18},{6,4,14,11},{14,5,0,0},{15,14,0,0},{9,15,0,0},{0,8,15,10},{9,0,0,0},{12,5,0,17},{13,11,0,0},{1,12,0,0},{7,6,5,15},{8,7,9,14},{0,1,18,17},{0,11,16,18},{0,4,17,16}};
    //			                              0		    1		 2			3	 		4		    5		   6		  7		   8		  	9	         10	         11		    12			13		14		    15			16		  	17		 	18

    int[][] trCoordinate= new int[][]{{0,0,0,0},{2,13,0,16},{3,1,0,0},{4,2,0,0},{5,3,0,18},{6,4,14,11},{14,5,0,0},{15,14,0,0},{9,15,0,0},{0,8,15,10},{9,0,0,0},{12,5,0,17},{13,11,0,0},{1,12,0,0},{7,6,5,15},{8,7,9,14},{0,1,18,17},{0,11,16,18},{0,4,17,16}};
    //			                         0		    1		 2			3	 		4		    5		   6		  7		   8		  	9	         10	         11		    12			13		14		    15			16		  	17		 	18

    public BluetoothConnectionService mBTCnt=new BluetoothConnectionService(transfer_msgActivity.this);
    //BluetoothConnectionService mBluetoothConnection;

    //狀態
    public static final String ACTION_TEXT_CHANGED = "changed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_msg);


        //透過ID找到物件
        //btn_sd=findViewById(R.id.button7);
        btn_dr=findViewById(R.id.button8);
        btn_sw=findViewById(R.id.button9);
        //ET_msg=findViewById(R.id.editText);
        TV_msg=findViewById(R.id.textView);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //MainActivity傳來的
        Intent intent =getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mBTCnt.startClient(mDevice,mDeviceUUID);
        //


        // Register for broadcasts when a device is discovered
        /*IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mReceiver, intentFilter);*/

        //z//
        //IntentFilter intentFilter1=new IntentFilter(BluetoothConnectionService.ACTION_TEXT_CHANGED);
        registerReceiver(broadcastReceiver,new IntentFilter(BluetoothConnectionService.ACTION_TEXT_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(ACTION_TEXT_CHANGED));
        //global variable


        // inside doInBackground() function

        //副程式

        initialMethod();

    }

    //初始化
    private void initialMethod(){
            btn_start_point=card_now;
            //sendMessageMethod();
            ButtonClickMethod();
            ButtonPositioning();
            determineRouteMethod();
            swRouteMethod();
            //showToast("已連線");
    }

    //每次到新地點再次定位
    private void againMethod() throws InterruptedException {
        btn_start_point=card_now;
        checkIfRouteRight();
        ButtonPositioning();
        //iniHeadDirection();
        swRouteMethod();
    }

    //檢查現在路線是否正確
    private void checkIfRouteRight() throws InterruptedException {
        String[][] rt={{"0"},rt1,rt2,rt3,rt4};
        if(rt[sw].length>1){
            if(checkc>=rt[sw].length){
                //Log.d(TAG,"checkc>=rt[sw].length)");
                checkc=2;
                if(Integer.parseInt(rt[sw][checkc])==btn_start_point){
                    Log.d(TAG,"路線正確");
                    //mBTCnt.write("w".getBytes(StandardCharsets.US_ASCII));
                    ifright+=2;
                    //routePlanning();
                    writeRouteMethod();
                    Log.d(TAG,"已送出新路線");
                }
                else{
                    Log.d(TAG,"btn_start_point= "+btn_start_point+" 原定= "+Integer.parseInt(rt[sw][checkc]));
                    Log.d(TAG,"正在重新規劃路線...");
                    sw=1;
                    ifright=0;
                    //mBTCnt.write("w".getBytes(StandardCharsets.US_ASCII));
                    routePlanning();
                    buttonSelectedByRouteMethod();
                    changemsg(TV_msg,"重新規劃路線");
                    Log.d(TAG,"已送出新路線");
                    //Log.d(TAG,"checkc= "+checkc);
                }
            }
            else{
                //Log.d(TAG,"checkc<rt[sw].length)");
                if(Integer.parseInt(rt[sw][checkc])==btn_start_point){
                    checkc=2;
                    Log.d(TAG,"路線正確");
                    //mBTCnt.write("w".getBytes(StandardCharsets.US_ASCII));
                    ifright+=2;
                    //routePlanning();
                    writeRouteMethod();
                    Log.d(TAG,"已送出新路線");
                    //Log.d(TAG,"checkc= "+checkc);
                }
                else{
                    Log.d(TAG,"btn_start_point= "+btn_start_point+",原定= "+Integer.parseInt(rt[sw][checkc]));
                    Log.d(TAG,"正在重新規劃路線...");
                    sw=1;
                    ifright=0;
                    //mBTCnt.write("w".getBytes(StandardCharsets.US_ASCII));
                    routePlanning();
                    buttonSelectedByRouteMethod();
                    changemsg(TV_msg,"重新規劃路線");
                    Log.d(TAG,"已送出新路線");
                    checkc=2;
                    //Log.d(TAG,"checkc= "+checkc);
                }
            }
        }
        /*else{
            Log.d(TAG,"已到達目的地");
            sw=1;
            routePlanning();
        }*/
    }

    //按下btn_send 發送訊息
    private void sendMessageMethod(){
        btn_sd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            byte[] bytes = ET_msg.getText().toString().getBytes(Charset.defaultCharset());
            //BluetoothConnectionService mBluetoothConnection=new BluetoothConnectionService(transfer_msgActivity.this);
                try{
                    /*if(bytes!=null){*/
                        mBTCnt.write(bytes);
                        ET_msg.getText().clear();
                    //}
                }
                catch(Exception e){
                    e.printStackTrace();
                    showToast("fail to send");
                }
            }
        });
    }

    //初始設定所有按鈕的按下事件
    private void ButtonClickMethod(){
        btn_lt1=findViewById(R.id.locationButton1);
        btn_lt2=findViewById(R.id.locationButton2);
        btn_lt3=findViewById(R.id.locationButton3);
        btn_lt4=findViewById(R.id.locationButton4);
        btn_lt5=findViewById(R.id.locationButton5);
        btn_lt6=findViewById(R.id.locationButton6);
        btn_lt7=findViewById(R.id.locationButton7);
        btn_lt8=findViewById(R.id.locationButton8);
        btn_lt9=findViewById(R.id.locationButton9);
        btn_lt10=findViewById(R.id.locationButton10);
        btn_lt11=findViewById(R.id.locationButton11);
        btn_lt12=findViewById(R.id.locationButton12);
        btn_lt13=findViewById(R.id.locationButton13);


        btn_lt1.setOnClickListener(onClickListener);
        btn_lt2.setOnClickListener(onClickListener);
        btn_lt3.setOnClickListener(onClickListener);
        btn_lt4.setOnClickListener(onClickListener);
        btn_lt5.setOnClickListener(onClickListener);
        btn_lt6.setOnClickListener(onClickListener);
        btn_lt7.setOnClickListener(onClickListener);
        btn_lt8.setOnClickListener(onClickListener);
        btn_lt9.setOnClickListener(onClickListener);
        btn_lt10.setOnClickListener(onClickListener);
        btn_lt11.setOnClickListener(onClickListener);
        btn_lt12.setOnClickListener(onClickListener);
        btn_lt13.setOnClickListener(onClickListener);

    }

    //刷新螢幕鎖定按鈕
    private void ButtonPositioning(){
        /*int[] ClockWiseRoute={1,2,3,4,5,12,11,22,19,6,20,21,10,9,7,8,9,21,22,12,13,14,15};
        int regPositionNow=23,regPositionPast=23;

        for(int i=0;i<23;i++){
            if(card_now==ClockWiseRoute[i]){
                regPositionNow=i;
            }
            if(card_past==ClockWiseRoute[i]){
                regPositionPast=i;
            }
        }
        if((regPositionNow>regPositionPast)){
            if((regPositionNow==22&&regPositionPast==0)){
                position=2;
            }
            else{
                position=1;//順時針
            }
        }
        else if((regPositionNow<regPositionPast)){
            if((regPositionNow==0&&regPositionPast==22)){
                position=1;
            }
            else{
                position=2;//逆時針
            }

        }*/
        //Log.d(TAG,"ButtonPositioning card_now="+card_now);
        //Log.d(TAG,"ButtonPositioning card_past="+card_past);
        //Log.d(TAG,"now position="+position);
        String[][] rt={{"0"},rt1,rt2,rt3,rt4};
        Log.d(TAG,"ButtonPositioning: start_point="+btn_start_point+",goal_point="+btn_goal_point+",routePlanningFL="+Arrays.toString(routePlanningFL));
        if(btn_start_point!=btn_goal_point){
            Log.d(TAG,"ButtonPositioning:尚未到達終點");
            for(int i=1;i<=13;i++){
                switch(i){
                    case(1):{
                        btn_lt1.setSelected((btn_start_point==1||btn_goal_point==1||routePlanningFL[1]==1));
                        break;
                    }
                    case(2):{
                        btn_lt2.setSelected((btn_start_point==2||btn_goal_point==2||routePlanningFL[2]==2));
                        break;
                    }
                    case(3):{
                        btn_lt3.setSelected((btn_start_point==3||btn_goal_point==3||routePlanningFL[3]==3));
                        break;
                    }
                    case(4):{
                        btn_lt4.setSelected((btn_start_point==4||btn_goal_point==4||routePlanningFL[4]==4));
                        break;
                    }
                    case(5):{
                        btn_lt5.setSelected((btn_start_point==5||btn_goal_point==5||routePlanningFL[5]==5));
                        break;
                    }
                    case(6):{
                        btn_lt6.setSelected((btn_start_point==6||btn_goal_point==6||routePlanningFL[6]==6));
                        break;
                    }
                    case(7):{
                        btn_lt7.setSelected((btn_start_point==7||btn_goal_point==7||routePlanningFL[7]==7));
                        break;
                    }
                    case(8):{
                        btn_lt8.setSelected((btn_start_point==8||btn_goal_point==8||routePlanningFL[8]==8));
                        break;
                    }
                    case(9):{
                        btn_lt9.setSelected((btn_start_point==9||btn_goal_point==9||routePlanningFL[9]==9));
                        break;
                    }
                    case(10):{
                        btn_lt10.setSelected((btn_start_point==10||btn_goal_point==10||routePlanningFL[10]==10));
                        break;
                    }
                    case(11):{
                        btn_lt11.setSelected((btn_start_point==11||btn_goal_point==11||routePlanningFL[11]==11));
                        break;
                    }
                    case(12):{
                        btn_lt12.setSelected((btn_start_point==12||btn_goal_point==12||routePlanningFL[12]==12));
                        break;
                    }
                    case(13):{
                        btn_lt13.setSelected((btn_start_point==13||btn_goal_point==13||routePlanningFL[13]==13));
                        break;
                    }
                    default:{
                        break;
                    }
                }
            }
        }
        else if((btn_start_point!=0)&&(btn_goal_point!=0)&&(btn_start_point==btn_goal_point)){
            Log.d(TAG,"ButtonPositioning:到達終點");
            btn_lt1.setSelected(btn_start_point==1);
            btn_lt2.setSelected(btn_start_point==2);
            btn_lt3.setSelected(btn_start_point==3);
            btn_lt4.setSelected(btn_start_point==4);
            btn_lt5.setSelected(btn_start_point==5);
            btn_lt6.setSelected(btn_start_point==6);
            btn_lt7.setSelected(btn_start_point==7);
            btn_lt8.setSelected(btn_start_point==8);
            btn_lt9.setSelected(btn_start_point==9);
            btn_lt10.setSelected(btn_start_point==10);
            btn_lt11.setSelected(btn_start_point==11);
            btn_lt12.setSelected(btn_start_point==12);
            btn_lt13.setSelected(btn_start_point==13);
        }
    }

    //按鈕_位置 按下後 保持選擇狀態 以及設定起點位置及終點位置的旗標
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*int[] cardID={0,R.id.locationButton1,R.id.locationButton2,R.id.locationButton3,R.id.locationButton4,R.id.locationButton5
            ,R.id.locationButton6,R.id.locationButton7,R.id.locationButton8,R.id.locationButton9,R.id.locationButton10,R.id.locationButton11
            ,R.id.locationButton12,R.id.locationButton13,R.id.locationButton14,R.id.locationButton15};*/
            btn_start_point=card_now;
            if (btn_selected_num < 2) {
                if (!(v.isSelected())) {
                    v.setSelected(!v.isSelected());
                    btn_selected_num++;
                    switch (v.getId()) {
                        case R.id.locationButton1:{
                            try {
                                btn_goal_point = 1;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton2:{
                            try {
                                btn_goal_point = 2;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton3:{
                            try {
                                btn_goal_point = 3;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton4:{
                            try {
                                btn_goal_point = 4;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton5:{
                            try {
                                btn_goal_point = 5;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton6:{
                            try {
                                btn_goal_point = 6;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton7:{
                            try {
                                btn_goal_point = 7;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton8:{
                            try {
                                btn_goal_point = 8;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton9:{
                            try {
                                btn_goal_point = 9;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton10:{
                            try {
                                btn_goal_point = 10;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton11:{
                            try {
                                btn_goal_point = 11;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton12:{
                            try {
                                btn_goal_point = 12;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        case R.id.locationButton13:{
                            try {
                                btn_goal_point = 13;
                                routePlanning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        default:{
                            break;
                        }
                    }
                    Log.d(TAG,"btn_selected_num= "+btn_selected_num);
                    Log.d(TAG,"start_point="+btn_start_point+",goal_point="+btn_goal_point);
                    //Handle selected state change
                }
                else if (v.isSelected()) {
                    btn_lt1.setSelected(btn_start_point==1);
                    btn_lt2.setSelected(btn_start_point==2);
                    btn_lt3.setSelected(btn_start_point==3);
                    btn_lt4.setSelected(btn_start_point==4);
                    btn_lt5.setSelected(btn_start_point==5);
                    btn_lt6.setSelected(btn_start_point==6);
                    btn_lt7.setSelected(btn_start_point==7);
                    btn_lt8.setSelected(btn_start_point==8);
                    btn_lt9.setSelected(btn_start_point==9);
                    btn_lt10.setSelected(btn_start_point==10);
                    btn_lt11.setSelected(btn_start_point==11);
                    btn_lt12.setSelected(btn_start_point==12);
                    btn_lt13.setSelected(btn_start_point==13);
                    //Handle de-select state change

                    if(btn_selected_num>1){
                        btn_selected_num=1;
                    }
                    Log.d(TAG,"btn_selected_num= "+btn_selected_num);
                    Log.d(TAG,"start_point="+btn_start_point+",goal_point="+btn_goal_point);

                }
            }
            else if (btn_selected_num >= 2) {
                if (!(v.isSelected())) {
                    v.setSelected(false);
                }
                else if (v.isSelected()) {
                    for(int i=0;i<=13;i++){
                        routePlanningFL[i]=0;
                    }
                    btn_goal_point=0;
                    sw=1;
                    ButtonPositioning();
                    if(btn_selected_num>1){
                        btn_selected_num=1;
                    }

                    //Log.d(TAG,"btn_selected_num= "+ btn_selected_num);
                    //Log.d(TAG,"btn_goal_point= "+ btn_goal_point);
                }
            }
            hintMsgMethod();
        }
    };


    //設定路線規劃提示訊息
    private void hintMsgMethod(){
        if(btn_selected_num==1){
            changemsg(TV_msg,"請選擇終點");
        }
        else if(btn_selected_num==2){
            changemsg(TV_msg,"請選擇路線");
        }
        /*else if(btn_selected_num==2){
            changemsg(TV_msg,"請選擇路線");
        }*/
    }


    //取得起點和終點旗標 開始計算規劃路徑
    private void routePlanning() throws InterruptedException {
        //計算路徑
        String dir[]={"往上" , "往下" , "往左" , "往右","" };
        int[][] num= new int[][]{{0,0,0,0},{2,13,0,16},{3,1,0,0},{4,2,0,0},{5,3,0,18},{6,4,14,11},{14,5,0,0},{15,14,0,0},{9,15,0,0},{0,8,15,10},{9,0,0,0},{12,5,0,17},{13,11,0,0},{1,12,0,0},{7,6,5,15},{8,7,9,14},{0,1,18,17},{0,11,16,18},{0,4,17,16}};
        //			                 0		    1		 2			3	 		4		    5		   6		  7		   8		  	9	         10	         11		    12			13		14		    15			16		  	17		 	18
        int start,fin;
        int first;
        int num2[]={0,0,0,0,0};
        int second,second_2;
        int third,third_2,third_3;
        int forth,forth_2,forth_3,forth_4;
        int fifth,fifth_2,fifth_3,fifth_4,fifth_5;
        int sixth,sixth_2,sixth_3,sixth_4,sixth_5,sixth_6;
        int seventh,seventh_2,seventh_3,seventh_4,seventh_5,seventh_6,seventh_7;
        int position1=4,position2=4,position3=4,position4=4,position5=4,position6=4,position7=4,position8=4;
        int cont=1;
        String direction,direction2;
        String[][] rt={{"0"},rt1,rt2,rt3,rt4};
        //int start,fin;
        start=btn_start_point;
        fin=btn_goal_point;
        for(int i=0;i<=13;i++){
            routePlanningFL[i]=0;
        }
        Log.d(TAG,"重置routePlanningFL");
        //Log.d(TAG,"start= "+start+",fin= "+fin);
        if(start == fin) {
            Log.d(TAG,"不須移動");
            cont=0;
            //rt1=new String[]{""};
            //rt2=new String[]{""};
            //rt3=new String[]{""};
            //rt4=new String[]{""};
            rt[sw]=new String[]{String.valueOf(start)};
        }
        Log.d(TAG,"rt[sw]="+Arrays.toString(rt[sw]));
        for(int i = 0 ; i < 4 ; i ++ ) {
            num2[i] = num[start][i];
        }
        //經過0個點
        if(cont == 1) {
            int rt_count=0;
            for(int i = 0 ; i < 4 ; i ++ ) {
                if(num2[i] == fin) {
                    for(int j = 0 ; j < 4 ; j ++ ) {
                        if(num[fin][j] == start) {
                            if(j == 0) {
                                position1 = 1;
                            } else if(j == 1) {
                                position1 = 0;
                            } else if(j == 2) {
                                position1 = 3;
                            } else if(j == 3) {
                                position1 = 2;
                            }
                        }
                    }
                    //showToast(start +  dir[position1] + fin);
                    //Log.d(TAG,start +  dir[position1] + fin);

                    rt_count++;
                    switch(rt_count){
                        case(1):{
                            rt1=new String[]{String.valueOf(start), dir[position1], String.valueOf(fin)};
                            rt2=new String[]{""};
                            rt3=new String[]{""};
                            rt4=new String[]{""};
                            break;
                        }
                        case(2):{
                            rt2= new String[]{String.valueOf(start), dir[position1], String.valueOf(fin)};
                            rt3=new String[]{""};
                            rt4=new String[]{""};
                            break;
                        }
                        case(3):{
                            rt3= new String[]{String.valueOf(start), dir[position1], String.valueOf(fin)};
                            rt4=new String[]{""};
                            break;
                        }
                        case(4):{
                            rt4= new String[]{String.valueOf(start), dir[position1], String.valueOf(fin)};
                            break;
                        }
                    }
                    //Log.d(TAG, Arrays.toString(rt1));
                    //Log.d(TAG, Arrays.toString(rt2));
                    //Log.d(TAG, Arrays.toString(rt3));
                    //Log.d(TAG, Arrays.toString(rt4));
                    rt_count_out=rt_count;
                    rt_pt_num=2;
                    cont = 0;
                    break;
                }
            }
        }
        //經過1個點
        if(cont == 1) {
            int rt_count=0;
            for(int i = 0 ; i < 4 ; i ++ ) {
                for(int j = 0 ; j < 4 ; j ++ ) {
                    first = num2[i];
                    if(num[first][j] == fin) {
                        for(int k = 0 ; k < 4 ; k ++ ) {
                            if(num[first][k] == start) {
                                if(k == 0) {
                                    position2 = 1;
                                } else if(k == 1) {
                                    position2 = 0;
                                } else if(k == 2) {
                                    position2 = 3;
                                } else if(k == 3) {
                                    position2 = 2;
                                }
                            }
                            if(num[fin][k] == first) {
                                if(k == 0) {
                                    position1 = 1;
                                } else if(k == 1) {
                                    position1 = 0;
                                } else if(k == 2) {
                                    position1 = 3;
                                } else if(k == 3) {
                                    position1 = 2;
                                }
                            }
                        }
                        //cout << start << " ->\t " << first << " ->\t " << fin << "\t車頭方向:" << dir[position] << "\n";
                        //showToast(start + dir[position2] + first + dir[position1] + fin);
                        //Log.d(TAG,start + dir[position2] + first + dir[position1] + fin);

                        rt_count++;
                        switch(rt_count){
                            case(1):{
                                Log.d(TAG,String.valueOf(start)+dir[position2]+String.valueOf(first)+dir[position1]+String.valueOf(fin));
                                rt1= new String[]{String.valueOf(start), dir[position2], String.valueOf(first), dir[position1], String.valueOf(fin)};
                                rt2=new String[]{""};
                                rt3=new String[]{""};
                                rt4=new String[]{""};
                                break;
                            }
                            case(2):{
                                rt2= new String[]{String.valueOf(start), dir[position2], String.valueOf(first), dir[position1], String.valueOf(fin)};
                                rt3=new String[]{""};
                                rt4=new String[]{""};
                                break;
                            }
                            case(3):{
                                rt2= new String[]{String.valueOf(start), dir[position2], String.valueOf(first), dir[position1], String.valueOf(fin)};
                                rt4=new String[]{""};
                                break;
                            }
                            case(4):{
                                rt4= new String[]{String.valueOf(start), dir[position2], String.valueOf(first), dir[position1], String.valueOf(fin)};
                                break;
                            }
                        }
                        //Log.d(TAG, Arrays.toString(rt1));
                        //Log.d(TAG, Arrays.toString(rt2));
                        //Log.d(TAG, Arrays.toString(rt3));
                        //Log.d(TAG, Arrays.toString(rt4));
                        rt_count_out=rt_count;
                        rt_pt_num=3;
                        cont = 0;
                        break;
                    }
                }
            }
        }
        //經過2個點
        if(cont == 1) {
            //Log.d(TAG,"in 經過2個點");
            int rt_count=0;
            for (int i = 0 ; i < 4 ; i ++ ) {
                for(int j = 0 ; j < 4 ; j++ ) {
                    for(int k = 0 ; k < 4 ; k ++ ) {
                        second = num2[i];
                        second_2 = num[second][j];
                        if(num[second_2][k] == fin) {
                            for(int l = 0 ; l < 4 ; l ++ ) {
                                if(num[second][l] == start) {
                                    if(l == 0) {
                                        position3 = 1;
                                    } else if(l == 1) {
                                        position3 = 0;
                                    } else if(l == 2) {
                                        position3 = 3;
                                    } else if(l == 3) {
                                        position3 = 2;
                                    }
                                }
                                if(num[second_2][l] == second) {
                                    if(l == 0) {
                                        position2 = 1;
                                    } else if(l == 1) {
                                        position2 = 0;
                                    } else if(l == 2) {
                                        position2 = 3;
                                    } else if(l == 3) {
                                        position2 = 2;
                                    }
                                }
                                if(num[fin][l] == second_2) {
                                    if(l == 0) {
                                        position1 = 1;
                                    } else if(l == 1) {
                                        position1 = 0;
                                    } else if(l == 2) {
                                        position1 = 3;
                                    } else if(l == 3) {
                                        position1 = 2;
                                    }
                                }
                            }
                            //cout << start << " ->\t " << second << " ->\t " << second_2 << " ->\t " << fin << "\t車頭方向:" << dir[position] << "\n";
                            //showToast(start + dir[position3] + second + dir[position2] + second_2 + dir[position1] + fin);
                            //Log.d(TAG,start + dir[position3] + second + dir[position2] + second_2 + dir[position1] + fin);


                            rt_count++;
                            switch(rt_count){
                                case(1):{
                                    rt1=new String[]{String.valueOf(start), dir[position3], String.valueOf(second), dir[position2],
                                            String.valueOf(second_2), dir[position1], String.valueOf(fin)};
                                    rt2=new String[]{""};
                                    rt3=new String[]{""};
                                    rt4=new String[]{""};
                                    break;
                                }
                                case(2):{
                                    rt2= new String[]{String.valueOf(start), dir[position3], String.valueOf(second), dir[position2],
                                            String.valueOf(second_2), dir[position1], String.valueOf(fin)};
                                    rt3=new String[]{""};
                                    rt4=new String[]{""};
                                    break;
                                }
                                case(3):{
                                    rt3= new String[]{String.valueOf(start), dir[position3], String.valueOf(second), dir[position2],
                                            String.valueOf(second_2), dir[position1], String.valueOf(fin)};
                                    rt4=new String[]{""};
                                    break;
                                }
                                case(4):{
                                    rt4= new String[]{String.valueOf(start), dir[position3], String.valueOf(second), dir[position2],
                                            String.valueOf(second_2), dir[position1], String.valueOf(fin)};
                                    break;
                                }
                            }
                            //Log.d(TAG, Arrays.toString(rt1));
                            //Log.d(TAG, Arrays.toString(rt2));
                            //Log.d(TAG, Arrays.toString(rt3));
                            //Log.d(TAG, Arrays.toString(rt4));
                            rt_count_out=rt_count;
                            rt_pt_num=4;
                            cont = 0;
                            break;
                        }
                    }
                }
            }
        }
        //經過3個點
        if(cont == 1) {
            int rt_count=0;
            for(int i = 0 ; i < 4 ; i ++) {
                for(int j = 0 ; j < 4 ; j ++) {
                    for(int k = 0 ; k < 4 ; k ++) {
                        for(int l = 0 ; l < 4 ; l ++) {
                            third = num2[i];
                            third_2 = num[third][j];
                            third_3 = num[third_2][k];
                            if(num[third_3][l] == fin) {
                                for(int m = 0 ; m < 4 ; m ++ ) {
                                    if(num[third][m] == start) {
                                        if(m == 0) {
                                            position4 = 1;
                                        } else if(m == 1) {
                                            position4 = 0;
                                        } else if(m == 2) {
                                            position4 = 3;
                                        } else if(m == 3) {
                                            position4 = 2;
                                        }
                                    }
                                    if(num[third_2][m] == third) {
                                        if(m == 0) {
                                            position3 = 1;
                                        } else if(m == 1) {
                                            position3 = 0;
                                        } else if(m == 2) {
                                            position3 = 3;
                                        } else if(m == 3) {
                                            position3 = 2;
                                        }
                                    }
                                    if(num[third_3][m] == third_2) {
                                        if(m == 0) {
                                            position2 = 1;
                                        } else if(m == 1) {
                                            position2 = 0;
                                        } else if(m == 2) {
                                            position2 = 3;
                                        } else if(m == 3) {
                                            position2 = 2;
                                        }
                                    }
                                    if(num[fin][m] == third_3) {
                                        if(m == 0) {
                                            position1 = 1;
                                        } else if(m == 1) {
                                            position1 = 0;
                                        } else if(m == 2) {
                                            position1 = 3;
                                        } else if(m == 3) {
                                            position1 = 2;
                                        }
                                    }
                                }
                                //cout << start << " ->\t " << third << " ->\t " << third_2 << " ->\t " << third_3 << " ->\t " << fin << "\t車頭方向:" << dir[position] << "\n";
                                //showToast(start + dir[position4] + third + dir[position3] + third_2 + dir[position2] + third_3 + dir[position1] + fin);
                                Log.d(TAG,start + dir[position4] + third + dir[position3] + third_2 + dir[position2] + third_3 + dir[position1] + fin);

                                rt_count++;
                                switch(rt_count){
                                    case(1):{
                                        rt1= new String[]{String.valueOf(start), dir[position4], String.valueOf(third), dir[position3], String.valueOf(third_2),
                                                dir[position2], String.valueOf(third_3), dir[position1], String.valueOf(fin)};
                                        rt2=new String[]{""};
                                        rt3=new String[]{""};
                                        rt4=new String[]{""};
                                        break;
                                    }
                                    case(2):{
                                        rt2= new String[]{String.valueOf(start), dir[position4], String.valueOf(third), dir[position3], String.valueOf(third_2),
                                                dir[position2], String.valueOf(third_3), dir[position1], String.valueOf(fin)};
                                        rt3=new String[]{""};
                                        rt4=new String[]{""};
                                        break;
                                    }
                                    case(3):{
                                        rt3= new String[]{String.valueOf(start), dir[position4], String.valueOf(third), dir[position3], String.valueOf(third_2),
                                                dir[position2], String.valueOf(third_3), dir[position1], String.valueOf(fin)};
                                        rt4=new String[]{""};
                                        break;
                                    }
                                    case(4):{
                                        rt4= new String[]{String.valueOf(start), dir[position4], String.valueOf(third), dir[position3], String.valueOf(third_2),
                                                dir[position2], String.valueOf(third_3), dir[position1], String.valueOf(fin)};
                                        break;
                                    }
                                }
                                //Log.d(TAG, Arrays.toString(rt1));
                                //Log.d(TAG, Arrays.toString(rt2));
                                //Log.d(TAG, Arrays.toString(rt3));
                                //Log.d(TAG, Arrays.toString(rt4));
                                rt_count_out=rt_count;
                                rt_pt_num=5;
                                cont = 0;
                                break;
                            }
                        }
                    }
                }
            }
        }
        //經過4個點
        if(cont == 1) {
            int rt_count=0;
            for(int i = 0 ; i < 4 ; i ++) {
                for(int j = 0 ; j < 4 ; j ++) {
                    for(int k = 0 ; k < 4 ; k ++) {
                        for(int l = 0 ; l < 4 ; l ++) {
                            for(int m = 0 ; m < 4 ; m ++) {
                                forth = num2[i];
                                forth_2 = num[forth][j];
                                forth_3 = num[forth_2][k];
                                forth_4 = num[forth_3][l];
                                if(num[forth_4][m] == fin) {
                                    for(int n = 0 ; n < 4 ; n ++ ) {
                                        if(num[forth][n] == start) {
                                            if(n == 0) {
                                                position5 = 1;
                                            } else if(n == 1) {
                                                position5 = 0;
                                            } else if(n == 2) {
                                                position5 = 3;
                                            } else if(n == 3) {
                                                position5 = 2;
                                            }
                                        }
                                        if(num[forth_2][n] == forth) {
                                            if(n == 0) {
                                                position4 = 1;
                                            } else if(n == 1) {
                                                position4 = 0;
                                            } else if(n == 2) {
                                                position4 = 3;
                                            } else if(n == 3) {
                                                position4 = 2;
                                            }
                                        }
                                        if(num[forth_3][n] == forth_2) {
                                            if(n == 0) {
                                                position3 = 1;
                                            } else if(n == 1) {
                                                position3 = 0;
                                            } else if(n == 2) {
                                                position3 = 3;
                                            } else if(n == 3) {
                                                position3 = 2;
                                            }
                                        }
                                        if(num[forth_4][n] == forth_3) {
                                            if(n == 0) {
                                                position2 = 1;
                                            } else if(n == 1) {
                                                position2 = 0;
                                            } else if(n == 2) {
                                                position2 = 3;
                                            } else if(n == 3) {
                                                position2 = 2;
                                            }
                                        }
                                        if(num[fin][n] == forth_4) {
                                            if(n == 0) {
                                                position1 = 1;
                                            } else if(n == 1) {
                                                position1 = 0;
                                            } else if(n == 2) {
                                                position1 = 3;
                                            } else if(n == 3) {
                                                position1 = 2;
                                            }
                                        }
                                    }
                                    //cout << start << " ->\t " << forth << " ->\t " << forth_2 << " ->\t " << forth_3 << " ->\t " << forth_4 << " ->\t " << fin << "\t車頭方向:" << dir[position] << "\n";
                                    //showToast(start + dir[position5] + forth + dir[position4] + forth_2 + dir[position3] + forth_3 + dir[position2] + forth_4 + dir[position1] + fin);
                                    Log.d(TAG,start + dir[position5] + forth + dir[position4] + forth_2 + dir[position3] + forth_3 + dir[position2] + forth_4 + dir[position1] + fin);

                                    rt_count++;
                                    switch(rt_count){
                                        case(1):{
                                            rt1= new String[]{String.valueOf(start), dir[position5], String.valueOf(forth), dir[position4], String.valueOf(forth_2),
                                                    dir[position3], String.valueOf(forth_3), dir[position2], String.valueOf(forth_4), dir[position1], String.valueOf(fin)};
                                            rt2=new String[]{""};
                                            rt3=new String[]{""};
                                            rt4=new String[]{""};
                                            break;
                                        }
                                        case(2):{
                                            rt2= new String[]{String.valueOf(start), dir[position5], String.valueOf(forth), dir[position4], String.valueOf(forth_2),
                                                    dir[position3], String.valueOf(forth_3), dir[position2], String.valueOf(forth_4), dir[position1], String.valueOf(fin)};
                                            rt3=new String[]{""};
                                            rt4=new String[]{""};
                                            break;
                                        }
                                        case(3):{
                                            rt3= new String[]{String.valueOf(start), dir[position5], String.valueOf(forth), dir[position4], String.valueOf(forth_2),
                                                    dir[position3], String.valueOf(forth_3), dir[position2], String.valueOf(forth_4), dir[position1], String.valueOf(fin)};
                                            rt4=new String[]{""};
                                            break;
                                        }
                                        case(4):{
                                            rt4= new String[]{String.valueOf(start), dir[position5], String.valueOf(forth), dir[position4], String.valueOf(forth_2),
                                                    dir[position3], String.valueOf(forth_3), dir[position2], String.valueOf(forth_4), dir[position1], String.valueOf(fin)};
                                            break;
                                        }
                                    }
                                    //Log.d(TAG, Arrays.toString(rt1));
                                    //Log.d(TAG, Arrays.toString(rt2));
                                    //Log.d(TAG, Arrays.toString(rt3));
                                    //Log.d(TAG, Arrays.toString(rt4));
                                    rt_count_out=rt_count;
                                    rt_pt_num=6;
                                    cont = 0;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        //經過5個點
        if(cont == 1) {
            int rt_count=0;
            for(int i = 0 ; i < 4 ; i ++) {
                for(int j = 0 ; j < 4 ; j ++) {
                    for(int k = 0 ; k < 4 ; k ++) {
                        for(int l = 0 ; l < 4 ; l ++) {
                            for(int m = 0 ; m < 4 ; m ++) {
                                for(int n = 0 ; n < 4 ; n ++) {
                                    fifth = num2[i];
                                    fifth_2 = num[fifth][j];
                                    fifth_3 = num[fifth_2][k];
                                    fifth_4 = num[fifth_3][l];
                                    fifth_5 = num[fifth_4][m];
                                    if(num[fifth_5][n] == fin) {
                                        for(int o = 0 ; o < 4 ; o ++ ) {
                                            if(num[fifth][o] == start) {
                                                if(o == 0) {
                                                    position6 = 1;
                                                } else if(o == 1) {
                                                    position6 = 0;
                                                } else if(o == 2) {
                                                    position6 = 3;
                                                } else if(o == 3) {
                                                    position6 = 2;
                                                }
                                            }
                                            if(num[fifth_2][o] == fifth) {
                                                if(o == 0) {
                                                    position5 = 1;
                                                } else if(o == 1) {
                                                    position5 = 0;
                                                } else if(o == 2) {
                                                    position5 = 3;
                                                } else if(o == 3) {
                                                    position5 = 2;
                                                }
                                            }
                                            if(num[fifth_3][o] == fifth_2) {
                                                if(o == 0) {
                                                    position4 = 1;
                                                } else if(o == 1) {
                                                    position4 = 0;
                                                } else if(o == 2) {
                                                    position4 = 3;
                                                } else if(o == 3) {
                                                    position4 = 2;
                                                }
                                            }
                                            if(num[fifth_4][o] == fifth_3) {
                                                if(o == 0) {
                                                    position3 = 1;
                                                } else if(o == 1) {
                                                    position3 = 0;
                                                } else if(o == 2) {
                                                    position3 = 3;
                                                } else if(o == 3) {
                                                    position3 = 2;
                                                }
                                            }
                                            if(num[fifth_5][o] == fifth_4) {
                                                if(o == 0) {
                                                    position2 = 1;
                                                } else if(o == 1) {
                                                    position2 = 0;
                                                } else if(o == 2) {
                                                    position2 = 3;
                                                } else if(o == 3) {
                                                    position2 = 2;
                                                }
                                            }
                                            if(num[fin][o] == fifth_5) {
                                                if(o == 0) {
                                                    position1 = 1;
                                                } else if(o == 1) {
                                                    position1 = 0;
                                                } else if(o == 2) {
                                                    position1 = 3;
                                                } else if(o == 3) {
                                                    position1 = 2;
                                                }
                                            }
                                        }
                                        //cout << start << " ->\t " << fifth << " ->\t " << fifth_2 << " ->\t " << fifth_3 << " ->\t " << fifth_4 << " ->\t " << fifth_5 << " ->\t " << fin << "\t車頭方向:" << dir[position] << "\n";
                                        //showToast(start + dir[position6] + fifth + dir[position5] + fifth_2 + dir[position4] + fifth_3 + dir[position3] + fifth_4 + dir[position2] + fifth_5 + dir[position1] + fin);
                                        Log.d(TAG,start + dir[position6] + fifth + dir[position5] + fifth_2 + dir[position4] + fifth_3 + dir[position3] + fifth_4 + dir[position2] + fifth_5 + dir[position1] + fin);

                                        rt_count++;
                                        switch(rt_count){
                                            case(1):{
                                                rt1= new String[]{String.valueOf(start), dir[position6], String.valueOf(fifth), dir[position5], String.valueOf(fifth_2),
                                                        dir[position4], String.valueOf(fifth_3), dir[position3], String.valueOf(fifth_4), dir[position2],
                                                        String.valueOf(fifth_5), dir[position1], String.valueOf(fin)};
                                                rt2=new String[]{""};
                                                rt3=new String[]{""};
                                                rt4=new String[]{""};
                                                break;
                                            }
                                            case(2):{
                                                rt2= new String[]{String.valueOf(start), dir[position6], String.valueOf(fifth), dir[position5], String.valueOf(fifth_2),
                                                        dir[position4], String.valueOf(fifth_3), dir[position3], String.valueOf(fifth_4), dir[position2],
                                                        String.valueOf(fifth_5), dir[position1], String.valueOf(fin)};
                                                rt3=new String[]{""};
                                                rt4=new String[]{""};
                                                break;
                                            }
                                            case(3):{
                                                rt3= new String[]{String.valueOf(start), dir[position6], String.valueOf(fifth), dir[position5], String.valueOf(fifth_2),
                                                        dir[position4], String.valueOf(fifth_3), dir[position3], String.valueOf(fifth_4), dir[position2],
                                                        String.valueOf(fifth_5), dir[position1], String.valueOf(fin)};
                                                rt4=new String[]{""};
                                                break;
                                            }
                                            case(4):{
                                                rt4= new String[]{String.valueOf(start), dir[position6], String.valueOf(fifth), dir[position5], String.valueOf(fifth_2),
                                                        dir[position4], String.valueOf(fifth_3), dir[position3], String.valueOf(fifth_4), dir[position2],
                                                        String.valueOf(fifth_5), dir[position1], String.valueOf(fin)};
                                                break;
                                            }
                                        }
                                        //.d(TAG, Arrays.toString(rt1));
                                        //Log.d(TAG, Arrays.toString(rt2));
                                        //Log.d(TAG, Arrays.toString(rt3));
                                        //Log.d(TAG, Arrays.toString(rt4));
                                        rt_count_out=rt_count;
                                        rt_pt_num=7;
                                        cont = 0;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //經過6個點
        if(cont == 1) {
            int rt_count=0;
            for(int i = 0 ; i < 4 ; i ++) {
                for(int j = 0 ; j < 4 ; j ++) {
                    for(int k = 0 ; k < 4 ; k ++) {
                        for(int l = 0 ; l < 4 ; l ++) {
                            for(int m = 0 ; m < 4 ; m ++) {
                                for(int n = 0 ; n < 4 ; n ++) {
                                    for(int o = 0 ; o < 4 ; o ++) {
                                        sixth = num2[i];
                                        sixth_2 = num[sixth][j];
                                        sixth_3 = num[sixth_2][k];
                                        sixth_4 = num[sixth_3][l];
                                        sixth_5 = num[sixth_4][m];
                                        sixth_6 = num[sixth_5][n];
                                        if(num[sixth_6][o] == fin) {
                                            for(int p = 0 ; p < 4 ; p ++ ) {
                                                if(num[sixth][p] == start) {
                                                    if(p == 0) {
                                                        position7 = 1;
                                                    } else if(p == 1) {
                                                        position7 = 0;
                                                    } else if(p == 2) {
                                                        position7 = 3;
                                                    } else if(p == 3) {
                                                        position7 = 2;
                                                    }
                                                }
                                                if(num[sixth_2][p] == sixth) {
                                                    if(p == 0) {
                                                        position6 = 1;
                                                    } else if(p == 1) {
                                                        position6 = 0;
                                                    } else if(p == 2) {
                                                        position6 = 3;
                                                    } else if(p == 3) {
                                                        position6 = 2;
                                                    }
                                                }
                                                if(num[sixth_3][p] == sixth_2) {
                                                    if(p == 0) {
                                                        position5 = 1;
                                                    } else if(p == 1) {
                                                        position5 = 0;
                                                    } else if(p == 2) {
                                                        position5 = 3;
                                                    } else if(p == 3) {
                                                        position5 = 2;
                                                    }
                                                }
                                                if(num[sixth_4][p] == sixth_3) {
                                                    if(p == 0) {
                                                        position4 = 1;
                                                    } else if(p == 1) {
                                                        position4 = 0;
                                                    } else if(p == 2) {
                                                        position4 = 3;
                                                    } else if(p == 3) {
                                                        position4 = 2;
                                                    }
                                                }
                                                if(num[sixth_5][p] == sixth_4) {
                                                    if(p == 0) {
                                                        position3 = 1;
                                                    } else if(p == 1) {
                                                        position3 = 0;
                                                    } else if(p == 2) {
                                                        position3 = 3;
                                                    } else if(p == 3) {
                                                        position3 = 2;
                                                    }
                                                }
                                                if(num[sixth_6][p] == sixth_5) {
                                                    if(p == 0) {
                                                        position2 = 1;
                                                    } else if(p == 1) {
                                                        position2 = 0;
                                                    } else if(p == 2) {
                                                        position2 = 3;
                                                    } else if(p == 3) {
                                                        position2 = 2;
                                                    }
                                                }
                                                if(num[fin][p] == sixth_6) {
                                                    if(p == 0) {
                                                        position1 = 1;
                                                    } else if(p == 1) {
                                                        position1 = 0;
                                                    } else if(p == 2) {
                                                        position1 = 3;
                                                    } else if(p == 3) {
                                                        position1 = 2;
                                                    }
                                                }
                                            }
                                            //cout << start << " ->\t " << sixth << " ->\t " << sixth_2 << " ->\t " << sixth_3 << " ->\t " << sixth_4 << " ->\t " << sixth_5 << " ->\t " << sixth_6 << " ->\t " << fin << "\t車頭方向:" << dir[position] << "\n";
                                            //showToast(start + dir[position7] + sixth + dir[position6] + sixth_2 + dir[position5] + sixth_3 + dir[position4] + sixth_4 + dir[position3] + sixth_5 + dir[position2] + sixth_6 + dir[position1] + fin);
                                            Log.d(TAG,start + dir[position7] + sixth + dir[position6] + sixth_2 + dir[position5] + sixth_3 + dir[position4] + sixth_4 + dir[position3] + sixth_5 + dir[position2] + sixth_6 + dir[position1] + fin);

                                            rt_count++;
                                            switch(rt_count){
                                                case(1):{
                                                    rt1= new String[]{String.valueOf(start), dir[position7], String.valueOf(sixth), dir[position6], String.valueOf(sixth_2),
                                                            dir[position5], String.valueOf(sixth_3), dir[position4], String.valueOf(sixth_4), dir[position3]
                                                            , String.valueOf(sixth_5), dir[position2], String.valueOf(sixth_6), dir[position1], String.valueOf(fin)};
                                                    rt2=new String[]{""};
                                                    rt3=new String[]{""};
                                                    rt4=new String[]{""};
                                                    break;
                                                }
                                                case(2):{
                                                    rt2= new String[]{String.valueOf(start), dir[position7], String.valueOf(sixth), dir[position6], String.valueOf(sixth_2),
                                                            dir[position5], String.valueOf(sixth_3), dir[position4], String.valueOf(sixth_4), dir[position3]
                                                            , String.valueOf(sixth_5), dir[position2], String.valueOf(sixth_6), dir[position1], String.valueOf(fin)};
                                                    rt3=new String[]{""};
                                                    rt4=new String[]{""};
                                                    break;
                                                }
                                                case(3):{
                                                    rt3= new String[]{String.valueOf(start), dir[position7], String.valueOf(sixth), dir[position6], String.valueOf(sixth_2),
                                                            dir[position5], String.valueOf(sixth_3), dir[position4], String.valueOf(sixth_4), dir[position3]
                                                            , String.valueOf(sixth_5), dir[position2], String.valueOf(sixth_6), dir[position1], String.valueOf(fin)};
                                                    rt4=new String[]{""};
                                                    break;
                                                }
                                                case(4):{
                                                    rt4= new String[]{String.valueOf(start), dir[position7], String.valueOf(sixth), dir[position6], String.valueOf(sixth_2),
                                                            dir[position5], String.valueOf(sixth_3), dir[position4], String.valueOf(sixth_4), dir[position3]
                                                            , String.valueOf(sixth_5), dir[position2], String.valueOf(sixth_6), dir[position1], String.valueOf(fin)};
                                                    break;
                                                }
                                            }
                                            //Log.d(TAG, Arrays.toString(rt1));
                                            //Log.d(TAG, Arrays.toString(rt2));
                                            //Log.d(TAG, Arrays.toString(rt3));
                                            //Log.d(TAG, Arrays.toString(rt4));
                                            rt_count_out=rt_count;
                                            rt_pt_num=8;
                                            cont = 0;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //經過7個點
        if(cont == 1) {
            int rt_count=0;
            for(int i = 0 ; i < 4 ; i ++) {
                for(int j = 0 ; j < 4 ; j ++) {
                    for(int k = 0 ; k < 4 ; k ++) {
                        for(int l = 0 ; l < 4 ; l ++) {
                            for(int m = 0 ; m < 4 ; m ++) {
                                for(int n = 0 ; n < 4 ; n ++) {
                                    for(int o = 0 ; o < 4 ; o ++) {
                                        for(int p = 0 ; p < 4 ; p ++){
                                            seventh = num2[i];
                                            seventh_2 = num[seventh][j];
                                            seventh_3 = num[seventh_2][k];
                                            seventh_4 = num[seventh_3][l];
                                            seventh_5 = num[seventh_4][m];
                                            seventh_6 = num[seventh_5][n];
                                            seventh_7 = num[seventh_6][o];
                                            if(num[seventh_7][p] == fin) {
                                                for(int q = 0 ; q < 4 ; q ++ ) {
                                                    if(num[seventh][q] == start) {
                                                        if(q == 0) {
                                                            position8 = 1;
                                                        } else if(q == 1) {
                                                            position8 = 0;
                                                        } else if(q == 2) {
                                                            position8 = 3;
                                                        } else if(q == 3) {
                                                            position8 = 2;
                                                        }
                                                    }
                                                    if(num[seventh_2][q] == seventh) {
                                                        if(q == 0) {
                                                            position7 = 1;
                                                        } else if(q == 1) {
                                                            position7 = 0;
                                                        } else if(q == 2) {
                                                            position7 = 3;
                                                        } else if(q == 3) {
                                                            position7 = 2;
                                                        }
                                                    }
                                                    if(num[seventh_3][q] == seventh_2) {
                                                        if(q == 0) {
                                                            position6 = 1;
                                                        } else if(q == 1) {
                                                            position6 = 0;
                                                        } else if(q == 2) {
                                                            position6 = 3;
                                                        } else if(q == 3) {
                                                            position6 = 2;
                                                        }
                                                    }
                                                    if(num[seventh_4][q] == seventh_3) {
                                                        if(q == 0) {
                                                            position5 = 1;
                                                        } else if(q == 1) {
                                                            position5 = 0;
                                                        } else if(q == 2) {
                                                            position5 = 3;
                                                        } else if(q == 3) {
                                                            position5 = 2;
                                                        }
                                                    }
                                                    if(num[seventh_5][q] == seventh_4) {
                                                        if(q == 0) {
                                                            position4 = 1;
                                                        } else if(q == 1) {
                                                            position4 = 0;
                                                        } else if(q == 2) {
                                                            position4 = 3;
                                                        } else if(q == 3) {
                                                            position4 = 2;
                                                        }
                                                    }
                                                    if(num[seventh_6][q] == seventh_5) {
                                                        if(q == 0) {
                                                            position3 = 1;
                                                        } else if(q == 1) {
                                                            position3 = 0;
                                                        } else if(q == 2) {
                                                            position3 = 3;
                                                        } else if(q == 3) {
                                                            position3 = 2;
                                                        }
                                                    }
                                                    if(num[seventh_7][q] == seventh_6) {
                                                        if(q == 0) {
                                                            position2 = 1;
                                                        } else if(q == 1) {
                                                            position2 = 0;
                                                        } else if(q == 2) {
                                                            position2 = 3;
                                                        } else if(q == 3) {
                                                            position2 = 2;
                                                        }
                                                    }
                                                    if(num[fin][q] == seventh_7) {
                                                        if(q == 0) {
                                                            position1 = 1;
                                                        } else if(q == 1) {
                                                            position1 = 0;
                                                        } else if(q == 2) {
                                                            position1 = 3;
                                                        } else if(q == 3) {
                                                            position1 = 2;
                                                        }
                                                    }
                                                }

                                                //cout << start << " ->\t " << seventh << " ->\t " << seventh_2 << " ->\t " << seventh_3 << " ->\t " << seventh_4 << " ->\t " << seventh_5 << " ->\t " << seventh_6 << " ->\t " << seventh_7 << " ->\t " << fin << "\t車頭方向:" << dir[position] << "\n";
                                                //showToast(start + dir[position8] + seventh + dir[position7] + seventh_2 + dir[position6] + seventh_3 + dir[position5] + seventh_4 + dir[position4] + seventh_5 + dir[position3] + seventh_6 + dir[position2] + seventh_7 + dir[position1] + fin);
                                                Log.d(TAG,start + dir[position8] + seventh + dir[position7] + seventh_2 + dir[position6] + seventh_3 + dir[position5] + seventh_4 + dir[position4] + seventh_5 + dir[position3] + seventh_6 + dir[position2] + seventh_7 + dir[position1] + fin);

                                                rt_count++;
                                                switch(rt_count){
                                                    case(1):{
                                                        rt1= new String[]{String.valueOf(start), dir[position8], String.valueOf(seventh), dir[position7], String.valueOf(seventh_2)
                                                                , dir[position6], String.valueOf(seventh_3), dir[position5], String.valueOf(seventh_4), dir[position4], String.valueOf(seventh_5), dir[position3]
                                                                , String.valueOf(seventh_6), dir[position2], String.valueOf(seventh_7), dir[position1], String.valueOf(fin)};
                                                        rt2=new String[]{""};
                                                        rt3=new String[]{""};
                                                        rt4=new String[]{""};
                                                        break;
                                                    }
                                                    case(2):{
                                                        rt2= new String[]{String.valueOf(start), dir[position8], String.valueOf(seventh), dir[position7], String.valueOf(seventh_2)
                                                                , dir[position6], String.valueOf(seventh_3), dir[position5], String.valueOf(seventh_4), dir[position4], String.valueOf(seventh_5), dir[position3]
                                                                , String.valueOf(seventh_6), dir[position2], String.valueOf(seventh_7), dir[position1], String.valueOf(fin)};
                                                        rt3=new String[]{""};
                                                        rt4=new String[]{""};
                                                        break;
                                                    }
                                                    case(3):{
                                                        rt3= new String[]{String.valueOf(start), dir[position8], String.valueOf(seventh), dir[position7], String.valueOf(seventh_2)
                                                                , dir[position6], String.valueOf(seventh_3), dir[position5], String.valueOf(seventh_4), dir[position4], String.valueOf(seventh_5), dir[position3]
                                                                , String.valueOf(seventh_6), dir[position2], String.valueOf(seventh_7), dir[position1], String.valueOf(fin)};
                                                        rt4=new String[]{""};
                                                        break;
                                                    }
                                                    case(4):{
                                                        rt4= new String[]{String.valueOf(start), dir[position8], String.valueOf(seventh), dir[position7], String.valueOf(seventh_2)
                                                                , dir[position6], String.valueOf(seventh_3), dir[position5], String.valueOf(seventh_4), dir[position4], String.valueOf(seventh_5), dir[position3]
                                                                , String.valueOf(seventh_6), dir[position2], String.valueOf(seventh_7), dir[position1], String.valueOf(fin)};
                                                        break;
                                                    }
                                                }
                                                //Log.d(TAG, Arrays.toString(rt1));
                                                //Log.d(TAG, Arrays.toString(rt2));
                                                //Log.d(TAG, Arrays.toString(rt3));
                                                //Log.d(TAG, Arrays.toString(rt4));
                                                rt_count_out=rt_count;
                                                rt_pt_num=9;
                                                cont = 0;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(cont == 1){
           Log.d(TAG,"route planning error") ;
            //cout << "error\n";
        }


        //取得路徑中的所有按鈕並鎖定
        rt=new String[][]{{"0"},rt1,rt2,rt3,rt4};
        Log.d(TAG,"rt1="+Arrays.toString(rt1));
        Log.d(TAG,"rt1="+Arrays.toString(rt2));
        Log.d(TAG,"rt1="+Arrays.toString(rt3));
        Log.d(TAG,"rt1="+Arrays.toString(rt4));
        Log.d(TAG,"rt1="+Arrays.toString(rt[sw])+",sw= "+sw);
        for(int i=0;i<rt[sw].length;i+=2){
            switch (Integer.parseInt(rt[sw][i])){
                case(1):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==1));
                    //btn_lt1.setSelected(Integer.parseInt(rt[sw][i])==1);
                    routePlanningFL[1]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(2):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==2));
                    //btn_lt2.setSelected(Integer.parseInt(rt[sw][i])==2);
                    routePlanningFL[2]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(3):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==3));
                    //btn_lt3.setSelected(Integer.parseInt(rt[sw][i])==3);
                    routePlanningFL[3]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(4):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==4));
                    //btn_lt4.setSelected(Integer.parseInt(rt[sw][i])==4);
                    routePlanningFL[4]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(5):{
                    //(TAG,"5");
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==5));
                    //btn_lt5.setSelected(Integer.parseInt(rt[sw][i])==5);
                    routePlanningFL[5]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(6):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==6));
                    //btn_lt6.setSelected(Integer.parseInt(rt[sw][i])==6);
                    routePlanningFL[6]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(7):{
                    //Log.d(TAG,"7");
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==7));
                    //btn_lt7.setSelected(Integer.parseInt(rt[sw][i])==7);
                    routePlanningFL[7]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(8):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==8));
                    //btn_lt8.setSelected(Integer.parseInt(rt[sw][i])==8);
                    routePlanningFL[8]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(9):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==9));
                    //btn_lt9.setSelected(Integer.parseInt(rt[sw][i])==9);
                    routePlanningFL[9]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(10):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==10));
                    //btn_lt10.setSelected(Integer.parseInt(rt[sw][i])==10);
                    routePlanningFL[10]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(11):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==11));
                    //btn_lt11.setSelected(Integer.parseInt(rt[sw][i])==11);
                    routePlanningFL[11]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(12):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==12));
                    //btn_lt12.setSelected(Integer.parseInt(rt[sw][i])==12);
                    routePlanningFL[12]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                case(13):{
                    //Log.d(TAG,"in case: "+ String.valueOf(Integer.parseInt(rt[sw][i]))+String.valueOf(Integer.parseInt(rt[sw][i])==13));
                    //btn_lt13.setSelected(Integer.parseInt(rt[sw][i])==13);
                    routePlanningFL[13]=Integer.parseInt(rt[sw][i]);
                    break;
                }
                default:{
                    //Log.d(TAG,"rt[sw][i]: "+ String.valueOf(Integer.parseInt(rt[sw][i])));
                    break;
                }
            }
        }
        ButtonPositioning();

    }

    //按鈕_切換路線
    private void swRouteMethod(){
        btn_sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[][] rt={{"0"},rt1,rt2,rt3,rt4};
                int routeNum=1;
                Log.d(TAG,"進swRouteMethod");
                if(rt4.length>1){
                    routeNum=4;
                }
                else if(rt3.length>1){
                    routeNum=3;
                }
                else if(rt2.length>1){
                    routeNum=2;
                }
                //Log.d(TAG,"routenum="+routeNum);
                //Log.d(TAG,"rt4.length="+rt4[0]);
                //Log.d(TAG,"rt3.length="+rt3[0]);
                //Log.d(TAG,"rt2.length="+rt2[0]);
                //Log.d(TAG,"rt1.length="+rt1[0]);



                if(sw<routeNum){
                    sw++;
                }
                else{
                    sw=1;
                }
                try {
                    routePlanning();
                    ButtonPositioning();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ButtonPositioning();
            }
        });
    }


    //按鈕_確定路線(selectRoute) 按下後 執行轉換規劃路徑
    private void determineRouteMethod() {
        btn_dr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_selected_num >= 2) {
                    try {
                        buttonSelectedByRouteMethod();
                        ButtonPositioning();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                btn_selected_num=1;
            }
        });
    }

    //轉換規劃路徑
    private void buttonSelectedByRouteMethod() throws InterruptedException {
        String dir[]={"直走" , "下改" , "左改" , "右改"};
        String[] regrt = new String[4];
        int setChangeFL=0;
        trCoordinate=regtrCoordinate;
        /*int[][] trcoordinate={{}};
        trcoordinate= new int[][]{{0,0,0,0},{2,15,0,16},{3,0,0,1},{4,2,0,0},{5,3,0,18},{19,0,4,12},{20,19,0,0},{0,20,8,9},{0,0,7,9},{8,21,7,10},{9,21,0,0},{22,12,0,0},{22,13,5,11},{12,14,17,0},{13,15,0,0},{14,0,1,0},{0,1,18,17},{18,16,0,13},{17,16,4,0},{20,5,6,22},{7,19,6,21},{9,22,20,10},{21,12,19,11}};
        //				    0		    1		     2			3	 		4		   5		 	6		  7		    8		  	9		   10	        11		    12			13			14			15			16		  	17		 	18			19		  	20		 	21			 	22
        //16=A 17=B 18=C 19=D 20=E 21=F 22=G
        //rt_dirin=1;
        //card_now=1;
        //card_past=15;
        if(rt_dirin==1){
            num2= new int[][]{{0,0,0,0},{2,15,0,16},{3,0,0,1},{4,2,0,0},{5,3,0,18},{12,4,19,0},{20,19,0,0},{8,9,20,0},{9,7,0,0},{7,10,21,8},{9,21,0,0},{22,12,0,0},{11,5,22,13},{14,12,0,17},{15,13,0,0},{1,14,0,0},{0,1,18,17},{0,13,16,18},{0,4,17,16},{6,22,5,20},{21,6,7,19},{10,20,9,22},{19,11,12,21}};
            //				        0		    1		         2			    3	 		4		        5		 	        6		     7		         8		  9{21 8 10 7}	      10	         11		        12			        13			14			        15			16		        	17		    	18			    19		     	20		 	    21			 	22
            //16=A 17=B 18=C 19=D 20=E 21=F 22=G
            fl1=1;
            /*if((card_now==9&&card_past==8)||(card_now==21&&card_past==9)||(card_now==22&&card_past==21)||(card_now==12&&card_past==22)){
                num[9][0]=21;
                num[9][1]=8;
                num[9][2]=10;
                num[9][3]=7;

                num[21][0]=22;
                num[21][1]=9;
                num[21][2]=10;
                num[21][3]=20;

                num[22][0]=12;
                num[22][1]=21;
                num[22][2]=11;
                num[22][3]=19;

                num[12][0]=13;
                num[12][1]=22;
                num[12][2]=11;
                num[12][3]=5;
            }
        }
        /*else if(rt_dirin==2){
            num= new int[][]{{0, 0, 0, 0}, {15, 2, 16, 0}, {3, 0, 0, 1}, {4, 2, 0, 0}, {5, 3, 0, 18}, {12, 4, 19, 0}, {20, 19, 0, 0}, {8, 9, 20, 0}, {9, 7, 0, 0}, {7, 10, 21, 8}, {9, 21, 0, 0}, {22, 12, 0, 0}, {11, 5, 22, 13}, {14, 12, 0, 17}, {13, 15, 0, 0}, {14, 1, 0, 0}, {0, 1, 18, 17}, {0, 13, 16, 18}, {0, 4, 17, 16}, {6, 22, 5, 20}, {21, 6, 7, 19}, {10, 20, 9, 22}, {19, 11, 12, 21}};
            //				        0		    1		         2			    3	 		4		        5		 	        6		     7		         8		  9{21 8 10 7}	      10	         11		        12			        13			14			        15			16		        	17		    	18			    19		     	20		 	    21			 	22
            //16=A 17=B 18=C 19=D 20=E 21=F 22=G
        }*/

        //
        String[][] rt={{"0"},rt1,rt2,rt3,rt4};
        Log.d(TAG, "替換前 "+Arrays.toString(rt[sw]));

        //根據初始車頭方向 轉換第一個位置的座標方向
        for(int j=0;j<=3;j++){
            if(trCoordinate[Integer.parseInt(rt[sw][0])][j]==card_past){
                setChangeFL=1;
                if(j==0){
                    regrt[0] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][1]);
                    regrt[1] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][0]);
                    regrt[2] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][3]);
                    regrt[3] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][2]);
                }
                else if(j==1){
                    regrt[0] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][0]);
                    regrt[1] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][1]);
                    regrt[2] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][2]);
                    regrt[3] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][3]);
                }
                else if(j==2){
                    regrt[0] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][3]);
                    regrt[1] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][2]);
                    regrt[2] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][0]);
                    regrt[3] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][1]);
                }
                else if(j==3){
                    regrt[0] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][2]);
                    regrt[1] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][3]);
                    regrt[2] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][1]);
                    regrt[3] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][0])][0]);
                }
            }
        }
        if(setChangeFL==1){
            trCoordinate[Integer.parseInt(rt[sw][0])][0]= Integer.parseInt(regrt[0]);
            trCoordinate[Integer.parseInt(rt[sw][0])][1]= Integer.parseInt(regrt[1]);
            trCoordinate[Integer.parseInt(rt[sw][0])][2]= Integer.parseInt(regrt[2]);
            trCoordinate[Integer.parseInt(rt[sw][0])][3]= Integer.parseInt(regrt[3]);
            setChangeFL=0;
        }
        Log.d(TAG, "替換第一項= "+ Arrays.toString(trCoordinate[Integer.parseInt(rt[sw][0])]));

        //轉換剩下位置的座標方向
        for(int i=0;i<rt_pt_num-1;i++){
            for(int j=0;j<=3;j++){
                if(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][j]==Integer.parseInt(rt[sw][2*i])){
                    if(j==0){
                        regrt[0] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][1]);
                        regrt[1] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][0]);
                        regrt[2] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][3]);
                        regrt[3] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][2]);
                    }
                    else if(j==1){
                        regrt[0] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][0]);
                        regrt[1] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][1]);
                        regrt[2] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][2]);
                        regrt[3] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][3]);
                    }
                    else if(j==2){
                        regrt[0] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][3]);
                        regrt[1] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][2]);
                        regrt[2] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][0]);
                        regrt[3] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][1]);
                    }
                    else if(j==3){
                        regrt[0] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][2]);
                        regrt[1] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][3]);
                        regrt[2] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][1]);
                        regrt[3] = String.valueOf(trCoordinate[Integer.parseInt(rt[sw][2*i+2])][0]);
                    }
                }
                if(trCoordinate[Integer.parseInt(rt[sw][2*i])][j]==Integer.parseInt(rt[sw][2*i+2])){
                    if(j==0){
                        rt[sw][2*i+1]= dir[0];
                    }
                    else if(j==1){
                        rt[sw][2*i+1]= dir[1];
                    }
                    else if(j==2){
                        rt[sw][2*i+1]= dir[2];
                    }
                    else if(j==3){
                        rt[sw][2*i+1]= dir[3];
                    }
                }
            }
            trCoordinate[Integer.parseInt(rt[sw][2*i+2])][0]= Integer.parseInt(regrt[0]);
            trCoordinate[Integer.parseInt(rt[sw][2*i+2])][1]= Integer.parseInt(regrt[1]);
            trCoordinate[Integer.parseInt(rt[sw][2*i+2])][2]= Integer.parseInt(regrt[2]);
            trCoordinate[Integer.parseInt(rt[sw][2*i+2])][3]= Integer.parseInt(regrt[3]);

            //z//
            //card_now=Integer.parseInt(rt[sw][2*(rt_pt_num-2)+2]);
            //card_past=Integer.parseInt(rt[sw][2*(rt_pt_num-2)]);
        }
        /*for(int i=0;i<22;i++){
            Log.d(TAG, Arrays.toString(num2[i]));
        }*/
        Log.d(TAG, "替換後 " + Arrays.toString(rt[sw]));

        byte[] bytes={};
        for(int i=0;i<rt[sw].length;i++){
            if(rt[sw][i].equals("直走")){
                rt[sw][i]="31";
            }
            else if(rt[sw][i].equals("下改")){
                rt[sw][i]="32";
            }
            else if(rt[sw][i].equals("左改")){
                rt[sw][i]="33";
            }
            else if(rt[sw][i].equals("右改")){
                rt[sw][i]="34";
            }
            else if(rt[sw][i].length()<2){
                rt[sw][i]="0"+rt[sw][i];
            }
        }
        writeRouteMethod();
    }
    //送出路線
    private void writeRouteMethod(){
        byte[] bytes={};
        String[][] rt={{"0"},rt1,rt2,rt3,rt4};
        if(!(btn_start_point==btn_goal_point)){
            for(int i=ifright;i<=ifright+2;i++){
                bytes=rt[sw][i].getBytes();
                mBTCnt.write(bytes);
            }
        }
    }
    //替換TextView的訊息
    public void changemsg(View view,String msg) {
        ((TextView)findViewById(R.id.textView)).setText(msg);
    }

    //廣播 接收訊息
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            int setc1=0,setc2=0,setw=0,index=0;
            //String[] regString1={};
            //String[] regString2={};
            //String[] regcontent={};

            String content = intent.getStringExtra("content");

            regcontent+=content;
            if(regcontent.contains("s")){
                Log.d(TAG,"regcontent="+regcontent);
                regcontent=("");
            }
            else {
                try{
                    if(inistart==0){
                        if(regcontent.length()>=4){
                            Log.d(TAG,"stream(initial)= "+regcontent);
                            if(!(regcontent.substring(0,2)==regcontent.substring(2,4))){
                                regString1=regcontent.substring(0,2);
                                regString2=regcontent.substring(2,4);
                                card_past=Integer.parseInt(regString1);
                                card_now=Integer.parseInt(regString2);
                                Log.d(TAG,"card_past(initial)= "+card_past);
                                Log.d(TAG,"card_now(initial)= "+card_now);
                            }
                            regcontent=("");
                        }
                    }
                    else{
                        if(regcontent.length()>=2){
                            Log.d(TAG,"stream= "+regcontent);
                            if(!(Integer.parseInt(regcontent.substring(0,2))==card_now)){
                                regString1=regcontent.substring(0,2);
                                card_past=card_now;
                                card_now=Integer.parseInt(regString1);
                                Log.d(TAG,"card_past= "+card_past);
                                Log.d(TAG,"card_now= "+card_now);
                                try {
                                    Log.d(TAG,"AGAIN");
                                    againMethod();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            regcontent=("");
                            btn_start_point=card_now;
                        }
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }



            TV_msg.setText("現在地點: "+ card_now);
            if(card_past>0&&card_now>0&&inistart==0){
                inistart=1;
                Log.d(TAG,"INITIAL");
                initialMethod();
            }
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"NowState: onPause");
        byte[] end="e".getBytes(StandardCharsets.US_ASCII);
        mBTCnt.write(end);
        unregisterReceiver(broadcastReceiver);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"NowState: onResume");
        if(broadcastReceiver==null){
            //LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(ACTION_TEXT_CHANGED));
        }
    }

    /*public class MyReceiver extends BroadcastReceiver {
                public MyReceiver() {

                }
                @Override
                public void onReceive(Context context, Intent intent) {
                    ObservableObject.getInstance().updateValue(intent);
                }
            }*/
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

