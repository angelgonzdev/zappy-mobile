package com.example.zappy_mobile;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zappy_mobile.R;

import java.io.File;

/**
 * Clase encargada de visualizar archivos PDF dentro del aplicativo Zappy.
 * Permite navegar entre páginas con botones "Anterior" y "Siguiente".
 */
public class PdfViewerActivity extends AppCompatActivity {

    private ImageView imgPage;
    private TextView tvPage, tvTitle;
    private Button btnPrev, btnNext;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private int pageIndex = 0;
    private int pageCount = 0;
    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        // Referencias de UI
        imgPage = findViewById(R.id.imgPage);
        tvPage = findViewById(R.id.tvPage);
        tvTitle = findViewById(R.id.tvTitlePdf);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        // Obtener datos del intent
        path = getIntent().getStringExtra("path");
        String title = getIntent().getStringExtra("title");

        if (title != null) tvTitle.setText(title);

        // Abrir el PDF
        try {
            openRenderer(path);
            pageCount = pdfRenderer.getPageCount();
            showPage(pageIndex);
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "No se pudo abrir el PDF", Toast.LENGTH_SHORT).show();
        }

        // Botón Anterior
        btnPrev.setOnClickListener(v -> {
            if (pageIndex > 0) {
                pageIndex--;
                showPage(pageIndex);
            }
        });

        // Botón Siguiente
        btnNext.setOnClickListener(v -> {
            if (pageIndex < pageCount - 1) {
                pageIndex++;
                showPage(pageIndex);
            }
        });
    }

    /**
     * Abre el archivo PDF desde el almacenamiento local.
     */
    private void openRenderer(String path) throws Exception {
        File file = new File(path);
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
    }

    /**
     * Muestra la página indicada del PDF en pantalla.
     */
    private void showPage(int index) {
        if (pdfRenderer == null || pdfRenderer.getPageCount() <= index) return;

        // Cerrar la página anterior
        if (currentPage != null) {
            currentPage.close();
        }

        // Abrir la nueva página
        currentPage = pdfRenderer.openPage(index);

        int density = getResources().getDisplayMetrics().densityDpi;
        int width = density / 72 * currentPage.getWidth();
        int height = density / 72 * currentPage.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imgPage.setImageBitmap(bitmap);

        tvPage.setText((index + 1) + " / " + pdfRenderer.getPageCount());
    }

    /**
     * Libera los recursos al cerrar la actividad.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (currentPage != null) currentPage.close();
            if (pdfRenderer != null) pdfRenderer.close();
            if (parcelFileDescriptor != null) parcelFileDescriptor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

