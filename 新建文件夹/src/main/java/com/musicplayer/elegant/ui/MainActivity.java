package com.musicplayer.elegant.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicplayer.elegant.R;
import com.musicplayer.elegant.model.Song;
import com.musicplayer.elegant.service.MusicPlaybackService;
import com.musicplayer.elegant.ui.fragments.AlbumsFragment;
import com.musicplayer.elegant.ui.fragments.HomeFragment;
import com.musicplayer.elegant.ui.fragments.PlaylistsFragment;
import com.musicplayer.elegant.ui.fragments.SongsFragment;
import com.musicplayer.elegant.viewmodel.MusicPlayerViewModel;

/**
 * 主活动，包含底部导航栏和音乐播放控制器
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private MusicPlayerViewModel viewModel;
    private BottomNavigationView navigationView;
    private View miniPlayerView;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    
    // 迷你播放器控件
    private ImageView albumArtImageView;
    private TextView songTitleTextView;
    private TextView artistNameTextView;
    private FloatingActionButton playPauseButton;
    
    // 音乐服务
    private MusicPlaybackService musicService;
    private boolean serviceBound = false;
    
    // 服务连接
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            serviceBound = true;
            
            // 将服务实例传递给ViewModel
            viewModel.setMusicService(musicService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(MusicPlayerViewModel.class);
        
        // 初始化视图
        initViews();
        
        // 设置底部导航
        setupBottomNavigation();
        
        // 设置迷你播放器
        setupMiniPlayer();
        
        // 绑定音乐服务
        bindMusicService();
        
        // 默认显示首页
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
        
        // 观察当前播放歌曲变化
        observeCurrentSong();
    }

    private void initViews() {
        navigationView = findViewById(R.id.bottom_navigation);
        miniPlayerView = findViewById(R.id.mini_player);
        albumArtImageView = findViewById(R.id.mini_player_album_art);
        songTitleTextView = findViewById(R.id.mini_player_song_title);
        artistNameTextView = findViewById(R.id.mini_player_artist_name);
        playPauseButton = findViewById(R.id.mini_player_play_pause);
        
        // 初始化底部播放器行为
        bottomSheetBehavior = BottomSheetBehavior.from(miniPlayerView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setupBottomNavigation() {
        navigationView.setOnNavigationItemSelectedListener(this);
    }

    private void setupMiniPlayer() {
        // 设置迷你播放器点击事件，打开播放详情
        miniPlayerView.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayDetailActivity.class);
            startActivity(intent);
        });
        
        // 设置播放/暂停按钮点击事件
        playPauseButton.setOnClickListener(v -> {
            if (musicService != null) {
                if (musicService.isPlaying()) {
                    musicService.pause();
                    playPauseButton.setImageResource(R.drawable.ic_play);
                } else {
                    musicService.resume();
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    private void bindMusicService() {
        Intent intent = new Intent(this, MusicPlaybackService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void observeCurrentSong() {
        viewModel.getCurrentSong().observe(this, song -> {
            if (song != null) {
                updateMiniPlayer(song);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
        
        viewModel.isPlaying().observe(this, isPlaying -> {
            if (isPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.ic_play);
            }
        });
    }

    private void updateMiniPlayer(Song song) {
        songTitleTextView.setText(song.getTitle());
        artistNameTextView.setText(song.getArtist());
        
        // 加载专辑封面
        Glide.with(this)
                .load(song.getAlbumArtUri())
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(albumArtImageView);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_songs) {
            fragment = new SongsFragment();
        } else if (itemId == R.id.nav_albums) {
            fragment = new AlbumsFragment();
        } else if (itemId == R.id.nav_playlists) {
            fragment = new PlaylistsFragment();
        }
        
        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }
}