package com.herenit.socketpushdemo.common;

/**
 * Created by HouBin on 2017/3/14.
 */

public class Custom {
    //消息类型
    public static final int MESSAGE_ACTIVE = 0;//心跳包
    public static final int MESSAGE_EVENT = 1;//事件包
    public static final int MESSAGE_CLOSE = 3;//断开连接
    //定义客户端和服务器端的称呼
    public static final String NAME_SERVER = "服务器";
    public static final String NAME_CLIENT = "客户端";

    //定义服务器的ip和端口号
    public static final String SERVER_HOST = "10.10.117.36";
    public static final int SERVER_PORT = 9001;

    public static final int SOCKET_CONNECT_TIMEOUT = 15;//设置Socket连接超时为6秒
    public static final int SOCKET_ACTIVE_TIME = 60;//发送心跳包的时间间隔为60秒

    //收到了SocketMessage消息
    public static final String ACTION_SOCKET_MESSSAGE = "com.herenit.socketmessage";
    //Socket当前的状态
    public static final String ACTION_SOCKET_STATUS = "action_socket_status";
}
