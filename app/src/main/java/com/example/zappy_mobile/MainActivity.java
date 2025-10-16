package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ComicAdapter.OnItemClickListener {

    private RecyclerView rvComics;
    private DBHelper dbHelper;
    private ComicAdapter adapter;
    private Button btnCreateComic;
    private LinearLayout btnHome, btnLibrary, btnCreate, btnProfile;
    private ImageView btnSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicialización de componentes
        dbHelper = new DBHelper(this);
        rvComics = findViewById(R.id.rvComics);
        btnCreateComic = findViewById(R.id.btnCreateComic);
        btnSettings = findViewById(R.id.btnSettings);

        // Botones del footer (LinearLayout)
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreate = findViewById(R.id.btnCreate);
        btnProfile = findViewById(R.id.btnProfile);

        // Configuración del RecyclerView
        rvComics.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ComicAdapter(this);
        adapter.setOnItemClickListener(this);
        rvComics.setAdapter(adapter);

        // Botón crear (centro de la pantalla)
        btnCreateComic.setOnClickListener(v -> openUpload());

        // Botón configuración
        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Configuración (próximamente)", Toast.LENGTH_SHORT).show()
        );

        // Navegación del footer
        btnHome.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
        );

        btnLibrary.setOnClickListener(v ->
                Toast.makeText(this, "Biblioteca (próximamente)", Toast.LENGTH_SHORT).show()
        );

        btnCreate.setOnClickListener(v -> openUpload());

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComics();
    }

    private void loadComics() {
        List<Comic> list = dbHelper.getAllComics();
        adapter.setComics(list);
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