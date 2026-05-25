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
import com.spritelab.bookmark.model.BookmarkModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView btnSettings;
    RecyclerView rvBookMarks;
    EditText etBookMark;

    private BookmarkAdapter adapter;
    private final List<BookmarkModel> bookmarks = new ArrayList<>();

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
            public void onItemClick(BookmarkModel bookmark, int position) {
                Toast.makeText(MainActivity.this, "Clicked: " + bookmark.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        rvBookMarks.setAdapter(adapter);
    }

    private void loadSampleData() {
        bookmarks.add(new BookmarkModel("Google", "25.05.2026"));
        bookmarks.add(new BookmarkModel("StackOverflow", "24.05.2026"));
        bookmarks.add(new BookmarkModel("GitHub", "23.05.2026"));
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}