package com.musicplayer.elegant.model;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 歌曲实体类
 */
@Entity(tableName = "songs")
public class Song {

    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String title;        // 歌曲标题
    private String artist;       // 艺术家
    private String album;        // 专辑名
    private String path;         // 文件路径
    private long duration;       // 时长（毫秒）
    private String albumArtUri;  // 专辑封面URI
    private long albumId;        // 专辑ID
    private long artistId;       // 艺术家ID
    private long size;           // 文件大小
    private long dateAdded;      // 添加日期

    public Song() {
    }

    public Song(long id, String title, String artist, String album, String path, long duration, 
               String albumArtUri, long albumId, long artistId, long size, long dateAdded) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
        this.albumArtUri = albumArtUri;
        this.albumId = albumId;
        this.artistId = artistId;
        this.size = size;
        this.dateAdded = dateAdded;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    /**
     * 格式化歌曲时长为分:秒格式
     */
    public String getFormattedDuration() {
        long minutes = duration / 60000;
        long seconds = (duration % 60000) / 1000;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id == song.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}