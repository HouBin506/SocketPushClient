package com.herenit.socketpushdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.herenit.socketpushdemo.bean.SocketMessage;
import com.herenit.socketpushdemo.common.Custom;
import com.herenit.socketpushdemo.core.SocketService;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends Activity implements View.OnClickListener {
    private Button mBtn_send, mBtn_connect, mBtn_interrupt;
    private EditText mEt_sendContent;
    private ListView mLv_result;
    private List<SocketMessage> mData = new ArrayList<>();
    private MessageAdapter mAdapter;
    private ISocketServiceInterface mSocket;
    private final int MSG_WHAT_UPDATE = 200;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT_UPDATE) {//跟新UI界面的消息列表
                SocketMessage sMessage = (SocketMessage) msg.obj;
                updateMessageList(sMessage);
            }
        }
    };


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //获取操作SocketService的aidl的接口对象（绑定成功后由去Service的onBind方法返回来的）
            mSocket = ISocketServiceInterface.Stub.asInterface(iBinder);
            try {
                //添加消息监听器
                mSocket.addMessageListener(messageListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSocket = null;
        }
    };


    //消息监听器，用来监听消息的手法，aidl接口
    private ISocketMessageListener messageListener = new ISocketMessageListener.Stub() {
        /**
         * 收到了服务器端发来的消息，或者发送消息到了服务器端都要跟新UI
         *
         * @param message
         */
        @Override
        public void updateMessageList(SocketMessage message) throws RemoteException {
            Message data = Message.obtain();
            data.what = MSG_WHAT_UPDATE;
            data.obj = message;
            mHandler.sendMessage(data);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        /**
         * 绑定服务
         */
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        initView();
    }

    private void initView() {
        mBtn_connect = (Button) findViewById(R.id.btn_connect);
        mBtn_interrupt = (Button) findViewById(R.id.btn_interrupt);
        mBtn_send = (Button) findViewById(R.id.btn_send);
        mEt_sendContent = (EditText) findViewById(R.id.et_sendContent);
        mLv_result = (ListView) findViewById(R.id.lv_result);
        mBtn_connect.setOnClickListener(this);
        mBtn_interrupt.setOnClickListener(this);
        mBtn_send.setOnClickListener(this);
        mAdapter = new MessageAdapter(this, mData);
        mLv_result.setAdapter(mAdapter);
    }

    /**
     * 收到了服务器端发来的消息，或者发送消息到了服务器端都要跟新UI
     *
     * @param message
     */
    private void updateMessageList(SocketMessage message) {
        mData.add(message);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_connect://连接服务器
                    mSocket.connectSocket();
                    break;
                case R.id.btn_interrupt://断开Socket连接
                    mSocket.disConnectSocket();
                    break;
                case R.id.btn_send://向服务器发送消息
                    String content = mEt_sendContent.getText().toString().trim();
                    if (TextUtils.isEmpty(content)) {
                        Toast.makeText(this, "消息为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendMessage(content);
                    mEt_sendContent.setText("");
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向服务器发消息
     */
    private void sendMessage(final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketMessage message = new SocketMessage();
                message.setType(Custom.MESSAGE_EVENT);
                message.setMessage(text);
                message.setFrom(Custom.NAME_CLIENT);
                message.setTo(Custom.NAME_SERVER);
                try {
                    mSocket.sendMessage(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 阻止程序在回退到最后一步被杀死，而是保持后台运行
     */
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(false);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
    @Override
    protected void onDestroy() {
        try {
            //取消消息监听
            mSocket.removeMessageListener(messageListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //解绑服务
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
