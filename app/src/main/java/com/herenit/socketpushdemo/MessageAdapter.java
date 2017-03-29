package com.herenit.socketpushdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.herenit.socketpushdemo.bean.SocketMessage;
import com.herenit.socketpushdemo.common.Custom;

import java.util.List;

/**
 * Created by HouBin on 2017/3/14.
 * 简单的Adapter，显示服务器与客户端的消息传输
 */

public class MessageAdapter extends BaseAdapter {
    private List<SocketMessage> mData;
    private LayoutInflater mInflater;

    public MessageAdapter(Context context, List<SocketMessage> messageList) {
        mData = messageList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    //分收到和发送两种模式
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).getFrom().equals(Custom.NAME_SERVER))
            return 0;
        else
            return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View fromView;
        View toView;
        SocketMessage message = mData.get(position);
        int viewType = getItemViewType(position);
        if (viewType == 0) {//服务器端发送来的消息
            FromViewHolder fromViewHolder = null;
            if (convertView == null) {
                fromViewHolder = new FromViewHolder();
                fromView = mInflater.inflate(R.layout.item_message_from, null);
                fromViewHolder.name = (TextView) fromView.findViewById(R.id.tv_from_name);
                fromViewHolder.message = (TextView) fromView.findViewById(R.id.tv_from_message);
                fromView.setTag(fromViewHolder);
                convertView = fromView;
            } else {
                fromViewHolder = (FromViewHolder) convertView.getTag();
            }
            fromViewHolder.name.setText(message.getFrom());
            fromViewHolder.message.setText(message.getMessage());
        } else {//客户端发出去的消息
            ToViewHolder toViewHolder = null;
            if (convertView == null) {
                toViewHolder = new ToViewHolder();
                toView = mInflater.inflate(R.layout.item_message_to, null);
                toViewHolder.name = (TextView) toView.findViewById(R.id.tv_to_name);
                toViewHolder.message = (TextView) toView.findViewById(R.id.tv_to_message);
                toView.setTag(toViewHolder);
                convertView = toView;
            } else {
                toViewHolder = (ToViewHolder) convertView.getTag();
            }
            toViewHolder.name.setText(message.getFrom());
            toViewHolder.message.setText(message.getMessage());
        }
        return convertView;
    }

    private class FromViewHolder {
        TextView name;
        TextView message;
    }

    private class ToViewHolder {
        TextView name;
        TextView message;
    }
}