package com.spritelab.bookmark.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spritelab.bookmark.R;
import com.spritelab.bookmark.adapter.BookmarkAdapter;
import com.spritelab.bookmark.model.Bookmark;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView btnSettings;
    RecyclerView rvBookMarks;
    EditText etBookMark;

    private BookmarkAdapter adapter;
    private final List<Bookmark> bookmarks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSettings = findViewById(R.id.btnSettings);
        rvBookMarks = findViewById(R.id.rvBookMarks);
        etBookMark = findViewById(R.id.etBookMark);

        setupRecyclerView();
        loadSampleData();
    }

    private void setupRecyclerView() {
        rvBookMarks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookmarkAdapter(this, bookmarks, new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Bookmark bookmark, int position) {
                Toast.makeText(MainActivity.this, "Clicked: " + bookmark.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        rvBookMarks.setAdapter(adapter);
    }

    private void loadSampleData() {
        bookmarks.add(new Bookmark("Google", "25.05.2026", "https://www.google.com"));
        bookmarks.add(new Bookmark("StackOverflow", "24.05.2026", "https://stackoverflow.com"));
        bookmarks.add(new Bookmark("GitHub", "23.05.2026", "https://github.com"));
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}