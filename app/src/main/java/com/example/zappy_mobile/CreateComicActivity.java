package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateComicActivity extends AppCompatActivity {

    // Footer buttons
    private LinearLayout btnHome, btnLibrary, btnProfile;
    private ImageView btnSettings;

    // Cuadro para añadir contenido
    private LinearLayout addContentCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_comic);

        // ====== Footer ======
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);

        // ====== Cuadro principal clickeable ======
        addContentCard = findViewById(R.id.addContentCard);
        addContentCard.setOnClickListener(v -> {
            Intent intent = new Intent(CreateComicActivity.this, EditorActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // ====== Footer navigation ======
        btnHome.setOnClickListener(v ->
                startActivity(new Intent(CreateComicActivity.this, HomeActivity.class))
        );

        btnLibrary.setOnClickListener(v ->
                startActivity(new Intent(CreateComicActivity.this, LibraryActivity.class))
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(CreateComicActivity.this, ProfileActivity.class))
        );

        // Configuración
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(CreateComicActivity.this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}
