package com.musicplayer.elegant.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.musicplayer.elegant.model.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐数据仓库，负责从设备获取音乐文件
 */
public class MusicRepository {

    private final Context context;
    private final MutableLiveData<List<Song>> allSongs = new MutableLiveData<>();
    private final MutableLiveData<List<Song>> recentSongs = new MutableLiveData<>();
    private final MutableLiveData<List<Song>> favoriteSongs = new MutableLiveData<>();

    public MusicRepository(Context context) {
        this.context = context;
        loadSongs();
    }

    /**
     * 从设备加载所有音乐文件
     */
    private void loadSongs() {
        List<Song> songs = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                long artistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                long dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED));

                // 获取专辑封面URI
                Uri albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                );

                Song song = new Song(
                        id, title, artist, album, path, duration,
                        albumArtUri.toString(), albumId, artistId, size, dateAdded
                );

                songs.add(song);
            }
            cursor.close();
        }

        allSongs.postValue(songs);
        
        // 最近添加的歌曲（按添加日期排序，取前20首）
        List<Song> recent = new ArrayList<>(songs);
        recent.sort((s1, s2) -> Long.compare(s2.getDateAdded(), s1.getDateAdded()));
        if (recent.size() > 20) {
            recent = recent.subList(0, 20);
        }
        recentSongs.postValue(recent);
        
        // 模拟收藏的歌曲（实际应用中应该从数据库读取）
        List<Song> favorites = new ArrayList<>();
        if (!songs.isEmpty()) {
            // 简单示例：将前5首歌曲设为收藏
            int count = Math.min(5, songs.size());
            for (int i = 0; i < count; i++) {
                favorites.add(songs.get(i));
            }
        }
        favoriteSongs.postValue(favorites);
    }

    /**
     * 获取所有歌曲
     */
    public LiveData<List<Song>> getAllSongs() {
        return allSongs;
    }

    /**
     * 获取最近添加的歌曲
     */
    public LiveData<List<Song>> getRecentSongs() {
        return recentSongs;
    }

    /**
     * 获取收藏的歌曲
     */
    public LiveData<List<Song>> getFavoriteSongs() {
        return favoriteSongs;
    }

    /**
     * 根据ID获取歌曲
     */
    public Song getSongById(long id) {
        List<Song> songs = allSongs.getValue();
        if (songs != null) {
            for (Song song : songs) {
                if (song.getId() == id) {
                    return song;
                }
            }
        }
        return null;
    }

    /**
     * 搜索歌曲
     */
    public List<Song> searchSongs(String query) {
        List<Song> result = new ArrayList<>();
        List<Song> songs = allSongs.getValue();
        
        if (songs != null && !query.isEmpty()) {
            String lowerQuery = query.toLowerCase();
            for (Song song : songs) {
                if (song.getTitle().toLowerCase().contains(lowerQuery) ||
                        song.getArtist().toLowerCase().contains(lowerQuery) ||
                        song.getAlbum().toLowerCase().contains(lowerQuery)) {
                    result.add(song);
                }
            }
        }
        
        return result;
    }
}