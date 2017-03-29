package com.herenit.socketpushdemo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by HouBin on 2017/3/14.
 * 模拟数据模型，用于客户端与服务端的传输
 * 由于SocketMessage作为aidl的参数传递，所以此处必须实现Parcelable接口，
 */
public class SocketMessage implements Parcelable {
    private int type;
    private String message;
    private String from;
    private String to;
    private String userId;

    public SocketMessage() {

    }
    protected SocketMessage(Parcel in) {
        type = in.readInt();
        message = in.readString();
        from = in.readString();
        to = in.readString();
        userId = in.readString();
    }

    public static final Creator<SocketMessage> CREATOR = new Creator<SocketMessage>() {
        @Override
        public SocketMessage createFromParcel(Parcel in) {
            return new SocketMessage(in);
        }

        @Override
        public SocketMessage[] newArray(int size) {
            return new SocketMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 这个方法是自定义的，stub对象中会用到，不定义会报错
     *
     * @param in
     */
    public void readFromParcel(Parcel in) {
        this.type = in.readInt();
        this.message = in.readString();
        this.from = in.readString();
        this.to = in.readString();
        this.userId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(message);
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(userId);
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "SocketMessage{" +
                "type=" + type +
                ", message='" + message + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
