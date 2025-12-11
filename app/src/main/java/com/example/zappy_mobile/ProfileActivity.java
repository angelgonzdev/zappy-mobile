package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer; // Importación necesaria para el contador
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog; // Importación para la alerta
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

    // --- VARIABLE DEL EASTER EGG ---
    private int contadorEasterEgg = 0;
    private ImageView profileImage; // Variable para la imagen

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

        // Enlazamos la imagen de perfil para el Easter Egg
        profileImage = findViewById(R.id.profileImage);

        // (Opcional: Enlazar contadores)
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
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // --- CLICKS Y EVENTOS ---

        // --- LÓGICA DEL EASTER EGG ---
        profileImage.setOnClickListener(v -> {
            contadorEasterEgg++;

            // Opcional: Feedback visual pequeño al usuario
            if (contadorEasterEgg > 4 && contadorEasterEgg < 7) {
                // Toast.makeText(this, (7 - contadorEasterEgg) + "...", Toast.LENGTH_SHORT).show();
            }

            if (contadorEasterEgg == 7) {
                // ¡BINGO! Se llegó a los 7 toques
                contadorEasterEgg = 0; // Reiniciar contador
                mostrarBromaExplosion();
            }
        });
        // -----------------------------

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            // Enviamos los datos actuales para editar
            intent.putExtra("CURRENT_NAME", tvName.getText().toString());
            intent.putExtra("CURRENT_DESC", tvDescription.getText().toString());
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Navegación al Home
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        // Lógica para el botón Biblioteca (Library)
        btnLibrary.setOnClickListener(v -> {
            startActivity(new Intent(this, LibraryActivity.class)); // Asegúrate de tener LibraryActivity creada
            finish();
        });

// Lógica para el botón Editar/Crear (el del medio)
        btnEdit.setOnClickListener(v -> {
            startActivity(new Intent(this, EditorActivity.class)); // O UploadActivity, como se llame tu vista
            // finish(); // Opcional, dependiendo si quieres cerrar el perfil
        });

    }

    // Método que se ejecuta al volver a la pantalla (para recargar datos editados)
    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null) {
            cargarDatosPerfil();
        }
    }


    // --- MÉTODO DEL EASTER EGG DE BATMAN ---
    // Asegúrate de importar Glide arriba en tu archivo:
// import com.bumptech.glide.Glide;

    private void mostrarBromaExplosion() {
        // 1. Preparar la alerta
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        // 2. Inflar el diseño personalizado (dialog_batman.xml)
        android.view.LayoutInflater inflater = this.getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_batman, null);
        builder.setView(dialogView);

        // 3. Referenciar la imagen del XML
        android.widget.ImageView imgBatman = dialogView.findViewById(R.id.imgBatman);

        // 4. USAR GLIDE PARA ANIMAR EL GIF
        // 'batman_gif' es el nombre de tu archivo en res/drawable
        com.bumptech.glide.Glide.with(this)
                .asGif()                 // Forzamos a que sea GIF
                .load(R.drawable.batman_gif)
                .into(imgBatman);

        // 5. Configurar y mostrar el diálogo
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Poner fondo transparente para que se vea bien el diseño negro
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
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

                            String nombre = document.getString("username");
                            if (nombre == null || nombre.isEmpty()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                                    nombre = user.getDisplayName();
                                } else {
                                    nombre = "Usuario Zappy";
                                }
                            }
                            tvName.setText(nombre);

                            String descripcion = document.getString("description");
                            if (descripcion == null) {
                                descripcion = document.getString("descripcion");
                            }

                            if (descripcion != null && !descripcion.isEmpty()) {
                                tvDescription.setText(descripcion);
                            } else {
                                tvDescription.setText("Sin descripción.");
                            }

                        } else {
                            tvDescription.setText("¡Bienvenido! Edita tu perfil.");
                        }
                    }
                });
    }
}
