package edu.fje.dominguez_rodriguez_m09uf1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnClient, btnServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnClient = findViewById(R.id.buttonC);
        btnServer = findViewById(R.id.buttonS);

        btnClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la actividad del cliente
                startActivity(new Intent(MainActivity.this, ClientActivity.class));
            }
        });

        btnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la actividad del servidor
                startActivity(new Intent(MainActivity.this, ServerActivity.class));
            }
        });
    }
}
