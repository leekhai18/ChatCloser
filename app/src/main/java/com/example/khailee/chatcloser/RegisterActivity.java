package com.example.khailee.chatcloser;

/**
 * Created by Khai Lee on 9/23/2017.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static java.lang.Thread.sleep;

public class RegisterActivity extends Activity {
    private final String CLIENT_REGISTER = "CLIENT_REGISTER";
    private final String SERVER_RE_REGISTER = "SERVER_RE_REGISTER";
    private final String SERVER_RE_CHECK_EXISTENCE = "SERVER_RE_CHECK_EXISTENCE";

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;

    private Socket mSocket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mSocket = SingletonSocket.getInstance().mSocket;

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Listening
        mSocket.on(SERVER_RE_REGISTER, onRegister);
        mSocket.on(SERVER_RE_CHECK_EXISTENCE, onCheckExistence);

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendInfoRegister();
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                launchLogin();
            }
        });
    }

    private void sendInfoRegister() {
        String username = inputFullName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        boolean checkInfo = checkInputInfoUser(username, email, password);
        if (checkInfo == true) {
            mSocket.emit(CLIENT_REGISTER, username, password, email);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter your details!", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean checkInputInfoUser(String username, String email, String password) {
        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            return true;
        }
        return false;
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
            pDialog.cancel();
        }
    }

    // Launch login activity
    private void launchLogin(){
        Intent i = new Intent(getApplicationContext(),
                LoginActivity.class);
        startActivity(i);
        finish();
    }

    private Emitter.Listener onCheckExistence = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = args[0].toString();

                    if (data == "true") {
                        Toast.makeText(getApplicationContext(), "Email has existed!", Toast.LENGTH_SHORT).show();
                    } else {
                        pDialog.setMessage("Registering...");
                        showDialog();
                    }
                }
            });
        }
    };

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */


    private Emitter.Listener onRegister = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = args[0].toString();

                    if (data == "true") {
                        launchLogin();
                    } else {
                        Log.d("error", "can't register");
                    }
                    hideDialog();
                }
            });
        }
    };
}