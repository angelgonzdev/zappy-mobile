package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout btnHome, btnLibrary, btnCreate, btnProfile;
    private MaterialButton btnEditProfile;
    private ImageView btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Vincular vistas
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreate = findViewById(R.id.btnEdit); // antes llamado btnEdit
        btnProfile = findViewById(R.id.btnProfile);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSettings = findViewById(R.id.btnSettings);

        // Navegación footer
        btnHome.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class))
        );

        btnLibrary.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, LibraryActivity.class))
        );

        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditorActivity.class))
        );

        btnProfile.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en Perfil", Toast.LENGTH_SHORT).show()
        );

        // Botón editar perfil
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class))
        );

        // Botón de Configuración -> Abrir SettingsActivity
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class))
        );
    }
}
