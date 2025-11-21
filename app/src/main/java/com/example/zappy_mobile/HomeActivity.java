package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements ComicAdapter.OnItemClickListener {

    // Variables de Firebase
    private FirebaseFirestore db;

    // RecyclerView
    private RecyclerView rvComics;
    private ComicAdapter adapter;

    // Botones
    private LinearLayout btnHome, btnLibrary, btnCreateNav, btnProfile;
    private Button btnCreate; // botón central
    private ImageView btnSettings;

    // --- SEGURIDAD 1: Verificación al iniciar la actividad (ciclo de vida) ---
    @Override
    protected void onStart() {
        super.onStart();
        // Si no hay usuario logueado, expulsar inmediatamente
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irAlLogin();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- SEGURIDAD 2: Verificación al crear la actividad ---
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irAlLogin();
            return; // Detenemos la ejecución para que no cargue nada más
        }

        // 2. Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicialización de vistas
        rvComics = findViewById(R.id.rvComics);
        btnCreate = findViewById(R.id.btnCreateComic);
        btnSettings = findViewById(R.id.btnSettings);

        // Footer
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreateNav = findViewById(R.id.btnCreate);
        btnProfile = findViewById(R.id.btnProfile);

        // Configuración RecyclerView
        rvComics.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ComicAdapter(this);
        adapter.setOnItemClickListener(this);
        rvComics.setAdapter(adapter);

        // Botón crear central
        btnCreate.setOnClickListener(v -> openUpload());

        // Botón configuración
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class))
        );

        // Footer navigation
        btnHome.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
        );

        btnLibrary.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LibraryActivity.class))
        );

        btnCreateNav.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, EditorActivity.class))
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class))
        );

        loadComics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Opcional: Podrías verificar aquí también, pero con onStart suele bastar
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadComics();
        }
    }

    // --- MÉTODO DE SEGURIDAD PARA EXPULSAR AL USUARIO ---
    private void irAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        // Estas banderas borran el historial: el usuario no podrá volver atrás al Home
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadComics() {
        // --- NUEVO: Cargar desde Firebase Firestore ---
        db.collection("comics")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Comic> list = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Comic comic = document.toObject(Comic.class);
                                list.add(comic);
                            } catch (Exception e) {
                                // Error al convertir
                            }
                        }
                        adapter.setComics(list);
                    } else {
                        Toast.makeText(HomeActivity.this, "Error cargando datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openUpload() {
        startActivity(new Intent(this, UploadActivity.class));
    }

    @Override
    public void onItemClick(Comic comic) {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("path", comic.getFilePath());
        intent.putExtra("title", comic.getTitle());
        startActivity(intent);
    }
}
