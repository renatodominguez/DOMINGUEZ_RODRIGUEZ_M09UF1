package edu.fje.dominguez_rodriguez_m09uf1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SegundaPantalla extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<String> messages;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segunda_pantalla);

        // Inicializar Firebase
        FirebaseApp.initializeApp(this);
        databaseRef = FirebaseDatabase.getInstance().getReference("chat").child("mensajes");

        recyclerView = findViewById(R.id.recyclerViewChat);
        Button buttonSend = findViewById(R.id.buttonSend);
        final EditText editTextMessage = findViewById(R.id.editTextMessage);

        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages); // envia la array de mensajes al adaptador

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Agregar el mensaje al RecyclerView
                    messages.add(message);
                    adapter.notifyItemInserted(messages.size() - 1);
                    recyclerView.scrollToPosition(messages.size() - 1);

                    // Guardar el mensaje en Firebase Realtime Database
                    String messageId = databaseRef.push().getKey();
                    databaseRef.child(messageId).child("contenido").setValue(message);

                    // Limpiar el EditText después de enviar el mensaje
                    editTextMessage.setText("");
                }
            }
        });
    }
}
