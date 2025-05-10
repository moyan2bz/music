package com.musicplayer.elegant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.musicplayer.elegant.R;
import com.musicplayer.elegant.model.Song;

import java.util.List;

/**
 * 歌曲适配器，用于在RecyclerView中显示歌曲列表
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final Context context;
    private List<Song> songs;
    private final OnSongClickListener listener;

    /**
     * 歌曲点击监听器接口
     */
    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public SongAdapter(Context context, List<Song> songs, OnSongClickListener listener) {
        this.context = context;
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_card, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    /**
     * 更新歌曲列表
     */
    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    /**
     * 获取当前歌曲列表
     */
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * 歌曲ViewHolder
     */
    class SongViewHolder extends RecyclerView.ViewHolder {

        private final ImageView albumArtImageView;
        private final TextView songTitleTextView;
        private final TextView artistNameTextView;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArtImageView = itemView.findViewById(R.id.album_art);
            songTitleTextView = itemView.findViewById(R.id.song_title);
            artistNameTextView = itemView.findViewById(R.id.artist_name);

            // 设置点击事件
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSongClick(songs.get(position));
                }
            });
        }

        public void bind(Song song) {
            songTitleTextView.setText(song.getTitle());
            artistNameTextView.setText(song.getArtist());

            // 加载专辑封面
            Glide.with(context)
                    .load(song.getAlbumArtUri())
                    .placeholder(R.drawable.default_album_art)
                    .error(R.drawable.default_album_art)
                    .into(albumArtImageView);
        }
    }
}