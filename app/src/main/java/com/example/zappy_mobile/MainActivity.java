package com.example.zappy_mobile;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoappmoviles.R;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ComicAdapter.OnItemClickListener {

    private RecyclerView rvComics;
    private DBHelper dbHelper;
    private ComicAdapter adapter;
    private Button btnCreate;
    private ImageButton navUpload, navHome, navProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        rvComics = findViewById(R.id.rvComics);
        btnCreate = findViewById(R.id.btnCreateComic);
        navUpload = findViewById(R.id.navUpload);
        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);

        rvComics.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ComicAdapter(this);
        adapter.setOnItemClickListener(this);
        rvComics.setAdapter(adapter);

        btnCreate.setOnClickListener(v -> openUpload());

        navUpload.setOnClickListener(v -> openUpload());
        navHome.setOnClickListener(v -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show());
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
        // Abrir PDF viewer
        Intent i = new Intent(this, PdfViewerActivity.class);
        i.putExtra("path", comic.getFilePath());
        i.putExtra("title", comic.getTitle());
        startActivity(i);
    }
}