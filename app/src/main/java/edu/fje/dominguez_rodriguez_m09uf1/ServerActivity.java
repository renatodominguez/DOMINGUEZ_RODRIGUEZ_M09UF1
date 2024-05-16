package edu.fje.dominguez_rodriguez_m09uf1;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        textView = findViewById(R.id.textViewServerStatus);

        // Iniciar el servidor en un hilo aparte para evitar bloquear el hilo principal
        new Thread(new Runnable() {
            @Override
            public void run() {
                startServer();
            }
        }).start();
    }

    private void startServer() {
        int port = 12345;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Servidor iniciado. Esperando conexión...");
                }
            });

            while (true) { //Bucle que espera la conexio al client
                Socket clientSocket = serverSocket.accept();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Conexión establecida con el cliente.");
                    }
                });

                // Llegir missatge de Client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final String clientMessage = in.readLine();
                //EL MISSATGE ARRIBARA ENCRIPTAT
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("Mensaje del cliente: " + clientMessage);
                    }
                });

                // Responder al cliente
                OutputStream outputStream = clientSocket.getOutputStream();
                String serverResponse = "Mensaje encriptado: " + clientMessage;
                outputStream.write(serverResponse.getBytes());

                outputStream.close();
                clientSocket.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
