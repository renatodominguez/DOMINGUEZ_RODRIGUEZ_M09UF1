package edu.fje.dominguez_rodriguez_m09uf1;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class ClientActivity extends AppCompatActivity {

    private TextView textView;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<String> messages;
    private SecretKey symmetricKey;
    private KeyPair asymmetricKeyPair;
    private Button buttonSend;
    private EditText editTextMessage;
    private Socket socket;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewChat);
        buttonSend = findViewById(R.id.buttonSend);
        editTextMessage = findViewById(R.id.editTextMessage);
        textView = findViewById(R.id.textViewClientStatus);

        // Configurar RecyclerView
        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Generar claves simétricas y asimétricas
        try {
            generateSymmetricKey();
            generateAsymmetricKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Iniciar el cliente en un hilo aparte para evitar bloquear el hilo principal
        new Thread(new Runnable() {
            @Override
            public void run() {
                startClient();
            }
        }).start();

        // Configurar el botón de enviar mensaje
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Enviar el mensaje al servidor
                    sendMessage(message);
                }
            }
        });
    }

    private void startClient() {
        String serverAddress = "192.168.2.140";
        int port = 12345;

        try {
            socket = new Socket(serverAddress, port);
            outputStream = socket.getOutputStream();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Conectado al servidor.");
                }
            });

            // Recepción de la respuesta del servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                final String serverResponse = in.readLine();
                if (serverResponse != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*
                            String[] parts = serverResponse.split("\\|");
                            String encryptedMessage = parts[0];
                            String signature = parts[1];
                            if (verifySignature(encryptedMessage, signature)) {
                                String decryptedMessage = decryptSymmetrically(encryptedMessage);
                                textView.setText("Respuesta del servidor: " + decryptedMessage);
                            } else {
                                textView.setText("Error: La firma digital no es válida.");
                            }

                             */

                            textView.setText("Respuesta del servidor: " + serverResponse);
                        }
                    });
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("Error: " + e.getMessage());
                }
            });
        }
    }

    private void sendMessage(String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Xifratge missatge simétricament
                    String encryptedMessage = encryptSymmetrically(message);

                    // Firmar y cifrar la firma digital asimétricamente
                    String signature = signAndEncryptAsymmetrically(encryptedMessage);

                    String combinedMessage = encryptedMessage + "|" + signature;
                    // ENVIAR CLAU EMBOLQUELLADA AL SERVER
                    Log.i("test", "mensaje encriptado: " + combinedMessage);

                    outputStream.write((combinedMessage + "\n").getBytes());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.add(message);
                            adapter.notifyItemInserted(messages.size() - 1);
                            recyclerView.scrollToPosition(messages.size() - 1);
                            // Limpiar el EditText después de enviar el mensaje
                            editTextMessage.setText("");
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("Error al enviar el mensaje: " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    //-----------------------Encriptación------------------------------
    // Generar una clave simétrica
    private void generateSymmetricKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        symmetricKey = keyGenerator.generateKey();
    }

    // Generar un par de claves asimétricas
    private void generateAsymmetricKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        asymmetricKeyPair = keyPairGenerator.generateKeyPair();
    }

    // Cifrar el mensaje simétricamente
    private String encryptSymmetrically(String message) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Firmar y cifrar la firma digital asimétricamente
    private String signAndEncryptAsymmetrically(String message) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, asymmetricKeyPair.getPrivate());
            byte[] signatureBytes = cipher.doFinal(message.getBytes());
            return Base64.encodeToString(signatureBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Verificar la firma digital asimétricamente
    private boolean verifySignature(String message, String signature) {
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(asymmetricKeyPair.getPublic());
            verifier.update(message.getBytes());
            byte[] signatureBytes = Base64.decode(signature, Base64.DEFAULT);
            return verifier.verify(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String decryptSymmetrically(String encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, symmetricKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.decode(encryptedMessage, Base64.DEFAULT));
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
