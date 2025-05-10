package com.musicplayer.elegant.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.musicplayer.elegant.R;
import com.musicplayer.elegant.adapter.SongAdapter;
import com.musicplayer.elegant.model.Song;
import com.musicplayer.elegant.viewmodel.MusicPlayerViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页Fragment，显示最近播放、收藏和最近添加的歌曲
 */
public class HomeFragment extends Fragment {

    private MusicPlayerViewModel viewModel;
    private RecyclerView recentlyPlayedRecyclerView;
    private RecyclerView favoritesRecyclerView;
    private RecyclerView recentlyAddedRecyclerView;
    private SongAdapter recentlyPlayedAdapter;
    private SongAdapter favoritesAdapter;
    private SongAdapter recentlyAddedAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(MusicPlayerViewModel.class);
        
        // 初始化RecyclerView
        initRecyclerViews(view);
        
        // 加载数据
        loadData();
    }

    private void initRecyclerViews(View view) {
        // 最近播放
        recentlyPlayedRecyclerView = view.findViewById(R.id.recently_played_recycler_view);
        recentlyPlayedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recentlyPlayedAdapter = new SongAdapter(requireContext(), new ArrayList<>(), song -> {
            // 播放选中的歌曲
            viewModel.playSong(song, recentlyPlayedAdapter.getSongs());
        });
        recentlyPlayedRecyclerView.setAdapter(recentlyPlayedAdapter);
        
        // 收藏
        favoritesRecyclerView = view.findViewById(R.id.favorites_recycler_view);
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        favoritesAdapter = new SongAdapter(requireContext(), new ArrayList<>(), song -> {
            // 播放选中的歌曲
            viewModel.playSong(song, favoritesAdapter.getSongs());
        });
        favoritesRecyclerView.setAdapter(favoritesAdapter);
        
        // 最近添加
        recentlyAddedRecyclerView = view.findViewById(R.id.recently_added_recycler_view);
        recentlyAddedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recentlyAddedAdapter = new SongAdapter(requireContext(), new ArrayList<>(), song -> {
            // 播放选中的歌曲
            viewModel.playSong(song, recentlyAddedAdapter.getSongs());
        });
        recentlyAddedRecyclerView.setAdapter(recentlyAddedAdapter);
    }

    private void loadData() {
        // 加载最近播放的歌曲（示例数据，实际应用中应该从数据库获取）
        List<Song> recentlyPlayed = new ArrayList<>();
        viewModel.getAllSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null && !songs.isEmpty()) {
                // 简单示例：使用前几首歌曲作为最近播放
                int count = Math.min(10, songs.size());
                recentlyPlayed.addAll(songs.subList(0, count));
                recentlyPlayedAdapter.updateSongs(recentlyPlayed);
            }
        });
        
        // 加载收藏的歌曲
        viewModel.getFavoriteSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                favoritesAdapter.updateSongs(songs);
            }
        });
        
        // 加载最近添加的歌曲
        viewModel.getRecentSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                recentlyAddedAdapter.updateSongs(songs);
            }
        });
    }
}