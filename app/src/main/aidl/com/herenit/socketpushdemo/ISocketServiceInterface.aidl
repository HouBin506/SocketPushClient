// ISocketServiceInterface.aidl
package com.herenit.socketpushdemo;
import com.herenit.socketpushdemo.ISocketMessageListener;
import com.herenit.socketpushdemo.bean.SocketMessage;

// Declare any non-default types here with import statements

//负责连接Socket的Service服务于客户端交互的接口
interface ISocketServiceInterface {
    //连接Socket
    void connectSocket();
    //断开连接
    void disConnectSocket();
    //发送消息到服务器
    void sendMessage(in SocketMessage message);
    //添加消息监听
    void addMessageListener(ISocketMessageListener listener);
    //取消监听
    void removeMessageListener(ISocketMessageListener listener);
}
