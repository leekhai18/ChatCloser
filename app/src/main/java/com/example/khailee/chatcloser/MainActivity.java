package com.example.khailee.chatcloser;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.StringPrepParseException;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.client.Ack;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;



public class MainActivity extends AppCompatActivity {

    private  final String CLIENT_NEW_USER = "CLIENT_NEW_USER";
    private  final String CLIENT_SEND_MESSAGE = "CLIENT_SEND_MESSAGE";
    private  final String CLIENT_SEND_IMAGE = "CLIENT_SEND_IMAGE";
    private  final String CLIENT_SEND_REQUEST_IMAGE = "CLIENT_SEND_REQUEST_IMAGE";
    private  final String CLIENT_SEND_REQUEST_SOUND = "CLIENT_SEND_REQUEST_SOUND";
    private  final String SERVER_LIST_USER_ONLINE = "SERVER_LIST_USER_ONLINE";
    private  final String SERVER_SEND_MESSAGE = "SERVER_SEND_MESSAGE";
    private  final String SERVER_SEND_IMAGE = "SERVER_SEND_IMAGE";
    private  final String SERVER_SEND_SOUND = "SERVER_SEND_SOUND";
    private  final String CLIENT_SEND_SOUND = "CLIENT_SEND_SOUND";
    private  final String SERVER_URL = "https://serverchatting.herokuapp.com/";
    //private  final String SERVER_URL_LOCAL = "http://192.168.79.1:3000";
    private  final int SERVER_PORT = 3000;



    private final int REQUEST_TAKE_PHOTO = 123;
    private final int REQUEST_CHOOSE_PHOTO = 321;

    EditText edt;
    Button btnSend;
    Button btnSendPic;
    Button btnChoose;
    Button btnCamera;
    Button btnRequest;
    Button btnStart;
    Button btnStop;
    Button btnSendRecord;
    Button btnGetRecord;
    ImageView imgView;
    ListView lvUsersLogin;
    ListView lvChatBox;
    ArrayList<String> listUserOn;
    ArrayList<String> listMessage = new ArrayList<String>();
    Recorder recorder;

    private Socket mSocket;
    {
        try {
            IO.Options opts = new IO.Options();
            opts.port = SERVER_PORT;
            mSocket = IO.socket(SERVER_URL, opts);
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSocket();
        addControls();
        addEvents();
    }

    private void initSocket() {
        mSocket.on(SERVER_LIST_USER_ONLINE, onNewMessage_UsersOnline);
        mSocket.on(SERVER_SEND_MESSAGE, onNewMessage_SendMessage);
        mSocket.on(SERVER_SEND_IMAGE, onNewMessage_SendImage);
        mSocket.on(SERVER_SEND_SOUND, onNewMessage_SendSound);
        mSocket.connect();
    }

    private void addControls() {
        edt = (EditText) findViewById(R.id.editText);
        btnSend = (Button) findViewById(R.id.buttonSend);
        btnSendPic = (Button) findViewById(R.id.buttonSendPic);
        btnChoose = (Button) findViewById(R.id.buttonChoose);
        btnCamera = (Button) findViewById(R.id.buttonCamera);
        btnRequest = (Button) findViewById(R.id.buttonRequest);
        btnStart = (Button) findViewById(R.id.buttonStart);
        btnStop = (Button) findViewById(R.id.buttonStop);
        btnGetRecord = (Button) findViewById(R.id.buttonGetRecord);
        btnSendRecord = (Button) findViewById(R.id.buttonSendRecord);
        imgView = (ImageView) findViewById(R.id.imageView);

        recorder = new Recorder(this);
    }

    private void addEvents() {

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit(CLIENT_SEND_MESSAGE, edt.getText().toString());
            }
        });

        btnSendPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImage();
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });

        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromServer();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recorder.start();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recorder.stop();
            }
        });

        btnSendRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSound();
            }
        });

        btnGetRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRecordFromServer();
            }
        });
    }

    private void sendImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_btn_speak_now);
        byte[] bytes = getByteArrayFromBitmap(bitmap);
        mSocket.emit(CLIENT_SEND_IMAGE, bytes);
    }

    public byte[] getByteArrayFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private  void choosePicture(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTO);
    }

    private void getImageFromServer(){
        mSocket.emit(CLIENT_SEND_REQUEST_IMAGE, "xxx");
    }

    private void getRecordFromServer() {
        mSocket.emit(CLIENT_SEND_REQUEST_SOUND, "xxx");
    }

    private byte[] getByteArrayFromLocalFile(String path){
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try{
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        return bytes;
    }

    private void sendSound(){
        String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/kaka.3gpp";
        byte[] bytes = getByteArrayFromLocalFile(outputFile);
        mSocket.emit(CLIENT_SEND_SOUND, bytes);
    }

    // Utility Resize image
    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHOOSE_PHOTO && resultCode == RESULT_OK){
            try {
                Uri imageUri = data.getData();
                InputStream is = getContentResolver().openInputStream(imageUri);
                Bitmap bm = BitmapFactory.decodeStream(is);
                bm = resize(bm, 100, 100);
                byte[] bytes = getByteArrayFromBitmap(bm);
                mSocket.emit(CLIENT_SEND_IMAGE, bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Bitmap bm = (Bitmap) data.getExtras().get("data");
            bm = resize(bm, 100, 100);
            byte[] bytes = getByteArrayFromBitmap(bm);
            mSocket.emit(CLIENT_SEND_IMAGE, bytes);
        }
    }

    private Emitter.Listener onNewMessage_SendMessage = new Emitter.Listener() {
        @Override
        public void call(final Object...args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    String message;
                    try {
                        lvChatBox = (ListView) findViewById(R.id.listViewChatBox);
                        message = data.getString(SERVER_SEND_MESSAGE);
                        listMessage.add(message);
                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listMessage);
                        lvChatBox.setAdapter(adapter);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage_UsersOnline = new Emitter.Listener() {
        @Override
        public void call(final Object...args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    JSONArray array;
                    try {
                        array = data.getJSONArray(SERVER_LIST_USER_ONLINE);
                        lvUsersLogin = (ListView) findViewById(R.id.listViewUsersLogin);
                        listUserOn = new ArrayList<String>();
                        for (int i = 0; i < array.length(); i++)
                        {
                            listUserOn.add(array.get(i).toString());
                        }
                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listUserOn);
                        lvUsersLogin.setAdapter(adapter);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage_SendImage = new Emitter.Listener() {
        @Override
        public void call(final Object...args) {
            handNewImage(args[0]);
        }
    };

    private void handNewImage(final Object arg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                byte[] imageByteArray = (byte[]) arg;
                Bitmap bm = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                imgView.setImageBitmap(bm);
            }
        });
    }

    private Emitter.Listener onNewMessage_SendSound = new Emitter.Listener() {
        @Override
        public void call(final Object...args) {
            handNewSound(args[0]);
        }
    };

    private void handNewSound(final Object arg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playMp3FromBytes((byte[]) arg);
            }
        });
    }

    private void playMp3FromBytes(byte[] mp3SoundByteArray){
        try{
            File tempMp3 = File.createTempFile("kurchina", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            MediaPlayer mediaPlayer = new MediaPlayer();
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
