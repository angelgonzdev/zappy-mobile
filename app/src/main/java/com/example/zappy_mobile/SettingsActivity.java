package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button btnLogout;
    private LinearLayout optionMissionVision, optionAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Botón cerrar sesión
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Opciones Misión/Visión y Acerca de la App
        optionMissionVision = findViewById(R.id.optionMissionVision);
        optionAbout = findViewById(R.id.optionAbout);

        optionMissionVision.setOnClickListener(v -> {
            Toast.makeText(this, "Misión: Desarrollar una app educativa.\nVisión: Facilitar la creación de cómics y aprendizaje.", Toast.LENGTH_LONG).show();
        });

        optionAbout.setOnClickListener(v -> {
            Toast.makeText(this, "App creada en 2025 para el curso de Desarrollo de Aplicaciones Móviles.", Toast.LENGTH_LONG).show();
        });
    }
}


