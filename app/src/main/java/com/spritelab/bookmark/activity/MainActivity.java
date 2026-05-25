package com.spritelab.bookmark.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.style.IOSStyle;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.adapter.BookmarkAdapter;
import com.spritelab.bookmark.model.BookmarkModel;
import com.spritelab.bookmark.utils.HelpUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView btnSettings;
    RecyclerView rvBookMarks;
    EditText etBookMark;

    private BookmarkAdapter adapter;
    private ItemTouchHelper itemTouchHelper;
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
        initDialogX();
    }

    private void initDialogX() {
        DialogX.init(this);
        DialogX.globalTheme = DialogX.THEME.DARK;
        DialogX.globalStyle = new IOSStyle();
        DialogX.backgroundColor = Color.parseColor("#222222");
    }

    private void setupRecyclerView() {
        rvBookMarks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookmarkAdapter(this, bookmarks);
        rvBookMarks.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                adapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvBookMarks);
        adapter.setTouchHelper(itemTouchHelper);
    }

    private void loadSampleData() {
        bookmarks.add(new BookmarkModel("Google", "25.05.2026"));
        bookmarks.add(new BookmarkModel("StackOverflow", "24.05.2026"));
        bookmarks.add(new BookmarkModel("GitHub", "23.05.2026"));
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}