package com.spritelab.bookmark.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spritelab.bookmark.R;
import com.spritelab.bookmark.adapter.BookmarkAdapter;
import com.spritelab.bookmark.utils.DatabaseHelper;
import com.spritelab.bookmark.model.BookmarkModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesFragment extends Fragment {

    RecyclerView rvBookMarks;

    private static final String TAG = "class:NotesFragment";
    private BookmarkAdapter adapter;
    private ItemTouchHelper itemTouchHelper;
    private final List<BookmarkModel> bookmarks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvBookMarks = view.findViewById(R.id.rvBookMarks);

        setupRecyclerView();
        loadFromDb();
    }

    private void loadFromDb() {
        if (getContext() == null) return;
        List<BookmarkModel> loaded = DatabaseHelper.getInstance(getContext()).getAllBookmarks();
        bookmarks.clear();
        bookmarks.addAll(loaded);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        rvBookMarks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BookmarkAdapter(getContext(), bookmarks);
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
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Save the new order to DB
                DatabaseHelper.getInstance(getContext()).updateBookmarksOrder(bookmarks);
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

    public void addNewBookMark(String name) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String dateTime = sdf.format(now);

        BookmarkModel newBookmark = new BookmarkModel(name, "Дата создания: " + dateTime);
        DatabaseHelper.getInstance(getContext()).addBookmark(newBookmark);
        
        // Refresh list from DB to get the ID
        loadFromDb();
    }
}
