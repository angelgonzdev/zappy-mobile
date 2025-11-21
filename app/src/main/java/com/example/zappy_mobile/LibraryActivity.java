package com.example.zappy_mobile;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends AppCompatActivity implements ComicAdapter.OnItemClickListener {

    private static final String CHANNEL_ID = "zappy_library_channel";
    private static final int NOTIF_PERMISSION_REQUEST = 201;
    private static final int NOTIF_ID = 1001;

    private RecyclerView rvLibrary;
    private ComicAdapter adapter;
    private DBHelper dbHelper;

    private SearchView searchView;

    // Lista completa + lista filtrada
    private List<Comic> fullList = new ArrayList<>();

    // Botones del footer
    private LinearLayout btnHome, btnLibrary, btnCreate, btnProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        rvLibrary = findViewById(R.id.rvLibrary);
        searchView = findViewById(R.id.searchBar);

        dbHelper = new DBHelper(this);

        // Footer buttons
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreate = findViewById(R.id.btnCreate);
        btnProfile = findViewById(R.id.btnProfile);

        // RecyclerView
        rvLibrary.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ComicAdapter(this);
        adapter.setOnItemClickListener(this);
        rvLibrary.setAdapter(adapter);

        // Cargar comics completos
        loadComics();

        // ================= BUSCADOR =================
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        // Navegación footer
        btnHome.setOnClickListener(v ->
                startActivity(new Intent(LibraryActivity.this, HomeActivity.class))
        );

        btnLibrary.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en Biblioteca", Toast.LENGTH_SHORT).show()
        );

        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(LibraryActivity.this, EditorActivity.class))
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(LibraryActivity.this, ProfileActivity.class))
        );

        // ===== Notificaciones: canal + permiso + mostrar notificación de bienvenida =====
        createNotificationChannelIfNeeded();
        requestNotificationPermissionIfNeeded();
        showWelcomeNotificationIfAllowed();
    }

    // == Notificaciones ==
    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Zappy Biblioteca";
            String description = "Notificaciones de la biblioteca (bienvenida y alertas)";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIF_PERMISSION_REQUEST);
            }
        }
    }

    private void showWelcomeNotificationIfAllowed() {
        // Si Android 13+ y no hay permiso → no mostrar ahora
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Opcional: puedes esperar a que el usuario acepte y luego mostrar la notificación
            return;
        }

        String title = "📚 ¡Bienvenido a la Biblioteca de Zappy! ✨";
        String message = "Has ingresado a un lugar lleno de aventuras, mundos mágicos, héroes legendarios y páginas esperando ser descubiertas. ¡Explora, imagina y disfruta! 🚀🌟📖";

        // Intent que abrirá la LibraryActivity cuando el usuario pulse la notificación
        Intent intent = new Intent(this, LibraryActivity.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // ajusta a tu drawable
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(this).notify(NOTIF_ID, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIF_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario concedió permiso ahora, mostramos la notificación inmediatamente
                showWelcomeNotificationIfAllowed();
            }
        }
    }

    private void loadComics() {
        fullList = dbHelper.getAllComics();
        adapter.setComics(fullList);
    }

    // ================= FILTRO =================
    private void filterList(String text) {
        List<Comic> filtered = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            adapter.setComics(fullList);
            return;
        }

        String q = text.toLowerCase();
        for (Comic c : fullList) {
            String title = c.getTitle() != null ? c.getTitle().toLowerCase() : "";
            String author = c.getAuthor() != null ? c.getAuthor().toLowerCase() : "";
            if (title.contains(q) || author.contains(q)) {
                filtered.add(c);
            }
        }

        adapter.setComics(filtered);
    }

    @Override
    public void onItemClick(Comic comic) {
        Intent intent = new Intent(LibraryActivity.this, PdfViewerActivity.class);
        intent.putExtra("path", comic.getFilePath());
        intent.putExtra("title", comic.getTitle());
        startActivity(intent);
    }
}
