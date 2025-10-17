package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack, profileImageEdit;
    private EditText etName, etDescription;
    private MaterialButton btnSaveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = findViewById(R.id.btnBack);
        profileImageEdit = findViewById(R.id.profileImageEdit);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        btnSaveChanges.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu nombre", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
