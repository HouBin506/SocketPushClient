package com.herenit.socketpushdemo.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.herenit.socketpushdemo.ISocketMessageListener;
import com.herenit.socketpushdemo.ISocketServiceInterface;
import com.herenit.socketpushdemo.MainActivity;
import com.herenit.socketpushdemo.R;
import com.herenit.socketpushdemo.bean.SocketMessage;
import com.herenit.socketpushdemo.common.Custom;
import com.herenit.socketpushdemo.common.SocketServiceSP;
import com.herenit.socketpushdemo.common.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;

/**
 * Created by HouBin on 2017/3/14.
 * 与服务器保持长连接的Service
 */

public class SocketService extends Service {
    private final String TAG = SocketService.class.getSimpleName();
    //Service实例，用于在Activity中进行连接断开发消息等图形界面化的操作

    //Socket的弱引用
    private WeakReference<Socket> mSocket;

    //消息发出的时间（不管是心跳包还是普通消息，发送完就会跟新时间）
    private long sendTime = 0;


    private final int MSG_WHAT_CONNECT = 111; //连接Socket
    private final int MSG_WHAT_DISCONNECT = 112;//断开Socket
    private final int MSG_WHAT_SENDMESSAGE = 113;//发送消息

    /**
     * 处理Socket的连接断开发消息的Handler机制
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_CONNECT:
                    if (isServerClose()) {
                        connectSocket();
                    } else {
                        Toast.makeText(SocketService.this, "Socket已经连接上了", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_WHAT_DISCONNECT:
                    if (isServerClose())
                        Toast.makeText(SocketService.this, "Socket已经断开了", Toast.LENGTH_SHORT).show();
                    else
                        interruptSocket();
                    break;
                case MSG_WHAT_SENDMESSAGE:
                    if (isServerClose()) {
                        Toast.makeText(SocketService.this, "请先连接Socket", Toast.LENGTH_SHORT).show();
                    } else {
                        SocketMessage socketMessage = (SocketMessage) msg.obj;
                        try {
                            SocketService.this.sendMessage(socketMessage);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    //读取服务器端发来的消息的线程
    private ReadThread mReadThread;

    //监控服务被杀死重启的广播，保持服务不被杀死
    private BroadcastReceiver restartBR;

    public SocketService() {
        super();
    }

    /**
     * 创建Service的同时要创建Socket
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate()");
        SocketServiceSP.getInstance(this).saveSocketServiceStatus(true);//保存了Service的开启状态
        //收到Service被杀死的广播，立即重启
        restartBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "SocketServer重启了......");
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action) && action.equals("socketService_killed")) ;
                Intent sIntent = new Intent(SocketService.this, SocketService.class);
                startService(sIntent);
                SocketServiceSP.getInstance(SocketService.this).saveSocketServiceStatus(true);//保存了Service的开启状态
            }
        };
        registerReceiver(restartBR, new IntentFilter("socketService_killed"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand(Intent intent, int flags, int startId)");
//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;//设置START_STICKY为了使服务被意外杀死后可以重启
    }


    /**
     * 客户端通过Socket与服务端建立连接
     */
    public void connectSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(Custom.SERVER_HOST, Custom.SERVER_PORT);
                    socket.setSoTimeout(Custom.SOCKET_CONNECT_TIMEOUT * 1000);
                    Log.e(TAG, "Socket连接成功。。。。。。");
                    mSocket = new WeakReference<Socket>(socket);
                    mReadThread = new ReadThread(socket);
                    mReadThread.start();
                    mHandler.postDelayed(activeRunnable, Custom.SOCKET_ACTIVE_TIME * 1000);//开启定时器，定时发送心跳包，保持长连接
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 发送心跳包的任务
     */
    private Runnable activeRunnable = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= Custom.SOCKET_ACTIVE_TIME * 1000) {
                SocketMessage message = new SocketMessage();
                message.setType(Custom.MESSAGE_ACTIVE);
                message.setMessage("");
                message.setFrom(Custom.NAME_CLIENT);
                message.setTo(Custom.NAME_SERVER);
                try {
                    if (!sendMessage(message)) {
                        if (mReadThread != null)
                            mReadThread.release();
                        releaseLastSocket(mSocket);
                        connectSocket();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mHandler.postDelayed(this, Custom.SOCKET_ACTIVE_TIME * 1000);
        }
    };

    /**
     * 发送消息到服务端
     *
     * @param message
     * @return
     */
    public boolean sendMessage(SocketMessage message) throws RemoteException {
        message.setUserId("001");
        if (mSocket == null || mSocket.get() == null) {
            return false;
        }
        Socket socket = mSocket.get();
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            if (!socket.isClosed()) {
                String jMessage = Util.initJsonObject(message).toString() + "\n";
                writer.write(jMessage);
                writer.flush();
                Log.e(TAG, "发送消息：" + jMessage);
                sendTime = System.currentTimeMillis();//每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间 
                if (message.getType() == Custom.MESSAGE_EVENT) {//通知实现了消息监听器的界面，让其跟新消息列表
                    messageListener.updateMessageList(message);
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 读取消息的线程
     */
    class ReadThread extends Thread {
        private WeakReference<Socket> mReadSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            mReadSocket = new WeakReference<Socket>(socket);
        }

        public void release() {
            isStart = false;
            releaseLastSocket(mReadSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mReadSocket.get();
            if (socket != null && !socket.isClosed()) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (isStart) {
                        if (reader.ready()) {
                            String message = reader.readLine();
                            Log.e(TAG, "收到消息：" + message);
                            SocketMessage sMessage = Util.parseJson(message);
                            if (sMessage.getType() == Custom.MESSAGE_ACTIVE) {//处理心跳回执

                            } else if (sMessage.getType() == Custom.MESSAGE_EVENT) {//事件消息
                                if (messageListener != null)
                                    messageListener.updateMessageList(sMessage);
                                sendNotification(sMessage);
                            } else if (sMessage.getType() == Custom.MESSAGE_CLOSE) {//断开连接消息回执
                                mHandler.removeCallbacks(activeRunnable);
                                release();
                                releaseLastSocket(mSocket);
                            }
                        }
                        Thread.sleep(100);//每隔0.1秒读取一次，节省点资源
                    }
                } catch (IOException e) {
                    release();
                    releaseLastSocket(mSocket);
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //通知的ID，为了分开显示，需要根据Id区分
    private int nId = 0;

    /**
     * 收到时间消息，发送通知提醒
     *
     * @param sMessage
     */
    private void sendNotification(SocketMessage sMessage) {
        //为了版本兼容，使用v7包的ＢＵＩＬＤＥＲ
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        //状态栏显示的提示，有的手机不显示
        builder.setTicker("简单的Notification");
        //通知栏标题
        builder.setContentTitle("from" + sMessage.getFrom() + "的消息");
        //通知栏内容
        builder.setContentText(sMessage.getMessage());
        //通知内容摘要
        builder.setSubText(sMessage.getUserId());
        //在通知右侧的时间下面用来展示一些其他信息
//        builder.setContentInfo("其他");
        //用来显示同种通知的数量，如果设置了ContentInfo属性，则NUmber属性会被覆盖，因为二者显示的位置相同
//        builder.setNumber(3);
        //可以点击通知栏的删除按钮
        builder.setAutoCancel(true);
        //系统状态栏显示的小图标
        builder.setSmallIcon(R.mipmap.jpush_notification_icon);
        //通知下拉显示的大图标
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        //点击通知跳转的INTENT
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);
        builder.setContentIntent(pendingIntent);//点击跳转
        //通知默认的声音，震动，呼吸灯
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(nId, notification);
        nId++;
    }

    /**
     * 释放Socket，并关闭
     *
     * @param socket
     */
    private void releaseLastSocket(WeakReference<Socket> socket) {
        if (socket != null) {
            Socket so = socket.get();
            try {
                if (so != null && !so.isClosed())
                    so.close();
                socket.clear();
                Log.e(TAG, "Socket断开连接。。。。。。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断是否断开连接，断开返回true,没有返回false
     *
     * @return
     */
    public boolean isServerClose() {
        try {
            if (mSocket != null && mSocket.get() != null) {
                mSocket.get().sendUrgentData(0);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
                return false;
            }
        } catch (Exception se) {
            return true;
        }
        return true;
    }

    /**
     * 销毁Service同时要销毁Socket
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
        if (mReadThread != null)
            mReadThread.release();
        releaseLastSocket(mSocket);
        sendBroadcast(new Intent("socketService_killed"));
        SocketServiceSP.getInstance(SocketService.this).saveSocketServiceStatus(false);
        unregisterReceiver(restartBR);
    }

    /**
     * 对外提供的断开Socket连接的方法（向服务器发送断开的包，服务器收到后会与之断开）
     */
    public void interruptSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketMessage message = new SocketMessage();
                message.setType(Custom.MESSAGE_CLOSE);
                message.setMessage("");
                message.setFrom(Custom.NAME_CLIENT);
                message.setTo(Custom.NAME_SERVER);
                try {
                    sendMessage(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind(Intent intent)");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "onBind(Intent intent)");
        super.onRebind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onRebind(Intent intent) ");
        return mBinder;
    }

    /**************************************************
     * AIDL
     ********************************************************/

    private ISocketMessageListener messageListener;
    private Binder mBinder = new ISocketServiceInterface.Stub() {
        private static final String ITAG = "ISocketServiceInterface";

        /**
         * 客户端要求连接Socket
         * @throws RemoteException
         */
        @Override
        public void connectSocket() throws RemoteException {
            Log.e(ITAG, "connectSocket");
            mHandler.sendEmptyMessage(MSG_WHAT_CONNECT);
        }

        /**
         * 客户端要求断开Socket连接
         * @throws RemoteException
         */
        @Override
        public void disConnectSocket() throws RemoteException {
            Log.e(ITAG, "disConnectSocket");
            mHandler.sendEmptyMessage(MSG_WHAT_DISCONNECT);
        }

        /**
         * 客户端向服务器端发送消息
         * @param message
         * @throws RemoteException
         */
        @Override
        public void sendMessage(SocketMessage message) throws RemoteException {
            Log.e(ITAG, "sendMessage");
            Message msg = Message.obtain();
            msg.what = MSG_WHAT_SENDMESSAGE;
            msg.obj = message;
            mHandler.sendMessage(msg);
        }

        /**
         * 客户端添加消息监听器，监听服务器端发来的消息
         * @param listener
         * @throws RemoteException
         */
        @Override
        public void addMessageListener(ISocketMessageListener listener) throws RemoteException {
            Log.e(ITAG, "addMessageListener");
            messageListener = listener;
        }
        @Override
        public void removeMessageListener(ISocketMessageListener listener) throws RemoteException {
            Log.e(ITAG, "removeMessageListener");
            messageListener = null;
        }
    };
}
