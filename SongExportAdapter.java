package com.example.mzizimahymnal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SongExportAdapter extends RecyclerView.Adapter<SongExportAdapter.ViewHolder> {

    public interface OnItemCheckListener {
        void onItemCheck(SongWithAudios song);
        void onItemUncheck(SongWithAudios song);
    }

    private final List<SongWithAudios> songs;
    private final OnItemCheckListener listener;

    public SongExportAdapter(List<SongWithAudios> songs, OnItemCheckListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongWithAudios swa = songs.get(position);
        holder.checkBox.setText(swa.song.title);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) listener.onItemCheck(swa);
            else listener.onItemUncheck(swa);
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(android.R.id.text1);
        }
    }
}