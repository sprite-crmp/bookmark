package com.spritelab.bookmark.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.style.IOSStyle;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.fragment.NotesFragment;
import com.spritelab.bookmark.model.ConfigModel;
import com.spritelab.bookmark.utils.HelpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView btnSettings;
    EditText etBookMark;
    ImageView btnSend;
    CardView cardNotes, cardTasks;

    private static final String TAG = "class:MainActivity";
    private boolean cooldown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSettings = findViewById(R.id.btnSettings);
        etBookMark = findViewById(R.id.etBookMark);
        btnSend = findViewById(R.id.btnSend);
        cardNotes = findViewById(R.id.card_notes);
        cardTasks = findViewById(R.id.card_tasks);

        loadConfig();
        initDialogX();
        listeners();
        updateTabs(true);
        replaceFragment(new NotesFragment());
    }

    private void listeners() {
        HelpUtils.setupDropAnimation(btnSettings, false, () -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        }, () -> {});

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

            NotesFragment fragment = (NotesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
            if (fragment != null) {
                fragment.addNewBookMark(bookmarkText);
                etBookMark.setText("");
            }
        }, () -> {});

        HelpUtils.setupDropAnimation(cardNotes, false, () -> {
            updateTabs(true);
            replaceFragment(new NotesFragment());
        }, () -> {});

        HelpUtils.setupDropAnimation(cardTasks, false, () -> {
            updateTabs(false);
            // Если в будущем появится TasksFragment, здесь можно будет вызывать: replaceFragment(new TasksFragment());
        }, () -> {});
    }

    private void updateTabs(boolean isNotesActive) {
        int activeColor = ContextCompat.getColor(this, R.color.aluminum);
        int transparentColor = ContextCompat.getColor(this, R.color.transparent);

        if (isNotesActive) {
            cardNotes.setCardBackgroundColor(activeColor);
            cardTasks.setCardBackgroundColor(transparentColor);
        } else {
            cardNotes.setCardBackgroundColor(transparentColor);
            cardTasks.setCardBackgroundColor(activeColor);
        }
    }

    private void loadConfig() {
        File file = new File(getExternalFilesDir(null), "config.json");
        if (!file.exists()) return;

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            String json = new String(data);

            Gson gson = new Gson();
            ConfigModel[] loaded = gson.fromJson(json, ConfigModel[].class);

            if (loaded.length > 0) {
                int theme = loaded[0].getTheme();
                switch(theme) {
                    case 0:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case 1:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case 2:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Ошибка загрузки: " + e.getMessage());
        }
    }

    private void initDialogX() {
        DialogX.init(this);
        DialogX.globalTheme = DialogX.THEME.DARK;
        DialogX.globalStyle = new IOSStyle();
        DialogX.backgroundColor = Color.parseColor("#222222");
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit();
    }
}
