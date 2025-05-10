package com.musicplayer.elegant;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.musicplayer.elegant.data.MusicRepository;

/**
 * 应用程序类，负责初始化应用程序级别的组件
 */
public class MusicPlayerApplication extends Application {

    public static final String CHANNEL_ID = "music_playback_channel";
    private MusicRepository musicRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化音乐仓库
        musicRepository = new MusicRepository(this);
        
        // 创建通知渠道（Android 8.0及以上需要）
        createNotificationChannel();
    }

    /**
     * 创建通知渠道，用于音乐播放服务的前台通知
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "音乐播放",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("用于显示当前播放的音乐");
            channel.setShowBadge(false);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 获取音乐仓库实例
     */
    public MusicRepository getMusicRepository() {
        return musicRepository;
    }
}