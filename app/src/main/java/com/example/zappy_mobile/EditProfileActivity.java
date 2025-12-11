package com.example.zappy_mobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack, profileImageEdit;
    private EditText etName, etDescription;
    private MaterialButton btnSaveChanges;

    // Variables de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        // 2. Vincular vistas
        btnBack = findViewById(R.id.btnBack);
        profileImageEdit = findViewById(R.id.profileImageEdit);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        // 3. Configurar botón Atrás
        // Usamos finish() para volver a la pantalla anterior sin recargarla desde cero
        btnBack.setOnClickListener(v -> finish());

        // 4. RECIBIR DATOS DEL PERFIL Y MOSTRARLOS EN EL EDITOR
        // Esto llena los campos con lo que enviaste desde ProfileActivity
        if (getIntent() != null) {
            String currentName = getIntent().getStringExtra("CURRENT_NAME");
            String currentDesc = getIntent().getStringExtra("CURRENT_DESC");

            if (currentName != null) etName.setText(currentName);
            if (currentDesc != null) etDescription.setText(currentDesc);
        }

        // 5. Configurar botón Guardar
        btnSaveChanges.setOnClickListener(v -> guardarCambios());
    }

    private void guardarCambios() {
        String nuevoNombre = etName.getText().toString().trim();
        String nuevaDescripcion = etDescription.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nuevoNombre)) {
            etName.setError("El nombre es obligatorio");
            return;
        }

        if (user == null) {
            Toast.makeText(this, "Error: No hay sesión activa", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- PASO A: Actualizar el nombre en Firebase Auth (Login) ---
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nuevoNombre)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // --- PASO B: Actualizar datos en Firestore (Base de Datos) ---
                        actualizarFirestore(nuevoNombre, nuevaDescripcion);
                    } else {
                        Toast.makeText(this, "Error al actualizar nombre de perfil", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void actualizarFirestore(String nombre, String descripcion) {
        // Preparamos los datos para guardar
        Map<String, Object> data = new HashMap<>();
        data.put("username", nombre);       // Actualizamos nombre en BD
        data.put("description", descripcion); // Guardamos descripción

        // Guardamos en la colección "users", documento con el ID del usuario actual
        // SetOptions.merge() sirve para NO borrar el email u otros datos que ya existan
        db.collection("users").document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "¡Cambios guardados correctamente!", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad y regresa al perfil actualizado
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Error al guardar en BD: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
