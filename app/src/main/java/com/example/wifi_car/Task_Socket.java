package com.example.wifi_car;

import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Task_Socket {
    private static Task_Socket instance;
    private static final String TAG = "TASK_SOCKET";
    public Socket socket;//socket
    private String IP_Address;//ip地址
    private int Port;//端口信息
    private Thread thread;
    private OutputStream outputStream;//输出流
    private InputStream inputStream;//输入流

    //    连接回调
    private OnServerConnectedCallbackBlock connectedCallback;
    //    断开连接回调(连接失败)
    private OnServerDisconnectedCallbackBlock disconnectedCallback;
    //    接收信息回调
    private OnReceiveCallbackBlock receivedCallback;


    private Task_Socket()
    {
        super();
    }
    //全局静态方法
    public static Task_Socket share_socket()
    {
        if(instance==null)
        {
            synchronized (Task_Socket.class)
            {
                if(instance==null)
                    instance = new Task_Socket();
            }
        }
        return instance;
    }
    //连接
    public void Connect(final String IP_Address,final int Port)
    {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    socket = new Socket(IP_Address,Port);
                    if(isConnected())
                    {
                        Task_Socket.share_socket().IP_Address = IP_Address;
                        Task_Socket.share_socket().Port = Port;
                        if(connectedCallback!=null)
                            connectedCallback.callback();
                        outputStream = socket.getOutputStream();
                        inputStream = socket.getInputStream();
                        receive();
                        Log.i(TAG,"success");
                    }
                    else
                    {
                        Log.i(TAG,"failed");
                        if(disconnectedCallback!=null)
                            disconnectedCallback.callback(new IOException("FAILED"));
                    }


                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    if (disconnectedCallback!=null)
                        disconnectedCallback.callback(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG,"ERROR");
                    if (disconnectedCallback!=null)
                        disconnectedCallback.callback(e);
                }
                Looper.loop();
            }
        });
        thread.start();
    }

    //是否连接
    public boolean isConnected()
    {
        return socket.isConnected();
    }
    /**
     * 连接
     */
    public void Connect() {
        Connect(IP_Address,Port);
    }
    /**
     * 断开连接
     */
    public void disconnect() {
        if (isConnected()) {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                socket.close();
                if (socket.isClosed()) {
                    if (disconnectedCallback != null) {
                        disconnectedCallback.callback(new IOException("断开连接"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 接收数据
     */
    public void receive() {
        while (isConnected()) {
            try {

                /**得到的是16进制数，需要进行解析*/
                byte[] bt = null;
                bt = new byte[1024];
                byte[] bs = new byte[0];
//                获取接收到的字节和字节数
                int length = inputStream.read(bt);
//                获取正确的字节
                if(length>0) {
                    bs = new byte[length];
                    System.arraycopy(bt, 0, bs, 0, length);
                }
                String str = new String(bs, "UTF-8");
                if (str != null) {
                    if (receivedCallback != null) {
                        receivedCallback.callback(str);
                    }
                }
                Log.i(TAG,"接收成功");

            } catch (IOException e) {
                Log.i(TAG,"接收失败");
            }
        }
    }
    /**
     * 发送数据
     *
     * @param data  数据
     */
    public void send(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null) {
                    try {
                        outputStream.write(data);
                        outputStream.flush();
                        Log.i(TAG,"发送成功");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i(TAG,"发送失败");
                    }
                } else {
                    //Connect();
                }
            }
        }).start();
    }

    //发送
    public void Send(final byte[] data)
    {
        if(socket!=null)
        {
            try {
                outputStream.write(data);
                outputStream.flush();
                Log.i(TAG,"发送成功");
            }catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG,"发送失败");
            }
        }
    }


    /**
     * 回调声明
     */
    public interface OnServerConnectedCallbackBlock {
        void callback();
    }
    public interface OnServerDisconnectedCallbackBlock {
        void callback(IOException e);
    }
    public interface OnReceiveCallbackBlock {
        void callback(String receicedMessage);
    }

    public void setConnectedCallback(OnServerConnectedCallbackBlock connectedCallback) {
        this.connectedCallback = connectedCallback;
    }

    public void setDisconnectedCallback(OnServerDisconnectedCallbackBlock disconnectedCallback) {
        this.disconnectedCallback = disconnectedCallback;
    }

    public void setReceivedCallback(OnReceiveCallbackBlock receivedCallback) {
        this.receivedCallback = receivedCallback;
    }
    /**
     * 移除回调
     */
    private void removeCallback() {
        connectedCallback = null;
        disconnectedCallback = null;
        receivedCallback = null;
    }

}
