package com.example.zappy_mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Adaptador del RecyclerView para mostrar la lista de cómics en Zappy.
 * Genera miniaturas (thumbnails) de las primeras páginas de los PDFs de forma asíncrona.
 */
public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ViewHolder> {

    // Interfaz para manejar clics en los elementos
    public interface OnItemClickListener {
        void onItemClick(Comic comic);
    }

    private List<Comic> comics = new ArrayList<>();
    private final Context context;
    private OnItemClickListener listener;

    // Constructor
    public ComicAdapter(Context context) {
        this.context = context;
    }

    // Actualiza la lista de cómics mostrada
    public void setComics(List<Comic> list) {
        this.comics = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Asigna el listener de clics
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comic, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comic comic = comics.get(position);

        holder.tvTitle.setText(comic.getTitle());
        holder.tvAuthor.setText("Por autor: " + (comic.getAuthor() == null ? "Anónimo" : comic.getAuthor()));

        // Manejar clic sobre el item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(comic);
        });

        // Imagen por defecto mientras se genera el thumbnail
        holder.imgThumb.setImageResource(android.R.color.darker_gray);

        // Generar miniatura de forma asíncrona
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File file = new File(comic.getFilePath());
                if (file.exists()) {
                    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                    PdfRenderer renderer = new PdfRenderer(pfd);

                    if (renderer.getPageCount() > 0) {
                        PdfRenderer.Page page = renderer.openPage(0);

                        int width = page.getWidth() * 2;
                        int height = page.getHeight() * 2;

                        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                        page.close();
                        renderer.close();
                        pfd.close();

                        // Actualiza la UI en el hilo principal
                        holder.imgThumb.post(() -> holder.imgThumb.setImageBitmap(bitmap));
                    } else {
                        renderer.close();
                        pfd.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return comics.size();
    }

    // Clase interna ViewHolder para cada elemento del RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgThumb;
        final TextView tvTitle, tvAuthor;

        ViewHolder(View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }
    }
}
