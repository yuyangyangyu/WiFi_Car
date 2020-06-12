package com.example.wifi_car;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity<speed_list> extends AppCompatActivity {

    private WifiManager wifiManager = null;
    private WifiInfo wifiInfo = null;
    final Handler handler = new Handler();//信号强度多线程
    final Handler handler_state = new Handler();//TCP连接状态
    private TextView Signal_value;//WiFi信号强度
    private TextView Speed_value;//速度
    private ImageView Signal_Image;//信号图标
    private Button Up;//向前
    private Button Down;//向后
    private Button Left;//向左
    private Button Right;//向右
    private Button Connect;//连接
    private ImageView Change_Speed;//变速
    private TextView Gears_value;//变档值


    byte[] buff_up={0x7f,0x03,0x02,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01};//向前
    byte[] buff_stop={0x7f,0x03,0x00,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01};//停止
    byte[] buff_down={0x7f,0x03,0x03,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01};//后退

    byte[] buff_speed={0x7f,0x03,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01};//改变速度

    byte[] buff_left={0x7f,0x03,0x04,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01};//左转
    byte[] buff_right={0x7f,0x03,0x05,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01,0x01};//右转

    private boolean Isconnect = false;
    private int Gears = 0;
    private final int[] speed_list={R.drawable.speed_0,R.drawable.speed_1,R.drawable.speed_2,R.drawable.speed_3,
    R.drawable.speed_4,R.drawable.speed_5};

    //前进后退
    private boolean Is_Up=false;
    private boolean Is_Down=false;

    private boolean Recvice_message=false;
    private String message;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//设置屏幕为横屏, 设置后会锁定方向
        Signal_value = findViewById(R.id.signal_value);
        Speed_value = findViewById(R.id.speed_value);
        Signal_Image = findViewById(R.id.signal);
        //方向控制
        Up = findViewById(R.id.Up);
        Down = findViewById(R.id.Down);
        Left = findViewById(R.id.Left);
        Right = findViewById(R.id.Right);
        Connect = findViewById(R.id.connect);
        Change_Speed = findViewById(R.id.change_speed);
        Gears_value = findViewById(R.id.gears_value);








        Task_Socket.share_socket().setDisconnectedCallback(new Task_Socket.OnServerDisconnectedCallbackBlock() {
            @Override
            public void callback(IOException e) {
                Toast.makeText(getApplicationContext(),"小车断开连接或连接失败",Toast.LENGTH_SHORT).show();
                Isconnect = false;
            }
        });
        Task_Socket.share_socket().setConnectedCallback(new Task_Socket.OnServerConnectedCallbackBlock() {
            @Override
            public void callback() {
                Toast.makeText(getApplicationContext(),"小车连接成功",Toast.LENGTH_SHORT).show();
                Isconnect = true;
            }
        });
        Task_Socket.share_socket().setReceivedCallback(new Task_Socket.OnReceiveCallbackBlock() {
            @Override
            public void callback(String receicedMessage) {
                //接受到信息
                Log.i("qqq",receicedMessage);
                Recvice_message=true;
                message=receicedMessage;

            }
        });









        //向前
        Up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action==MotionEvent.ACTION_DOWN)
                {
                    Log.d("aaa","按下-UP");
                    Is_Up=true;
                    if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected())
                        Task_Socket.share_socket().Send(buff_up);
                }
                else if(action==MotionEvent.ACTION_UP)
                {
                    Log.d("aaa","松开-UP");
                    Is_Up=false;
                    if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected())
                        Task_Socket.share_socket().Send(buff_stop);
                }
                return false;
            }
        });
        //向后
        Down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action==MotionEvent.ACTION_DOWN)
                {
                    Log.d("aaa","按下-Down");
                    Is_Down=true;
                    if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected())
                        Task_Socket.share_socket().Send(buff_down);
                }
                else if(action==MotionEvent.ACTION_UP)
                {
                    Log.d("aaa","松开-Down");
                    Is_Down=false;
                    if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected())
                        Task_Socket.share_socket().Send(buff_stop);
                }
                return false;
            }
        });
        //向左
        Left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action==MotionEvent.ACTION_DOWN)
                {
                    Log.d("aaa","按下-Left");
                    if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected())
                    {
                        if(Is_Up)
                        {
                            buff_left[3]=0x01;
                            buff_left[4]= (byte)(Gears+2);
                            Task_Socket.share_socket().Send(buff_left);
                        }
                        else if(Is_Down)
                        {
                            buff_left[3]=0x02;
                            buff_left[4]= (byte) (Gears+2);
                            Task_Socket.share_socket().Send(buff_left);
                        }

                    }
                    //Task_Socket.share_socket().send("向左".getBytes());
                }
                else if(action==MotionEvent.ACTION_UP)
                {
                    Log.d("aaa","松开-Left");
                    if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected()){
                        if(Is_Up)
                            Task_Socket.share_socket().Send(buff_up);
                        else if(Is_Down)
                            Task_Socket.share_socket().Send(buff_down);
                        else
                            Task_Socket.share_socket().Send(buff_stop);
                    }
                }
                return false;
            }
        });
        //向右
        Right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if(action==MotionEvent.ACTION_DOWN)
                {
                    Log.d("aaa","按下-Right");
                    if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected())
                    {
                        if(Is_Up)
                        {
                            buff_right[3]=0x01;
                            buff_right[4]= (byte) (Gears+2);
                            Task_Socket.share_socket().Send(buff_right);
                        }
                        else if(Is_Down)
                        {
                            buff_right[3]=0x02;
                            buff_right[4]= (byte) (Gears+2);
                            Task_Socket.share_socket().Send(buff_right);
                        }
                    }
                    //Task_Socket.share_socket().send("向右".getBytes());
                }
                else if(action==MotionEvent.ACTION_UP)
                {
                    Log.d("aaa","松开-Right");
                    if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected())
                    {
                        if(Is_Up)
                            Task_Socket.share_socket().Send(buff_up);
                        else if(Is_Down)
                            Task_Socket.share_socket().Send(buff_down);
                        else
                            Task_Socket.share_socket().Send(buff_stop);
                    }
                }
                return false;
            }
        });

        //变速
        Change_Speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //改变速度
                if(Task_Socket.share_socket().socket!=null&&Task_Socket.share_socket().socket.isConnected()) {
                    Gears++;
                    if (Gears == 6)
                        Gears = 0;
                    buff_speed[3] = (byte) Gears;
                    Change_Speed.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), speed_list[Gears]));
                    Gears_value.setText(String.valueOf(Gears) + "档");
                    Task_Socket.share_socket().Send(buff_speed);
                }
                else
                    Toast.makeText(getApplicationContext(),"请先连接小车",Toast.LENGTH_SHORT).show();
            }
        });



        //连接到小车
        Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Isconnect)
                    Task_Socket.share_socket().Connect("192.168.4.1",7676);
                else
                    Task_Socket.share_socket().disconnect();
            }
        });

        if(isWifiConnect())
        {
            //Toast.makeText(getApplicationContext(),"111",Toast.LENGTH_LONG).show();
            //wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
            handler.postDelayed(runnable,200);
            handler_state.postDelayed(runnable_state,1500);

        }
        else
            Toast.makeText(getApplicationContext(),"WiFi未连接！",Toast.LENGTH_SHORT).show();

    }
    //判断WiFi是否连接
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    //设置WiFi监听广播
    protected BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction()))
            {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        Toast.makeText(getApplicationContext(),"WiFi已断开!",Toast.LENGTH_SHORT).show();
                        handler.removeCallbacks(runnable);//关闭多线程
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Toast.makeText(getApplicationContext(),"WiFi已连接!",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    //定时器
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(wifiManager==null)
            {
                wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
                wifiInfo = wifiManager.getConnectionInfo();
            }
            else
            {
                wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
                wifiInfo = wifiManager.getConnectionInfo();
                int rssi = wifiInfo.getRssi();
                //Toast.makeText(getApplicationContext(),String.valueOf(wifiInfo.getRssi()),Toast.LENGTH_LONG).show();
                Signal_value.setText("信号:"+String.valueOf(rssi)+"dB");
                if(rssi>-70)
                    Signal_Image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.signal_has));//有信号
                else
                    Signal_Image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.no_signal));//信号弱

            }
            if(Recvice_message)
            {
                Recvice_message=false;
                Speed_value.setText("速度:"+message+"m/s");
            }
            handler.postDelayed(this,200);
        }
    };
    //连接小车
    Runnable runnable_state=new Runnable() {
        @Override
        public void run() {
            if(Isconnect)
                Connect.setBackgroundResource(R.drawable.connect);
            else {
                Connect.setBackgroundResource(R.drawable.discinnect);
                Gears=0;
                Change_Speed.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), speed_list[0]));
                Gears_value.setText("0档");
            }
            handler_state.postDelayed(this,1500);//1.5s
        }
    };




}