package com.example.khailee.chatcloser;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Khai Lee on 10/9/2017.
 */

public class ChatRoomActivity extends Activity {

    private final String CLIENT_SEND_MESSAGE = "CLIENT_SEND_MESSAGE";
    private final String SERVER_SEND_MESSAGE = "SERVER_SEND_MESSAGE";

    private Socket mSocket;
    private ListView lvUsersOnRoom;
    private ArrayAdapter adapterListUsers;
    private ArrayList<String> listUsers;
    private EditText editText;
    private ListView lvChatBox;
    private ArrayList<String> listMessage;
    private ArrayAdapter adapterListMessage;
    private Button btnSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        initSocket();
        addControls();
        addEvents();
        handleExtraPreActivity();
    }

    private void addEvents() {
        adapterListUsers.notifyDataSetChanged();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                adapterListMessage.notifyDataSetChanged();
            }
        });
    }

    private void addControls() {
        //
        listUsers = new ArrayList<String>();
        lvUsersOnRoom = (ListView) findViewById(R.id.listViewUserOnRoom);
        adapterListUsers = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listUsers);
        lvUsersOnRoom.setAdapter(adapterListUsers);

        editText = findViewById(R.id.editText);
        btnSend = findViewById(R.id.buttonSend);
        //
        lvChatBox = findViewById(R.id.listViewChatBox);
        listMessage = new ArrayList<String>();
        adapterListMessage = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listMessage);
        lvChatBox.setAdapter(adapterListMessage);
    }

    private void initSocket() {
         mSocket = SingletonSocket.getInstance().mSocket;

        mSocket.on(SERVER_SEND_MESSAGE, onListenServer_SendMessage);
    }


    private void handleExtraPreActivity() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String userJoinRoom = extras.getString("MEM_ROOM");
            listUsers.add(userJoinRoom);
        }
    }

    private void sendMessage() {
        listMessage.add(editText.getText().toString());

        org.json.simple.JSONObject  obj = new org.json.simple.JSONObject();
        obj.put("message", editText.getText().toString());
        obj.put("receiver", listUsers.get(0));

        mSocket.emit(CLIENT_SEND_MESSAGE, obj);
    }

    private Emitter.Listener onListenServer_SendMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString(SERVER_SEND_MESSAGE);
                        listMessage.add(message);
                        adapterListMessage.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}
