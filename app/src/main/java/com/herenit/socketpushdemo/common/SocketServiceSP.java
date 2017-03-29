package com.herenit.socketpushdemo.common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by HouBin on 2017/3/23.
 * 对SocketService的状态的存取
 */

public class SocketServiceSP {

    private static final String spName = "socket_service";
    private static final String KEY_SOCKETSERVICESTATUS = "key_isSocketService_started";

    private static SocketServiceSP mIntance;
    private SharedPreferences mPreferences;

    public static final SocketServiceSP getInstance(Context context) {
        if (mIntance == null)
            mIntance = new SocketServiceSP(context);
        return mIntance;
    }

    private SocketServiceSP(Context context) {
        mPreferences = context.getSharedPreferences(spName, context.MODE_PRIVATE);
    }

    /**
     * 保存当前SocketService的状态，连接或断开状态
     * @param isStart
     */
    public void saveSocketServiceStatus(boolean isStart) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(KEY_SOCKETSERVICESTATUS, isStart);
        editor.commit();
    }

    /**
     * 获取当前Service的状态，是否连接上
     * @return
     */
    public boolean isSocketServiceStarted() {
        return mPreferences.getBoolean(KEY_SOCKETSERVICESTATUS, false);
    }
}
