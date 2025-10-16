package com.example.zappy_mobile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    private EditText etTitle, etAuthor;
    private TextView tvPicked;
    private Button btnPick, btnUpload;
    private Uri pickedUri = null;
    private String pickedName = null;
    private DBHelper dbHelper;

    // Navegación del footer
    private LinearLayout btnHome, btnLibrary, btnCreate, btnProfile;
    private ImageView btnSettings;

    private final ActivityResultLauncher<Intent> pickPdfLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    pickedUri = result.getData().getData();
                    pickedName = queryName(pickedUri);
                    tvPicked.setText(pickedName);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Vincular vistas existentes
        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        tvPicked = findViewById(R.id.tvPicked);
        btnPick = findViewById(R.id.btnPick);
        btnUpload = findViewById(R.id.btnUpload);
        dbHelper = new DBHelper(this);

        // Vincular botones del footer
        btnHome = findViewById(R.id.btnHome);
        btnLibrary = findViewById(R.id.btnLibrary);
        btnCreate = findViewById(R.id.btnCreate);
        btnProfile = findViewById(R.id.btnProfile);
        btnSettings = findViewById(R.id.btnSettings);

        // Funcionalidad existente
        btnPick.setOnClickListener(v -> pickPdf());
        btnUpload.setOnClickListener(v -> uploadPdf());

        // Navegación del footer
        btnHome.setOnClickListener(v -> {
            startActivity(new Intent(UploadActivity.this, HomeActivity.class));
            finish();
        });

        btnLibrary.setOnClickListener(v -> {
            Toast.makeText(this, "Biblioteca (próximamente)", Toast.LENGTH_SHORT).show();
        });

        btnCreate.setOnClickListener(v -> {
            Toast.makeText(this, "Ya estás en Crear", Toast.LENGTH_SHORT).show();
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(UploadActivity.this, ProfileActivity.class));
        });

        // Botón configuración
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Toast.makeText(this, "Configuración (próximamente)", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        pickPdfLauncher.launch(intent);
    }

    private String queryName(Uri uri) {
        String result = "document.pdf";
        if (uri == null) return result;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) result = cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void uploadPdf() {
        String title = etTitle.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Ingrese título", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pickedUri == null) {
            Toast.makeText(this, "Seleccione un PDF primero", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Copiar el archivo PDF a almacenamiento interno
            ContentResolver resolver = getContentResolver();
            InputStream is = resolver.openInputStream(pickedUri);
            String safeName = System.currentTimeMillis() + "_" + (pickedName != null ? pickedName : "comic.pdf");
            File outFile = new File(getFilesDir(), safeName);

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            }
            if (is != null) is.close();

            // Guardar registro en la base de datos
            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            long id = dbHelper.insertComic(title, author, outFile.getAbsolutePath(), createdAt);

            if (id > 0) {
                Toast.makeText(this, "Cómic subido correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error guardando en la base de datos", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error al copiar el archivo PDF", Toast.LENGTH_SHORT).show();
        }
    }
}