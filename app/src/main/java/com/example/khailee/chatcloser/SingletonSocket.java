package com.example.khailee.chatcloser;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by Khai Lee on 10/6/2017.
 */

public class SingletonSocket {
    private final String SERVER_URL = "https://serverchatting.herokuapp.com/";
    private final int SERVER_PORT = 3000;
    private final String SERVER_URL_LOCAL = "http://192.168.79.1:3000";

    // instance
    private static SingletonSocket INSTANCE = new SingletonSocket();

    public Socket mSocket;

    {
        try {
            IO.Options opts = new IO.Options();
            opts.port = SERVER_PORT;
            mSocket = IO.socket(SERVER_URL_LOCAL);
        } catch (URISyntaxException e) {
        }
    }

    // other instance variables can be here
    private SingletonSocket() {

    };

    public static SingletonSocket getInstance() {
        return(INSTANCE);
    }
    // other instance methods can follow
}
