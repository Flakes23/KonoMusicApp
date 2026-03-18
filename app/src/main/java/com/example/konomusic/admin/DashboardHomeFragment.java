package com.example.konomusic.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.konomusic.R;
import com.example.konomusic.adapter.RecentActivityAdapter;
import com.example.konomusic.adapter.StatCardAdapter;
import com.example.konomusic.adapter.TopSongAdapter;
import com.example.konomusic.model.RecentActivity;
import com.example.konomusic.model.StatCard;
import com.example.konomusic.model.TopSong;

import java.util.ArrayList;
import java.util.List;

public class DashboardHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupStatCards(view);
        setupRecentActivity(view);
        setupTopSongs(view);
        setupQuickActions(view);
    }

    private void setupStatCards(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_stat_cards);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<StatCard> cards = new ArrayList<>();
        cards.add(new StatCard(
                R.drawable.ic_music_note, "Tổng bài hát", "2,486",
                0xFFFF6B6B, "+12%", true));
        cards.add(new StatCard(
                R.drawable.ic_people, "Người dùng", "10,241",
                0xFF4ECDC4, "+8%", true));
        cards.add(new StatCard(
                R.drawable.ic_album, "Album", "347",
                0xFFFFE66D, "+3%", true));
        cards.add(new StatCard(
                R.drawable.ic_artist, "Nghệ sĩ", "891",
                0xFFA29BFE, "-1%", false));

        rv.setAdapter(new StatCardAdapter(cards));
    }

    private void setupRecentActivity(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_recent_activity);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<RecentActivity> activities = new ArrayList<>();
        activities.add(new RecentActivity(
                "Bài hát mới được tải lên",
                "\"Hẹn Ước\" · Sơn Tùng M-TP",
                "2 phút trước",
                RecentActivity.TYPE_UPLOAD));
        activities.add(new RecentActivity(
                "Người dùng mới đăng ký",
                "nguyen_van_a@gmail.com",
                "5 phút trước",
                RecentActivity.TYPE_USER));
        activities.add(new RecentActivity(
                "Album được cập nhật",
                "\"Skyler\" · Hoàng Thùy Linh",
                "18 phút trước",
                RecentActivity.TYPE_EDIT));
        activities.add(new RecentActivity(
                "Bài hát bị xóa",
                "\"Bản Nhạc Lỗi\" · Unknown",
                "45 phút trước",
                RecentActivity.TYPE_DELETE));
        activities.add(new RecentActivity(
                "Nghệ sĩ mới được thêm",
                "HIEUTHUHAI · Hip-hop/Rap",
                "1 giờ trước",
                RecentActivity.TYPE_UPLOAD));
        activities.add(new RecentActivity(
                "Người dùng mới đăng ký",
                "tran_thi_b@yahoo.com",
                "2 giờ trước",
                RecentActivity.TYPE_USER));

        rv.setAdapter(new RecentActivityAdapter(activities));
    }

    private void setupTopSongs(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_top_songs);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<TopSong> songs = new ArrayList<>();
        songs.add(new TopSong(1, "Chúng Ta Của Hiện Tại", "Sơn Tùng M-TP", "2.4M"));
        songs.add(new TopSong(2, "Waiting For You", "MONO", "1.9M"));
        songs.add(new TopSong(3, "Nàng Thơ", "Hoàng Duyên", "1.6M"));
        songs.add(new TopSong(4, "Ngủ Một Mình", "HIEUTHUHAI", "1.3M"));
        songs.add(new TopSong(5, "Có Chắc Yêu Là Đây", "Sơn Tùng M-TP", "1.1M"));

        rv.setAdapter(new TopSongAdapter(songs));
    }

    private void setupQuickActions(View view) {
        LinearLayout actionAddSong   = view.findViewById(R.id.action_add_song);
        LinearLayout actionAddArtist = view.findViewById(R.id.action_add_artist);
        LinearLayout actionAddAlbum  = view.findViewById(R.id.action_add_album);
        LinearLayout actionManageUsers = view.findViewById(R.id.action_manage_users);

        actionAddSong.setOnClickListener(v ->
                Toast.makeText(getContext(), "➕ Thêm bài hát mới", Toast.LENGTH_SHORT).show());
        actionAddArtist.setOnClickListener(v ->
                Toast.makeText(getContext(), "➕ Thêm nghệ sĩ mới", Toast.LENGTH_SHORT).show());
        actionAddAlbum.setOnClickListener(v ->
                Toast.makeText(getContext(), "➕ Thêm album mới", Toast.LENGTH_SHORT).show());
        actionManageUsers.setOnClickListener(v ->
                Toast.makeText(getContext(), "👥 Quản lý người dùng", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.tv_see_all).setOnClickListener(v ->
                Toast.makeText(getContext(), "Xem tất cả hoạt động", Toast.LENGTH_SHORT).show());
    }
}

