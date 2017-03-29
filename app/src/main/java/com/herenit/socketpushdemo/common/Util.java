package com.herenit.socketpushdemo.common;

import com.herenit.socketpushdemo.bean.SocketMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HouBin on 2017/3/14.
 */

public class Util {
    /**
     * 根据消息对象构建Json对象
     *
     * @param message
     * @return
     */
    public static JSONObject initJsonObject(SocketMessage message) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (message.getType() != Custom.MESSAGE_ACTIVE)
                jsonObject.put("message", message.getMessage());
            else
                jsonObject.put("message", "");
            jsonObject.put("type", message.getType());
            jsonObject.put("from", message.getFrom());
            jsonObject.put("to", message.getTo());
            jsonObject.put("userId", message.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 解析Json数据
     *
     * @param json
     * @return
     */
    public static SocketMessage parseJson(String json) {
        SocketMessage message = new SocketMessage();
        try {
            JSONObject jsonObject = new JSONObject(json);
            message.setType(jsonObject.optInt("type"));
            if (message.getType() == Custom.MESSAGE_EVENT)
                message.setMessage(jsonObject.optString("message"));
            message.setFrom(jsonObject.optString("from"));
            message.setTo(jsonObject.optString("to"));
            message.setUserId(jsonObject.optString("userId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }
}
