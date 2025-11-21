package com.example.zappy_mobile;

import android.Manifest; // <--- NUEVO
import android.app.NotificationChannel; // <--- NUEVO
import android.app.NotificationManager; // <--- NUEVO
import android.content.Context; // <--- NUEVO
import android.content.pm.PackageManager; // <--- NUEVO
import android.os.Build; // <--- NUEVO
import androidx.core.app.ActivityCompat; // <--- NUEVO
import androidx.core.app.NotificationCompat; // <--- NUEVO
import androidx.core.app.NotificationManagerCompat; // <--- NUEVO

import com.google.firebase.auth.FirebaseUser;
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

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irAlLogin();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irAlLogin();
            return;
        }

        // --- NOTIFICACIÓN DE BIENVENIDA ---
        if (savedInstanceState == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String nombre = "";
            if (user != null && user.getDisplayName() != null) {
                nombre = user.getDisplayName();
            }
            String mensaje = nombre.isEmpty()
                    ? "Bienvenido a Zappy ⚡"
                    : "Bienvenido a Zappy, " + nombre + " ⚡";

            // EN LUGAR DE TOAST, LLAMAMOS A LA NOTIFICACIÓN
            lanzarNotificacionSistema("¡Hola!", mensaje);
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

        // Listeners
        btnCreate.setOnClickListener(v -> openUpload());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
        btnHome.setOnClickListener(v -> Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show());
        btnLibrary.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, LibraryActivity.class)));
        btnCreateNav.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, EditorActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        loadComics();
    }

    // --- NUEVO MÉTODO PARA CREAR LA NOTIFICACIÓN DE BARRA DE ESTADO ---
    private void lanzarNotificacionSistema(String titulo, String contenido) {
        String channelId = "canal_bienvenida";

        // 1. Crear el canal de notificación (Necesario para Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notificaciones de Bienvenida",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 2. Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Puedes cambiar esto por R.drawable.tu_logo
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Se borra al tocarla

        // 3. Mostrarla (Verificando permisos para Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Si no tenemos permiso, pedimos permiso (o simplemente no mostramos nada para no crashear)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadComics();
        }
    }

    private void irAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadComics() {
        db.collection("comics").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Comic> list = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Comic comic = document.toObject(Comic.class);
                        list.add(comic);
                    } catch (Exception e) {}
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
