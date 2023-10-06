package com.example.adiuvo;

import android.media.browse.MediaBrowser;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Adapter.GalleryAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    private List<MediaItem> mediaItems;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GalleryFragment newInstance(String param1, String param2) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        mediaItems = loadMediaItems(); // Load images and videos from storage

        galleryAdapter = new GalleryAdapter(mediaItems);
        recyclerView.setAdapter(galleryAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Set up click listener for items
        galleryAdapter.setOnItemClickListener(new GalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Open full-screen view or modal for the selected item
            }

            @Override
            public void onDeleteClick(int position) {
                // Delete the item at position
                mediaItems.remove(position);
                galleryAdapter.notifyItemRemoved(position);
            }
        });

        return view;
    }

    // Load images and videos from storage (this method needs to be implemented)
    private List<MediaItem> loadMediaItems() {
        List<MediaItem> items = new ArrayList<>();

        // Assuming you have a directory where your media items are stored.
        File mediaDirectory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "YourAppFolder");

        if (mediaDirectory.exists() && mediaDirectory.isDirectory()) {
            File[] files = mediaDirectory.listFiles();

            if (files != null) {
                for (File file : files) {
                    // Check if it's an image or video based on file extension
                    String path = file.getAbsolutePath();
                    boolean isVideo = path.endsWith(".mp4"); // Example: Check if the file is a video

                    // Add the media item to the list
                    items.add(new MediaItem(path, isVideo));
                }
            }
        }

        return items;
    }
}