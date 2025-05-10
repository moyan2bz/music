package com.musicplayer.elegant.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicplayer.elegant.R;
import com.musicplayer.elegant.model.Song;
import com.musicplayer.elegant.service.MusicPlaybackService;
import com.musicplayer.elegant.viewmodel.MusicPlayerViewModel;

/**
 * 播放详情页面，显示当前播放歌曲的详细信息和控制选项
 */
public class PlayDetailActivity extends AppCompatActivity {

    private MusicPlayerViewModel viewModel;
    
    // UI组件
    private ImageView albumArtImageView;
    private TextView songTitleTextView;
    private TextView artistNameTextView;
    private SeekBar seekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private FloatingActionButton playPauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton playModeButton;
    private ImageButton favoriteButton;
    
    // 播放模式图标资源
    private final int[] playModeIcons = {
            R.drawable.ic_repeat,       // 顺序播放
            R.drawable.ic_repeat_one,   // 单曲循环
            R.drawable.ic_repeat_all,   // 列表循环
            R.drawable.ic_shuffle       // 随机播放
    };
    
    // 用于更新进度条的Handler
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
            handler.postDelayed(this, 1000); // 每秒更新一次
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_detail);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(MusicPlayerViewModel.class);
        
        // 初始化视图
        initViews();
        
        // 设置监听器
        setupListeners();
        
        // 观察当前播放歌曲变化
        observeCurrentSong();
        
        // 开始更新进度条
        handler.post(updateSeekBarRunnable);
    }

    private void initViews() {
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        
        // 初始化UI组件
        albumArtImageView = findViewById(R.id.album_art);
        songTitleTextView = findViewById(R.id.song_title);
        artistNameTextView = findViewById(R.id.artist_name);
        seekBar = findViewById(R.id.seek_bar);
        currentTimeTextView = findViewById(R.id.current_time);
        totalTimeTextView = findViewById(R.id.total_time);
        playPauseButton = findViewById(R.id.play_pause_button);
        previousButton = findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);
        playModeButton = findViewById(R.id.play_mode_button);
        favoriteButton = findViewById(R.id.favorite_button);
    }

    private void setupListeners() {
        // 工具栏返回按钮点击事件
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // 播放/暂停按钮点击事件
        playPauseButton.setOnClickListener(v -> viewModel.togglePlayPause());
        
        // 上一首按钮点击事件
        previousButton.setOnClickListener(v -> viewModel.playPrevious());
        
        // 下一首按钮点击事件
        nextButton.setOnClickListener(v -> viewModel.playNext());
        
        // 播放模式按钮点击事件
        playModeButton.setOnClickListener(v -> {
            MusicPlaybackService.PlayMode currentMode = viewModel.getPlayMode();
            MusicPlaybackService.PlayMode nextMode;
            
            // 切换到下一个播放模式
            switch (currentMode) {
                case NORMAL:
                    nextMode = MusicPlaybackService.PlayMode.REPEAT_ONE;
                    break;
                case REPEAT_ONE:
                    nextMode = MusicPlaybackService.PlayMode.REPEAT_ALL;
                    break;
                case REPEAT_ALL:
                    nextMode = MusicPlaybackService.PlayMode.SHUFFLE;
                    break;
                case SHUFFLE:
                default:
                    nextMode = MusicPlaybackService.PlayMode.NORMAL;
                    break;
            }
            
            viewModel.setPlayMode(nextMode);
            updatePlayModeIcon(nextMode);
        });
        
        // 收藏按钮点击事件
        favoriteButton.setOnClickListener(v -> {
            // 切换收藏状态
            boolean isFavorite = favoriteButton.getTag() != null && (boolean) favoriteButton.getTag();
            setFavoriteState(!isFavorite);
            // 实际应用中应该将收藏状态保存到数据库
        });
        
        // 进度条拖动事件
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // 用户拖动进度条时更新当前时间显示
                    currentTimeTextView.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 开始拖动时暂停更新进度条
                handler.removeCallbacks(updateSeekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 停止拖动时，将播放位置设置为拖动位置
                viewModel.seekTo(seekBar.getProgress());
                // 恢复更新进度条
                handler.post(updateSeekBarRunnable);
            }
        });
    }

    private void observeCurrentSong() {
        // 观察当前播放的歌曲
        viewModel.getCurrentSong().observe(this, this::updateSongInfo);
        
        // 观察播放状态
        viewModel.isPlaying().observe(this, isPlaying -> {
            if (isPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.ic_play);
            }
        });
    }

    private void updateSongInfo(Song song) {
        if (song != null) {
            // 更新歌曲信息
            songTitleTextView.setText(song.getTitle());
            artistNameTextView.setText(song.getArtist());
            
            // 加载专辑封面
            Glide.with(this)
                    .load(song.getAlbumArtUri())
                    .placeholder(R.drawable.default_album_art)
                    .error(R.drawable.default_album_art)
                    .into(albumArtImageView);
            
            // 更新进度条最大值
            long duration = viewModel.getDuration();
            seekBar.setMax((int) duration);
            totalTimeTextView.setText(formatTime(duration));
            
            // 更新播放模式图标
            updatePlayModeIcon(viewModel.getPlayMode());
        }
    }

    private void updateSeekBar() {
        long currentPosition = viewModel.getCurrentPosition();
        seekBar.setProgress((int) currentPosition);
        currentTimeTextView.setText(formatTime(currentPosition));
    }

    private void updatePlayModeIcon(MusicPlaybackService.PlayMode playMode) {
        int iconResId;
        switch (playMode) {
            case REPEAT_ONE:
                iconResId = playModeIcons[1];
                break;
            case REPEAT_ALL:
                iconResId = playModeIcons[2];
                break;
            case SHUFFLE:
                iconResId = playModeIcons[3];
                break;
            case NORMAL:
            default:
                iconResId = playModeIcons[0];
                break;
        }
        playModeButton.setImageResource(iconResId);
    }

    private void setFavoriteState(boolean isFavorite) {
        favoriteButton.setTag(isFavorite);
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.ic_favorite);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    /**
     * 格式化时间为分:秒格式
     */
    private String formatTime(long timeMs) {
        long minutes = timeMs / 60000;
        long seconds = (timeMs % 60000) / 1000;
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止更新进度条
        handler.removeCallbacks(updateSeekBarRunnable);
    }
}