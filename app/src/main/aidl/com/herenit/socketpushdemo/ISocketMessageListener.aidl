// ISocketMessageListener.aidl
package com.herenit.socketpushdemo;
import com.herenit.socketpushdemo.bean.SocketMessage;

// Declare any non-default types here with import statements

//消息监听器，主要用于跟新消息
interface ISocketMessageListener {
     void updateMessageList(out SocketMessage message);
}
