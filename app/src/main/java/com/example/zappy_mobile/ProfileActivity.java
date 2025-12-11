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
            // Nota: No llamamos a cargarDatosPerfil() aquí porque onResume()
            // se ejecuta justo después de onCreate y lo haría dos veces.
        } else {
            // Si no hay sesión, mandar al Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // --- CLICKS Y EVENTOS ---

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);

            // Enviamos los datos actuales para editar
            intent.putExtra("CURRENT_NAME", tvName.getText().toString());
            intent.putExtra("CURRENT_DESC", tvDescription.getText().toString());

            startActivity(intent);
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

    // Este método asegura que si editas el perfil y vuelves, se actualice la info
    @Override
    protected void onResume() {
        super.onResume();
        // Verificar usuario nuevamente por seguridad
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            cargarDatosPerfil();
        }
    }

    // 5. Lógica principal para traer los datos
    private void cargarDatosPerfil() {
        if (userId == null) return;

        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {

                            // A. LEER NOMBRE ("username")
                            String nombre = document.getString("username");
                            if (nombre == null || nombre.isEmpty()) {
                                // Si no está en BD, usar el de Auth o valor por defecto
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                                    nombre = user.getDisplayName();
                                } else {
                                    nombre = "Usuario Zappy";
                                }
                            }
                            tvName.setText(nombre);

                            // B. LEER DESCRIPCIÓN ("description") <-- CAMBIO IMPORTANTE AQUÍ
                            // En EditProfile guardamos "description" (inglés), no "descripcion" (español)
                            String descripcion = document.getString("description");

                            // Si guardaste con el nombre viejo "descripcion", intentamos leer ese también
                            if (descripcion == null) {
                                descripcion = document.getString("descripcion");
                            }

                            if (descripcion != null && !descripcion.isEmpty()) {
                                tvDescription.setText(descripcion);
                            } else {
                                tvDescription.setText("Sin descripción.");
                            }

                        } else {
                            // El documento no existe aún en Firestore
                            tvDescription.setText("¡Bienvenido! Edita tu perfil.");
                        }
                    } else {
                        // Error silencioso o log para no molestar al usuario cada vez
                    }
                });
    }
}
