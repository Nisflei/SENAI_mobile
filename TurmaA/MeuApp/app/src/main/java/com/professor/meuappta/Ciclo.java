package com.professor.meuappta;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Ciclo extends AppCompatActivity {

    private int contator = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ciclo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d("CICLO", "onCreate");

        Button bt = findViewById(R.id.btContar);
        TextView txt = findViewById(R.id.txtSaida);

        bt.setOnClickListener(v -> {
            contator++;
            txt.setText(String.valueOf(contator));
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("contador", contator);
        Log.d("CICLOx", "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        contator = savedInstanceState.getInt("contador");

        TextView txt = findViewById(R.id.txtSaida);
        txt.setText(String.valueOf(contator));

        Log.d("CICLOx", "onRestoreInstanceState");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("CICLOx", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("CICLOx", "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("CICLOx", "onStop");
    }
}