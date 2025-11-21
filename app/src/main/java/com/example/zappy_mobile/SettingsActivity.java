package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

// IMPORTANTE: Importar Firebase Auth
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private Button btnLogout, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Referencias de botones
        btnLogout = findViewById(R.id.btnLogout);
        btnBack = findViewById(R.id.btnBack);

        // Acción del botón Cerrar Sesión
        btnLogout.setOnClickListener(v -> {
            // 1. CERRAR SESIÓN EN FIREBASE (Crucial)
            FirebaseAuth.getInstance().signOut();

            // 2. Redirigir al REGISTRO (como pediste)
            Intent intent = new Intent(SettingsActivity.this, RegistroActivity.class);

            // 3. Limpiar el historial para que no pueda volver atrás
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish(); // Cierra esta actividad actual
        });

        // Acción del botón Volver
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
