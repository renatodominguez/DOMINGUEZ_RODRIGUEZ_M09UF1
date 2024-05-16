package edu.fje.dominguez_rodriguez_m09uf1;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SegundaPantalla extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<String> messages;
    private SecretKey symmetricKey;
    private KeyPair asymmetricKeyPair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segunda_pantalla);

        recyclerView = findViewById(R.id.recyclerViewChat);
        Button buttonSend = findViewById(R.id.buttonSend);
        final EditText editTextMessage = findViewById(R.id.editTextMessage);

        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages); // envia la array de mensajes al adaptador

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Generar claves simétricas y asimétricas
        try {
            generateSymmetricKey();
            generateAsymmetricKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Cifrar el mensaje simétricamente
                    String encryptedMessage = encryptSymmetrically(message);

                    // Firmar y cifrar la firma digital asimétricamente
                    String signature = signAndEncryptAsymmetrically(encryptedMessage);

                    String combinedMessage = encryptedMessage + "|" + signature;
                    //ENVIAR COMBINED MESSAGE AL SERVER
                    Log.i("test","mensaje encriptado: "+combinedMessage);

                    messages.add(message);
                    adapter.notifyItemInserted(messages.size() - 1);
                    recyclerView.scrollToPosition(messages.size() - 1);

                    // Limpiar el EditText después de enviar el mensaje
                    editTextMessage.setText("");
                }
            }
        });
    }

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
}



