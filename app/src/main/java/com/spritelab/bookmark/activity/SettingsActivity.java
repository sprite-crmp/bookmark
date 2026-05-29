package com.spritelab.bookmark.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.Gson;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.model.ConfigModel;
import com.spritelab.bookmark.utils.HelpUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    FrameLayout btnChoiceTheme;
    ImageView btnHome;

    private static final String TAG = "class:SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnHome = findViewById(R.id.btnHome);
        btnChoiceTheme = findViewById(R.id.btnChoiceTheme);

        listeners();
        backPressed();
    }

    private void listeners(){
        HelpUtils.setupDropAnimation(btnHome, false, this::startMain, null);
        HelpUtils.setupDropAnimation(btnChoiceTheme, false, () -> {
            MessageDialog.show("Подтверждение", "Какую тему вы желаете выбрать?",
                    "Светлая","Темная", "Системная").setOkButton((dialog, v) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                saveTheme(0);
                return false;
            }).setCancelButton((dialog, v) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                saveTheme(1);
                return false;
            }).setOtherButton((dialog, v) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                saveTheme(2);
                return false;
            });
        }, null);
    }

    private void saveTheme(int theme) {
        ConfigModel model = new ConfigModel();
        model.setTheme(theme);

        List<ConfigModel> list = new ArrayList<>();
        list.add(model);

        String json = new Gson().toJson(list);

        try (FileOutputStream fos = new FileOutputStream(new File(getExternalFilesDir(null), "config.json"))) {
            fos.write(json.getBytes());
            Log.i(TAG, "Тема сохранена: " + theme);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void  backPressed(){
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startMain();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void startMain() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}