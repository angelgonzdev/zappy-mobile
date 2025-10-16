package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LibraryActivity extends AppCompatActivity implements ComicAdapter.OnItemClickListener {

    private RecyclerView rvLibrary;
    private ComicAdapter adapter;
    private DBHelper dbHelper;

    // Botones del footer
    private LinearLayout btnHome, btnLibrary, btnCreate, btnProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        rvLibrary = findViewById(R.id.rvLibrary);
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

        // Cargar comics
        loadComics();

        // Navegación footer
        btnHome.setOnClickListener(v ->
                startActivity(new Intent(LibraryActivity.this, HomeActivity.class))
        );

        btnLibrary.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en Biblioteca", Toast.LENGTH_SHORT).show()
        );

        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(LibraryActivity.this, UploadActivity.class))
        );

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(LibraryActivity.this, ProfileActivity.class))
        );
    }

    private void loadComics() {
        List<Comic> list = dbHelper.getAllComics();
        adapter.setComics(list);
    }

    @Override
    public void onItemClick(Comic comic) {
        // Abrir PdfViewerActivity con el cómic seleccionado
        Intent intent = new Intent(LibraryActivity.this, PdfViewerActivity.class);
        intent.putExtra("path", comic.getFilePath());
        intent.putExtra("title", comic.getTitle());
        startActivity(intent);
    }
}

