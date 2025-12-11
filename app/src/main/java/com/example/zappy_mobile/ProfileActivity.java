package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    // 1. Declarar variables
    private TextView tvName, tvDescription, tvComicsCount, tvFollowersCount;
    private MaterialButton btnEditProfile;
    private ImageView btnSettings;

    // Navegación inferior
    private LinearLayout btnHome, btnLibrary, btnEdit;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 2. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 3. Enlazar vistas con el XML
        tvName = findViewById(R.id.tvName);
        tvDescription = findViewById(R.id.tvDescription);

        // (Opcional: Enlazar contadores si los vas a usar luego)
        tvComicsCount = findViewById(R.id.tvComicsCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSettings = findViewById(R.id.btnSettings);

        // Botones del menú inferior
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnEdit = findViewById(R.id.btnEdit);

        // 4. Verificar usuario actual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            // Llamamos a la función para descargar los datos
            cargarDatosPerfil();
        } else {
            // Si no hay sesión, mandar al Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // --- CLICKS Y EVENTOS ---

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            // Navegar a la vista de Settings
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });


        // Navegación al Home
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    // Este método asegura que si editas el perfil y vuelves, se actualice el nombre
    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null) {
            cargarDatosPerfil();
        }
    }

    // 5. Lógica principal para traer los datos
    private void cargarDatosPerfil() {
        // CORRECCIÓN 1: Usar la colección "users" (en minúscula, como en Registro)
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            // CORRECCIÓN 2: Leer el campo "username" (como lo guardaste en Registro)
                            String nombre = document.getString("username");

                            // Si por alguna razón vieja usaste otro nombre, intentamos leerlo también
                            if (nombre == null) nombre = document.getString("nombre");

                            String descripcion = document.getString("descripcion");

                            // Asignar texto a la vista
                            if (nombre != null && !nombre.isEmpty()) {
                                tvName.setText(nombre);
                            } else {
                                tvName.setText("Usuario Zappy");
                            }

                            if (descripcion != null && !descripcion.isEmpty()) {
                                tvDescription.setText(descripcion);
                            } else {
                                tvDescription.setText("Sin descripción");
                            }
                        } else {
                            // Si entra aquí, es porque el usuario no tiene ficha en la colección "users"
                            Toast.makeText(this, "Datos de perfil no encontrados", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }
