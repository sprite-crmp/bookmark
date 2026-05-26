package com.spritelab.bookmark.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.style.IOSStyle;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.adapter.BookmarkAdapter;
import com.spritelab.bookmark.model.BookmarkModel;
import com.spritelab.bookmark.utils.HelpUtils;

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

public class MainActivity extends AppCompatActivity {

    ImageView btnSettings;
    RecyclerView rvBookMarks;
    EditText etBookMark;
    ImageView btnSend;

    private static final String TAG = "class:MainActivity";
    private boolean cooldown;
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
        btnSend = findViewById(R.id.btnSend);

        listeners();
        setupRecyclerView();
        loadFromJson();
        initDialogX();
    }

    private void listeners(){
        HelpUtils.setupDropAnimation(btnSend, false, () -> {
            String bookmarkText = etBookMark.getText().toString().trim();
            if (bookmarkText.isEmpty()) {
                if (!cooldown) {
                    cooldown = true;
                    new CountDownTimer(3000, 1000) {
                        @Override
                        public void onFinish() {
                            cooldown = false;
                        }

                        @Override
                        public void onTick(long millisUntilFinished) {

                        }
                    }.start();
                    PopTip.show("Введите закладку").iconWarning();
                }
                return;
            }

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String dateTime = sdf.format(now);

            addNewBookMark(bookmarkText, dateTime);
            etBookMark.setText("");
        }, () -> {});
    }

    public static void saveToJson(Context context, List<BookmarkModel> bookmarks) {
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
        File file = new File(getExternalFilesDir(null), "bookmarks.json");
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
            Log.e(TAG, "Ошибка загрузки: " + e.getMessage());
            e.printStackTrace();
        }
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

    private void addNewBookMark(String name, String date){
        bookmarks.add(new BookmarkModel(name, "Дата создания: " + date));
        if (adapter != null) adapter.notifyDataSetChanged();
        saveToJson(this, bookmarks);
    }
}