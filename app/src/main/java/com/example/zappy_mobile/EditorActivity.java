package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditorActivity extends AppCompatActivity {

    private LinearLayout btnHome, btnLibrary, btnEdit, btnProfile;
    private ImageView btnSettings;

    private LinearLayout addContentCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_comic);

        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnEdit = findViewById(R.id.btnEdit);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);

        addContentCard = findViewById(R.id.addContentCard);

        // CÓDIGO MODIFICADO PARA ABRIR CREATECOMICACTIVITY
        addContentCard.setOnClickListener(v -> {
            Intent intent = new Intent(EditorActivity.this, CreateComicActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // FIN CÓDIGO MODIFICADO

        btnHome.setOnClickListener(v ->
                startActivity(new Intent(EditorActivity.this, HomeActivity.class))
        );

        btnLibrary.setOnClickListener(v ->
                startActivity(new Intent(EditorActivity.this, LibraryActivity.class))
        );

        btnEdit.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en el editor ✏️", Toast.LENGTH_SHORT).show()
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(EditorActivity.this, ProfileActivity.class))
        );

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(EditorActivity.this, SettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}