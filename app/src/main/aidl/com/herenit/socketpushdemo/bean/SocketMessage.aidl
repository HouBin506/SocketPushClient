// SocketMessage.aidl
package com.herenit.socketpushdemo.bean;

import com.herenit.socketpushdemo.bean.SocketMessage;
//SocketMessage实现了Parcelable接口，因为该对象作为aidl方法的参数传递，所以需要在aidl中申明
parcelable SocketMessage;
