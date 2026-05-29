package com.spritelab.bookmark.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
    ImageView btnHome, imgHeart;
    TextView tvAutor;

    private static final String TAG = "class:SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnHome = findViewById(R.id.btnHome);
        btnChoiceTheme = findViewById(R.id.btnChoiceTheme);
        imgHeart = findViewById(R.id.imgHeart);
        tvAutor = findViewById(R.id.tvAutor);

        imgHeart.setTranslationY(500f);

        listeners();
        backPressed();
    }

    private void animateHeart() {
        imgHeart.setTranslationY(500f);
        imgHeart.setAlpha(0f);
        imgHeart.setVisibility(View.VISIBLE);

        imgHeart.animate()
                .translationY(-5f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.8f))
                .withEndAction(() -> {
                    imgHeart.animate()
                            .translationYBy(-10f)
                            .setDuration(180)
                            .setInterpolator(new android.view.animation.CycleInterpolator(0.5f))
                            .withEndAction(() -> {
                                imgHeart.animate()
                                        .translationYBy(10f)
                                        .setDuration(180)
                                        .setInterpolator(new android.view.animation.CycleInterpolator(0.5f))
                                        .withEndAction(() -> {
                                            imgHeart.animate()
                                                    .translationYBy(-6f)
                                                    .setDuration(150)
                                                    .setInterpolator(new android.view.animation.CycleInterpolator(0.4f))
                                                    .withEndAction(() -> {
                                                        imgHeart.animate()
                                                                .translationYBy(6f)
                                                                .setDuration(150)
                                                                .setInterpolator(new android.view.animation.CycleInterpolator(0.4f))
                                                                .withEndAction(() -> {
                                                                    imgHeart.animate()
                                                                            .translationYBy(-3f)
                                                                            .setDuration(120)
                                                                            .setInterpolator(new android.view.animation.CycleInterpolator(0.3f))
                                                                            .withEndAction(() -> {
                                                                                imgHeart.animate()
                                                                                        .translationY(600f)
                                                                                        .alpha(0f)
                                                                                        .setDuration(1000)
                                                                                        .setInterpolator(new android.view.animation.AccelerateInterpolator(1.2f))
                                                                                        .withEndAction(() -> imgHeart.setVisibility(View.GONE));
                                                                            });
                                                                });
                                                    });
                                        });
                            });
                })
                .start();
    }

    private void listeners(){
        HelpUtils.setupDropAnimation(btnHome, false, this::startMain, null);
        HelpUtils.setupDropAnimation(tvAutor, true, this::animateHeart, null);
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