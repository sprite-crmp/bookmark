package com.spritelab.bookmark.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.style.IOSStyle;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.utils.DatabaseHelper;
import com.spritelab.bookmark.fragment.NotesFragment;
import com.spritelab.bookmark.fragment.TasksFragment;
import com.spritelab.bookmark.utils.HelpUtils;

public class MainActivity extends AppCompatActivity {

    ImageView btnSettings;
    EditText etBookMark;
    ImageView btnSend;
    View cardNotes, cardTasks;
    CardView bgNotes, bgTasks;

    private static final String TAG = "class:MainActivity";
    private boolean cooldown;
    private boolean isNotesSelected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSettings = findViewById(R.id.btnSettings);
        etBookMark = findViewById(R.id.etBookMark);
        btnSend = findViewById(R.id.btnSend);
        
        cardNotes = findViewById(R.id.card_notes);
        cardTasks = findViewById(R.id.card_tasks);
        bgNotes = findViewById(R.id.bg_notes);
        bgTasks = findViewById(R.id.bg_tasks);

        loadConfig();
        initDialogX();
        listeners();
        bgNotes.setAlpha(1f);
        bgTasks.setAlpha(0f);
        
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
            int activeFragment = getActiveFragment();
            if (bookmarkText.isEmpty()) {
                if (!cooldown) {
                    cooldown = true;
                    new CountDownTimer(3000, 1000) {
                        @Override
                        public void onFinish() {
                            cooldown = false;
                        }

                        @Override
                        public void onTick(long millisUntilFinished) {}
                    }.start();
                    if (activeFragment == 1) PopTip.show("Введите текст заметки").iconWarning();
                    if (activeFragment == 2) PopTip.show("Введите имя задачи").iconWarning();
                }
                return;
            }

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);

            if (activeFragment == 1 && fragment instanceof NotesFragment) {
                ((NotesFragment) fragment).addNewBookMark(bookmarkText);
                etBookMark.setText("");
            } else if (activeFragment == 2 && fragment instanceof TasksFragment) {
                // Future task implementation
            }

        }, () -> {});

        HelpUtils.setupDropAnimation(cardNotes, false, () -> {
            if (!isNotesSelected) {
                animateTabSwitch(true);
                replaceFragment(new NotesFragment());
            }
        }, () -> {});

        HelpUtils.setupDropAnimation(cardTasks, false, () -> {
            if (isNotesSelected) {
                animateTabSwitch(false);
                replaceFragment(new TasksFragment());
            }
        }, () -> {});
    }

    private void animateTabSwitch(boolean isNotesActive) {
        this.isNotesSelected = isNotesActive;
        long duration = 180;
        if (isNotesActive) etBookMark.setHint("Введите текст заметки");
        else etBookMark.setHint("Введите имя задачи");
        bgNotes.animate().alpha(isNotesActive ? 1f : 0f).setDuration(duration).start();
        bgTasks.animate().alpha(isNotesActive ? 0f : 1f).setDuration(duration).start();
    }

    private void loadConfig() {
        int theme = DatabaseHelper.getInstance(this).getTheme();
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

    private Integer getActiveFragment() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container_view);

        if (currentFragment instanceof NotesFragment) {
            return 1;
        } else if (currentFragment instanceof TasksFragment) {
            return 2;
        }
        return 0;
    }
}
