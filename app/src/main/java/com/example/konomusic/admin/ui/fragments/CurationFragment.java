package com.example.konomusic.admin.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.konomusic.R;

/**
 * CurationFragment - Quản lý curation queue
 */
public class CurationFragment extends Fragment {

    private ListView listViewCurations;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadCurations();
    }

    /**
     * Khởi tạo các view
     */
    private void initViews(View view) {
        listViewCurations = view.findViewById(R.id.list_pending_curations);

        // Setup adapter
        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1);
        listViewCurations.setAdapter(adapter);
    }

    /**
     * Load curation items từ API
     */
    private void loadCurations() {
        // TODO: Gọi API GET /api/admin/curation/pending

        // Sample data
        adapter.add("Video 1 - Pending");
        adapter.add("Video 2 - Pending");
        adapter.add("Video 3 - Pending");

        Toast.makeText(getContext(), "Loaded pending curations", Toast.LENGTH_SHORT).show();
    }

    /**
     * Phê duyệt video
     */
    public void approveCuration(Long curationId) {
        // TODO: POST /api/admin/curation/{id}/approve
        Toast.makeText(getContext(), "Curation approved!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Từ chối video
     */
    public void rejectCuration(Long curationId) {
        // TODO: POST /api/admin/curation/{id}/reject
        Toast.makeText(getContext(), "Curation rejected!", Toast.LENGTH_SHORT).show();
    }

}

