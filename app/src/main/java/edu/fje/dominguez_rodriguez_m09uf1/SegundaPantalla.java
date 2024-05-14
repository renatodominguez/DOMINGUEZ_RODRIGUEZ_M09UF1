package edu.fje.dominguez_rodriguez_m09uf1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SegundaPantalla extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<String> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_segunda_pantalla);

        recyclerView = findViewById(R.id.recyclerViewChat);
        Button buttonSend = findViewById(R.id.buttonSend);
        final EditText editTextMessage = findViewById(R.id.editTextMessage);

        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages); //envia la array de mensajes al adaptador

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Agregar el mensaje al principio de la lista
                    messages.add(message);
                    adapter.notifyItemInserted(messages.size() - 1);
                    Log.i("test", "mensaje: "+message);
                    // Limpiar textbox después de enviar el mensaje
                    editTextMessage.setText("");
                    // Desplazarse al principio del RecyclerView para mostrar el nuevo mensaje
                    recyclerView.scrollToPosition(messages.size() - 1);
                }
            }
        });
    }
}
