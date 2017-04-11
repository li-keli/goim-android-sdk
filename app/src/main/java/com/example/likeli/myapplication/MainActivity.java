package com.example.likeli.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView tv;
    EditText et;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private WebSocketEcho webSocket;
    Handler tvhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String congteng = msg.getData().getString("msg");
            StringBuilder sendInfo = new StringBuilder();

            try {
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = (JsonObject) parser.parse(new String(congteng).trim());
                Boolean isGuest = jsonObject.get("IsGuest").getAsBoolean();

                if (isGuest) {
                    sendInfo.append("客户" + jsonObject.get("UserID").getAsString() + ": ");
                } else {
                    sendInfo.append("客服" + jsonObject.get("UserID").getAsString() + ": ");
                }
                sendInfo.append(jsonObject.get("MsgInfo").getAsString());

            } catch (Exception ex) {
                sendInfo.append("系统消息：" + ex.getMessage());
            }
            tv.setText(tv.getText() + sendInfo.toString() + "\r\n");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.submitInfo);
        tv = (TextView) findViewById(R.id.textView2);
        et = (EditText) findViewById(R.id.sendInfo);

        webSocket = new WebSocketEcho(tvhandler);
        webSocket.run();

//        WebsocketClient.startRequest();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webSocket.sendMessage(et.getText().toString(), 1);
                et.setText("");
//                WebsocketClient.sendMessage(et.getText().toString(), 1);
//                tv.setText(et.getText().toString());
            }
        });
    }

}
