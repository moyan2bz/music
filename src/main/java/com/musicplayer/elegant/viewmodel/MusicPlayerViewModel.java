package com.musicplayer.elegant.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.musicplayer.elegant.MusicPlayerApplication;
import com.musicplayer.elegant.data.MusicRepository;
import com.musicplayer.elegant.model.Song;
import com.musicplayer.elegant.service.MusicPlaybackService;

import java.util.List;

/**
 * 音乐播放器ViewModel，连接UI和数据层
 */
public class MusicPlayerViewModel extends AndroidViewModel {

    private final MusicRepository musicRepository;
    private MusicPlaybackService musicService;
    
    // LiveData用于观察当前播放的歌曲
    private final MutableLiveData<Song> currentSong = new MutableLiveData<>();
    
    // LiveData用于观察播放状态
    private final MutableLiveData<Boolean> playing = new MutableLiveData<>(false);
    
    // LiveData用于观察播放列表
    private final MutableLiveData<List<Song>> playlist = new MutableLiveData<>();

    public MusicPlayerViewModel(Application application) {
        super(application);
        musicRepository = ((MusicPlayerApplication) application).getMusicRepository();
        
        // 初始化播放状态
        playing.setValue(false);
    }

    /**
     * 设置音乐服务实例
     */
    public void setMusicService(MusicPlaybackService service) {
        this.musicService = service;
        
        // 设置播放状态监听器
        service.setPlaybackListener(new MusicPlaybackService.PlaybackListener() {
            @Override
            public void onPlaybackStateChanged(boolean isPlaying) {
                playing.postValue(isPlaying);
            }

            @Override
            public void onSongChanged(Song song) {
                currentSong.postValue(song);
            }

            @Override
            public void onPlaylistChanged(List<Song> songs) {
                playlist.postValue(songs);
            }
        });
        
        // 同步当前状态
        if (service.getCurrentSong() != null) {
            currentSong.setValue(service.getCurrentSong());
            playing.setValue(service.isPlaying());
        }
    }

    /**
     * 获取所有歌曲
     */
    public LiveData<List<Song>> getAllSongs() {
        return musicRepository.getAllSongs();
    }

    /**
     * 获取当前播放的歌曲
     */
    public LiveData<Song> getCurrentSong() {
        return currentSong;
    }

    /**
     * 获取播放状态
     */
    public LiveData<Boolean> isPlaying() {
        return playing;
    }

    /**
     * 获取当前播放列表
     */
    public LiveData<List<Song>> getPlaylist() {
        return playlist;
    }

    /**
     * 播放歌曲
     */
    public void playSong(Song song, List<Song> songs) {
        if (musicService != null && songs != null && !songs.isEmpty()) {
            int index = songs.indexOf(song);
            if (index != -1) {
                musicService.setPlaylist(songs, index);
            }
        }
    }

    /**
     * 播放/暂停
     */
    public void togglePlayPause() {
        if (musicService != null) {
            if (musicService.isPlaying()) {
                musicService.pause();
            } else {
                musicService.resume();
            }
        }
    }

    /**
     * 播放下一首
     */
    public void playNext() {
        if (musicService != null) {
            musicService.playNext();
        }
    }

    /**
     * 播放上一首
     */
    public void playPrevious() {
        if (musicService != null) {
            musicService.playPrevious();
        }
    }

    /**
     * 设置播放模式
     */
    public void setPlayMode(MusicPlaybackService.PlayMode playMode) {
        if (musicService != null) {
            musicService.setPlayMode(playMode);
        }
    }

    /**
     * 获取当前播放模式
     */
    public MusicPlaybackService.PlayMode getPlayMode() {
        if (musicService != null) {
            return musicService.getPlayMode();
        }
        return MusicPlaybackService.PlayMode.NORMAL;
    }

    /**
     * 获取当前播放进度
     */
    public long getCurrentPosition() {
        if (musicService != null) {
            return musicService.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 获取当前歌曲总时长
     */
    public long getDuration() {
        if (musicService != null) {
            return musicService.getDuration();
        }
        return 0;
    }

    /**
     * 跳转到指定位置
     */
    public void seekTo(long position) {
        if (musicService != null) {
            musicService.seekTo(position);
        }
    }
}