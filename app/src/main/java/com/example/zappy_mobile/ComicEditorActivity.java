package com.example.zappy_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ComicEditorActivity extends AppCompatActivity {

    private ImageView btnBack;
    private MaterialButton btnPublish;

    private LinearLayout toolText, toolImage, toolBalloon, toolPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_editor);

        // ====== Toolbar ======
        btnBack = findViewById(R.id.btnBack);
        btnPublish = findViewById(R.id.btnPublish);

        // Volver al perfil
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ComicEditorActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        // Publicar página
        btnPublish.setOnClickListener(v -> {
            Toast.makeText(this, "Página publicada ✅", Toast.LENGTH_SHORT).show();
            finish();
        });

        // ====== Herramientas ======
        toolText = findViewById(R.id.tool_text);
        toolImage = findViewById(R.id.tool_image);
        toolBalloon = findViewById(R.id.tool_balloon);
        toolPanel = findViewById(R.id.tool_panel);

        toolText.setOnClickListener(v ->
                Toast.makeText(this, "Agregar Texto", Toast.LENGTH_SHORT).show()
        );

        toolImage.setOnClickListener(v ->
                Toast.makeText(this, "Agregar Imagen", Toast.LENGTH_SHORT).show()
        );

        toolBalloon.setOnClickListener(v ->
                Toast.makeText(this, "Agregar Globo de diálogo", Toast.LENGTH_SHORT).show()
        );

        toolPanel.setOnClickListener(v ->
                Toast.makeText(this, "Agregar Panel", Toast.LENGTH_SHORT).show()
        );
    }
}
