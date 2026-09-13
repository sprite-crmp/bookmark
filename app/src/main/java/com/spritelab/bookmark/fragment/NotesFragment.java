package com.spritelab.bookmark.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.adapter.BookmarkAdapter;
import com.spritelab.bookmark.model.BookmarkModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
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
        loadFromJson();
    }

    public static void saveToJson(Context context, List<BookmarkModel> bookmarks) {
        if (context == null) return;
        File file = new File(context.getExternalFilesDir(null), "bookmarks.json");
        Gson gson = new Gson();
        String json = gson.toJson(bookmarks);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(json.getBytes());
            Log.i(TAG, "Закладки сохранены");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromJson() {
        if (getContext() == null) return;
        File file = new File(getContext().getExternalFilesDir(null), "bookmarks.json");
        if (!file.exists()) return;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            String json = new String(data);

            Gson gson = new Gson();
            Type type = new TypeToken<List<BookmarkModel>>(){}.getType();
            List<BookmarkModel> loaded = gson.fromJson(json, type);

            if (loaded != null) {
                bookmarks.clear();
                bookmarks.addAll(loaded);
                if (adapter != null) adapter.notifyDataSetChanged();
            }

            Log.i(TAG, "Данные загружены из bookmarks.json");
        } catch (IOException e) {
            Log.e(TAG, "Ошибка загрузки bookmarks.json: " + e.getMessage());
            e.printStackTrace();
        }
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
                saveToJson(getContext(), bookmarks);
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

        bookmarks.add(new BookmarkModel(name, "Дата создания: " + dateTime));
        if (adapter != null) adapter.notifyDataSetChanged();
        saveToJson(getContext(), bookmarks);
    }
}