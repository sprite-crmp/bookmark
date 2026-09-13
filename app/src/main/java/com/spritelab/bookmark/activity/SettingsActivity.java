package com.spritelab.bookmark.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.utils.DatabaseHelper;
import com.spritelab.bookmark.utils.HelpUtils;

public class SettingsActivity extends AppCompatActivity {
    FrameLayout btnChoiceTheme;
    ImageView btnHome, imgHeart;
    TextView tvAutor;

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
                DatabaseHelper.getInstance(this).setTheme(0);
                return false;
            }).setCancelButton((dialog, v) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                DatabaseHelper.getInstance(this).setTheme(1);
                return false;
            }).setOtherButton((dialog, v) -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                DatabaseHelper.getInstance(this).setTheme(2);
                return false;
            });
        }, null);
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
