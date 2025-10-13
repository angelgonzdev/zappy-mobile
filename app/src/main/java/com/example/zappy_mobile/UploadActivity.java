package com.example.zappy_mobile;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_PDF = 1001;
    private EditText etTitle, etAuthor;
    private TextView tvPicked;
    private Button btnPick, btnUpload;
    private Uri pickedUri = null;
    private String pickedName = null;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        etTitle = findViewById(R.id.etTitle);
        etAuthor = findViewById(R.id.etAuthor);
        tvPicked = findViewById(R.id.tvPicked);
        btnPick = findViewById(R.id.btnPick);
        btnUpload = findViewById(R.id.btnUpload);
        dbHelper = new DBHelper(this);

        btnPick.setOnClickListener(v -> pickPdf());
        btnUpload.setOnClickListener(v -> uploadPdf());
    }

    private void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_PICK_PDF);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_PDF && resultCode == Activity.RESULT_OK && data != null) {
            pickedUri = data.getData();
            pickedName = queryName(pickedUri);
            tvPicked.setText(pickedName);
        }
    }

    private String queryName(Uri uri) {
        String result = "document.pdf";
        try {
            ContentResolver resolver = getContentResolver();
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                try (CursorWrapper cw = new CursorWrapper(resolver.query(uri, null, null, null, null))) {
                    if (cw != null && cw.moveToFirst()) {
                        int idx = cw.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (idx >= 0) result = cw.getString(idx);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                result = new File(uri.getPath()).getName();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
            // Copiar archivo a almacenamiento interno de la app
            InputStream is = getContentResolver().openInputStream(pickedUri);
            String safeName = System.currentTimeMillis() + "_" + (pickedName != null ? pickedName : "comic.pdf");
            File outFile = new File(getFilesDir(), safeName);
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
            }
            if (is != null) is.close();

            String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            long id = dbHelper.insertComic(title, author, outFile.getAbsolutePath(), createdAt);
            if (id > 0) {
                Toast.makeText(this, "Cómic subido correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error guardando en DB", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error al copiar el PDF", Toast.LENGTH_SHORT).show();
        }
    }

    // pequeño wrapper para Cursor para poder auto-close
    static class CursorWrapper implements AutoCloseable {
        private final android.database.Cursor c;
        CursorWrapper(android.database.Cursor c) { this.c = c; }
        boolean moveToFirst() { return c != null && c.moveToFirst(); }
        int getColumnIndex(String name) { return c.getColumnIndex(name); }
        String getString(int idx) { return c.getString(idx); }
        @Override public void close() { if (c != null) c.close(); }
    }
}

