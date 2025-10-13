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

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Comic comic);
    }

    private List<Comic> comics = new ArrayList<>();
    private Context ctx;
    private OnItemClickListener listener;

    public ComicAdapter(Context ctx) { this.ctx = ctx; }

    public void setComics(List<Comic> list) {
        this.comics = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comic, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comic c = comics.get(position);
        holder.tvTitle.setText(c.getTitle());
        holder.tvAuthor.setText("Por autor: " + (c.getAuthor() == null ? "Anon" : c.getAuthor()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(c);
        });

        // Generar thumbnail de la primera página de forma asíncrona
        holder.imgThumb.setImageResource(android.R.color.darker_gray);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File f = new File(c.getFilePath());
                if (f.exists()) {
                    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
                    PdfRenderer renderer = new PdfRenderer(pfd);
                    if (renderer.getPageCount() > 0) {
                        PdfRenderer.Page page = renderer.openPage(0);
                        int w = page.getWidth() * 2;
                        int h = page.getHeight() * 2;
                        final Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        page.close();
                        renderer.close();
                        pfd.close();
                        holder.imgThumb.post(() -> holder.imgThumb.setImageBitmap(bmp));
                    } else {
                        renderer.close();
                        pfd.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() { return comics.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvAuthor;

        ViewHolder(View item) {
            super(item);
            imgThumb = item.findViewById(R.id.imgThumb);
            tvTitle = item.findViewById(R.id.tvTitle);
            tvAuthor = item.findViewById(R.id.tvAuthor);
        }
    }
}

