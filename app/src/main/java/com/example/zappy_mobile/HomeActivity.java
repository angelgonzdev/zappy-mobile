package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements ComicAdapter.OnItemClickListener {

    private RecyclerView rvComics;
    private DBHelper dbHelper;
    private ComicAdapter adapter;
    private Button btnCreate;
    private ImageButton navUpload, navHome, navProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // ✅ Usa tu layout correcto

        // Inicialización de componentes
        dbHelper = new DBHelper(this);
        rvComics = findViewById(R.id.rvComics);
        btnCreate = findViewById(R.id.btnCreateComic);
        navUpload = findViewById(R.id.navUpload);
        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);

        // Configuración del RecyclerView
        rvComics.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ComicAdapter(this);
        adapter.setOnItemClickListener(this);
        rvComics.setAdapter(adapter);

        // Botones
        btnCreate.setOnClickListener(v -> openUpload());
        navUpload.setOnClickListener(v -> openUpload());
        navHome.setOnClickListener(v -> Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show());
        navProfile.setOnClickListener(v -> Toast.makeText(this, "Perfil (demo)", Toast.LENGTH_SHORT).show());
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