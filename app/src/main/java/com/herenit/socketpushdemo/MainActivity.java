package com.herenit.socketpushdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.herenit.socketpushdemo.bean.SocketMessage;
import com.herenit.socketpushdemo.common.Custom;
import com.herenit.socketpushdemo.common.SocketServiceSP;
import com.herenit.socketpushdemo.core.SocketService;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!SocketServiceSP.getInstance(this).isSocketServiceStarted()) {
            Intent serviceIntent = new Intent(this, SocketService.class);
            startService(serviceIntent);
        }
    }

    public void intoTestSocket(View view) {
        startActivity(new Intent(this, TestActivity.class));
    }

}
