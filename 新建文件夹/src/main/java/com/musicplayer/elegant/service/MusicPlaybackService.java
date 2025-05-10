package com.musicplayer.elegant.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.musicplayer.elegant.MusicPlayerApplication;
import com.musicplayer.elegant.R;
import com.musicplayer.elegant.model.Song;
import com.musicplayer.elegant.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐播放服务，负责后台播放音乐
 */
public class MusicPlaybackService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private final IBinder binder = new MusicBinder();
    private ExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    
    private List<Song> playlist = new ArrayList<>();
    private int currentSongIndex = -1;
    private boolean isPlaying = false;
    
    // 播放模式
    public enum PlayMode {
        NORMAL,     // 顺序播放
        REPEAT_ONE, // 单曲循环
        REPEAT_ALL, // 列表循环
        SHUFFLE     // 随机播放
    }
    
    private PlayMode currentPlayMode = PlayMode.NORMAL;
    
    // 播放状态监听器
    public interface PlaybackListener {
        void onPlaybackStateChanged(boolean isPlaying);
        void onSongChanged(Song song);
        void onPlaylistChanged(List<Song> playlist);
    }
    
    private PlaybackListener playbackListener;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化ExoPlayer
        initializePlayer();
        
        // 初始化MediaSession
        initializeMediaSession();
        
        // 初始化音频焦点管理
        initializeAudioFocus();
    }

    private void initializePlayer() {
        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    // 播放结束，根据播放模式处理
                    handlePlaybackCompletion();
                }
            }
        });
    }

    private void initializeMediaSession() {
        mediaSession = new MediaSessionCompat(this, "MusicPlaybackService");
        mediaSession.setActive(true);
        
        // 设置媒体会话回调
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                resume();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                playPrevious();
            }
        });
    }

    private void initializeAudioFocus() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(this)
                    .build();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 启动前台服务
        startForeground(1, createNotification());
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * 创建前台服务通知
     */
    private Notification createNotification() {
        // 创建打开主活动的PendingIntent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        // 获取当前播放的歌曲信息
        String title = "未播放";
        String artist = "";
        if (currentSongIndex >= 0 && currentSongIndex < playlist.size()) {
            Song currentSong = playlist.get(currentSongIndex);
            title = currentSong.getTitle();
            artist = currentSong.getArtist();
        }
        
        // 构建通知
        return new NotificationCompat.Builder(this, MusicPlayerApplication.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    /**
     * 设置播放列表
     */
    public void setPlaylist(List<Song> songs, int startIndex) {
        playlist.clear();
        playlist.addAll(songs);
        
        if (playbackListener != null) {
            playbackListener.onPlaylistChanged(playlist);
        }
        
        if (startIndex >= 0 && startIndex < playlist.size()) {
            playSong(startIndex);
        }
    }

    /**
     * 播放指定索引的歌曲
     */
    public void playSong(int index) {
        if (index >= 0 && index < playlist.size()) {
            currentSongIndex = index;
            Song song = playlist.get(index);
            
            // 请求音频焦点
            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result = audioManager.requestAudioFocus(audioFocusRequest);
            } else {
                result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
            
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // 准备并播放音乐
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(song.getPath())));
                exoPlayer.prepare();
                exoPlayer.play();
                isPlaying = true;
                
                // 更新媒体会话状态
                updatePlaybackState();
                
                // 更新通知
                startForeground(1, createNotification());
                
                // 通知监听器
                if (playbackListener != null) {
                    playbackListener.onSongChanged(song);
                    playbackListener.onPlaybackStateChanged(true);
                }
            }
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (exoPlayer.isPlaying()) {
            exoPlayer.pause();
            isPlaying = false;
            
            // 更新媒体会话状态
            updatePlaybackState();
            
            // 通知监听器
            if (playbackListener != null) {
                playbackListener.onPlaybackStateChanged(false);
            }
        }
    }

    /**
     * 恢复播放
     */
    public void resume() {
        if (!exoPlayer.isPlaying()) {
            // 请求音频焦点
            int result;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result = audioManager.requestAudioFocus(audioFocusRequest);
            } else {
                result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
            
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                exoPlayer.play();
                isPlaying = true;
                
                // 更新媒体会话状态
                updatePlaybackState();
                
                // 通知监听器
                if (playbackListener != null) {
                    playbackListener.onPlaybackStateChanged(true);
                }
            }
        }
    }

    /**
     * 播放下一首
     */
    public void playNext() {
        if (playlist.isEmpty()) return;
        
        int nextIndex;
        switch (currentPlayMode) {
            case REPEAT_ONE:
                // 单曲循环模式下，继续播放当前歌曲
                nextIndex = currentSongIndex;
                break;
            case SHUFFLE:
                // 随机播放模式下，随机选择一首歌曲
                nextIndex = (int) (Math.random() * playlist.size());
                break;
            case REPEAT_ALL:
                // 列表循环模式下，播放下一首，如果是最后一首则回到第一首
                nextIndex = (currentSongIndex + 1) % playlist.size();
                break;
            case NORMAL:
            default:
                // 顺序播放模式下，播放下一首，如果是最后一首则停止
                nextIndex = currentSongIndex + 1;
                if (nextIndex >= playlist.size()) {
                    nextIndex = 0; // 循环回第一首
                }
                break;
        }
        
        playSong(nextIndex);
    }

    /**
     * 播放上一首
     */
    public void playPrevious() {
        if (playlist.isEmpty()) return;
        
        // 如果当前播放进度超过3秒，则重新播放当前歌曲
        if (exoPlayer.getCurrentPosition() > 3000) {
            exoPlayer.seekTo(0);
            return;
        }
        
        int prevIndex;
        switch (currentPlayMode) {
            case REPEAT_ONE:
                // 单曲循环模式下，继续播放当前歌曲
                prevIndex = currentSongIndex;
                break;
            case SHUFFLE:
                // 随机播放模式下，随机选择一首歌曲
                prevIndex = (int) (Math.random() * playlist.size());
                break;
            case REPEAT_ALL:
            case NORMAL:
            default:
                // 列表循环和顺序播放模式下，播放上一首，如果是第一首则跳到最后一首
                prevIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
                break;
        }
        
        playSong(prevIndex);
    }

    /**
     * 设置播放模式
     */
    public void setPlayMode(PlayMode playMode) {
        currentPlayMode = playMode;
    }

    /**
     * 获取当前播放模式
     */
    public PlayMode getPlayMode() {
        return currentPlayMode;
    }

    /**
     * 获取当前播放的歌曲
     */
    public Song getCurrentSong() {
        if (currentSongIndex >= 0 && currentSongIndex < playlist.size()) {
            return playlist.get(currentSongIndex);
        }
        return null;
    }

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 获取当前播放进度（毫秒）
     */
    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    /**
     * 获取当前歌曲总时长（毫秒）
     */
    public long getDuration() {
        return exoPlayer.getDuration();
    }

    /**
     * 跳转到指定位置
     */
    public void seekTo(long position) {
        exoPlayer.seekTo(position);
    }

    /**
     * 设置播放状态监听器
     */
    public void setPlaybackListener(PlaybackListener listener) {
        this.playbackListener = listener;
    }

    /**
     * 处理播放完成事件
     */
    private void handlePlaybackCompletion() {
        switch (currentPlayMode) {
            case REPEAT_ONE:
                // 单曲循环，重新播放当前歌曲
                exoPlayer.seekTo(0);
                exoPlayer.play();
                break;
            case NORMAL:
            case REPEAT_ALL:
            case SHUFFLE:
                // 其他模式，播放下一首
                playNext();
                break;
        }
    }

    /**
     * 更新媒体会话播放状态
     */
    private void updatePlaybackState() {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(
                        isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        exoPlayer.getCurrentPosition(),
                        1.0f
                );
        
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                // 长时间失去音频焦点，暂停播放
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // 暂时失去音频焦点，暂停播放
                pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                // 重新获得音频焦点，恢复播放
                resume();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // 释放ExoPlayer资源
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        
        // 释放MediaSession资源
        if (mediaSession != null) {
            mediaSession.release();
        }
        
        // 放弃音频焦点
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    /**
     * 服务绑定器
     */
    public class MusicBinder extends Binder {
        public MusicPlaybackService getService() {
            return MusicPlaybackService.this;
        }
    }
}