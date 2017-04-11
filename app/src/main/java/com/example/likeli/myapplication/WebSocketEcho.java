package com.example.likeli.myapplication;

import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * BruteForceCoding.java
 * Created by Likeli on 17/3/14.
 */

public final class WebSocketEcho extends WebSocketListener {
    private static String token;
    private static WebSocket sWebSocket;
    private Handler tvhandler;

    public WebSocketEcho(Handler _tvhandler) {
        tvhandler = _tvhandler;
    }

    public void run() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url("ws://172.16.5.63:8091/sub")
                .build();
        sWebSocket = client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        authMessage(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("获取到消息: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        byte[] inBuffer = bytes.toByteArray();
        byte[] result = BruteForceCoding.tail(inBuffer, inBuffer.length - 16);

        Long operation = BruteForceCoding.decodeIntBigEndian(inBuffer, 8, 4);
        if (3 == operation) {
//                heartBeatReceived();
        } else if (8 == operation) {
            authSuccess();
            try {
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = (JsonObject) parser.parse(new String(result).trim());
                System.out.println("token=" + jsonObject.get("token").getAsString());
                token = jsonObject.get("token").getAsString();
            } catch (Exception ex) {

            }
        } else if (5 == operation) {
//            Long packageLength = BruteForceCoding.decodeIntBigEndian(inBuffer, 0, 4);
//            Long headLength = BruteForceCoding.decodeIntBigEndian(inBuffer, 4, 2);
//            Long version = BruteForceCoding.decodeIntBigEndian(inBuffer, 6, 2);
//            Long sequenceId = BruteForceCoding.decodeIntBigEndian(inBuffer, 12, 4);
            String resultInfo = new String(result).trim();
            if (resultInfo.length() == 0)
                return;

            System.out.println("响应数据：" + resultInfo);
            Message msg = tvhandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("msg", resultInfo);
            msg.setData(bundle);
            tvhandler.sendMessage(msg);
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
//        webSocket.close(1000, null);
//        System.out.println("CLOSE: " + code + " " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }

    public void authSuccess() {
        System.out.println("认证通过！");
//        Message msg = tvhandler.obtainMessage();
//        Bundle bundle = new Bundle();
//        bundle.putString("msg", "{\"MsgInfo\":\"为您分配客服中，请稍后\", \"IsGuest\": false, \"UserID\": 0}");
//        msg.setData(bundle);
//        tvhandler.sendMessage(msg);
    }

    public void sendMessage(String msg, int msgType) {
        WebSocket webSocket;
        synchronized (WebSocketEcho.class) {
            webSocket = sWebSocket;
        }
        if (webSocket != null) {
            sendMessage(webSocket, msg, msgType);
        }
    }

    private static void sendMessage(WebSocket webSocket, String msg, int msgType) {
        String contentStr = "{\"MsgInfo\":\"" + msg + "\", \"MsgTypeID\": " + msgType + ", \"Token\": \"" + token + "\"}";
        byte[] conBute = contentStr.getBytes();
        int packLength = conBute.length + 16;
        byte[] message = new byte[16];

        int offset = BruteForceCoding.encodeIntBigEndian(message, packLength, 0, 4 * BruteForceCoding.BSIZE);
        offset = BruteForceCoding.encodeIntBigEndian(message, 16, offset, 2 * BruteForceCoding.BSIZE);
        offset = BruteForceCoding.encodeIntBigEndian(message, 1, offset, 2 * BruteForceCoding.BSIZE);
        offset = BruteForceCoding.encodeIntBigEndian(message, 4, offset, 4 * BruteForceCoding.BSIZE);
        offset = BruteForceCoding.encodeIntBigEndian(message, 1, offset, 4 * BruteForceCoding.BSIZE);

        ByteString aa = ByteString.of(BruteForceCoding.add(message, conBute));
        webSocket.send(aa);
    }

    private static void authMessage(WebSocket webSocket) {
        try {
            byte[] b = authWrite();
            webSocket.send(ByteString.of(b));
        } catch (Exception ex) {
            System.out.println("authMessage failure: " + ex.getMessage());
        }
    }

    public static synchronized byte[] authWrite() throws IOException {
        String contentStr = "{\"TypeID\": 10, \"AuthMsg\":{\"UserID\": 980,\"ServiceTypeID\":1}}";
        byte[] conBute = contentStr.getBytes();
        int packLength = conBute.length + 16;
        byte[] message = new byte[4 + 2 + 2 + 4 + 4];

        int offset = BruteForceCoding.encodeIntBigEndian(message, packLength, 0, 4 * BruteForceCoding.BSIZE);
        offset = BruteForceCoding.encodeIntBigEndian(message, 16, offset, 2 * BruteForceCoding.BSIZE);
        offset = BruteForceCoding.encodeIntBigEndian(message, 1, offset, 2 * BruteForceCoding.BSIZE);
        offset = BruteForceCoding.encodeIntBigEndian(message, 7, offset, 4 * BruteForceCoding.BSIZE);
        offset = BruteForceCoding.encodeIntBigEndian(message, 1, offset, 4 * BruteForceCoding.BSIZE);
        return BruteForceCoding.add(message, conBute);

    }
}