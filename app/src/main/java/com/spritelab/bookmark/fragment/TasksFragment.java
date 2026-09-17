package com.spritelab.bookmark.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kongzue.dialogx.dialogs.PopTip;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.adapter.DialogPointsAdapter;
import com.spritelab.bookmark.adapter.TasksAdapter;
import com.spritelab.bookmark.model.TaskModel;
import com.spritelab.bookmark.model.TaskPointModel;
import com.spritelab.bookmark.utils.DatabaseHelper;
import com.spritelab.bookmark.utils.HelpUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TasksFragment extends Fragment {

    private RecyclerView rvTasks;
    private ConstraintLayout dialogAddTask;
    private TextView tvNameTask;
    private RecyclerView rvDialogPoints;
    private EditText etBookMarkInner;
    private ImageView btnSendInner;
    private View btnCancel;
    private View globalTouchBlocker;

    private TasksAdapter adapter;
    private DialogPointsAdapter dialogPointsAdapter;
    private ItemTouchHelper itemTouchHelper;

    private final List<TaskModel> tasks = new ArrayList<>();
    private final List<TaskPointModel> currentNewPoints = new ArrayList<>();
    private String currentTaskTitle = "";
    private boolean cooldown = false;

    private OnBackPressedCallback backPressedCallback;

    public TasksFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTasks = view.findViewById(R.id.rvTasks);
        dialogAddTask = view.findViewById(R.id.dialog_add_task);
        tvNameTask = view.findViewById(R.id.tv_name_task);
        rvDialogPoints = view.findViewById(R.id.rvDialogPoints);
        etBookMarkInner = view.findViewById(R.id.etBookMark);
        btnSendInner = view.findViewById(R.id.btn_send);
        btnCancel = view.findViewById(R.id.btn_cancel);
        
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            globalTouchBlocker = getActivity().findViewById(R.id.global_blocker);
        }
        
        dialogAddTask.setVisibility(View.GONE);
        if (globalTouchBlocker != null) globalTouchBlocker.setVisibility(View.GONE);

        setupRecyclerView();
        setupDialogRecyclerView();
        loadFromDb();

        HelpUtils.setupDropAnimation(btnSendInner, false, () -> {
            String text = etBookMarkInner.getText().toString().trim();
            onSendPressed(text);
        }, () -> {});

        HelpUtils.setupDropAnimation(btnCancel, false, () -> {
            hideDialog();
        }, () -> {});

        setupBackPressed();
    }

    private void setupBackPressed() {
        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if (isDialogVisible()) {
                    hideDialog();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);
    }

    private void setupRecyclerView() {
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TasksAdapter(getContext(), tasks);
        rvTasks.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                adapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                DatabaseHelper.getInstance(getContext()).updateTasksOrder(tasks);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        };

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvTasks);
        adapter.setTouchHelper(itemTouchHelper);
    }

    private void setupDialogRecyclerView() {
        rvDialogPoints.setLayoutManager(new LinearLayoutManager(getContext()));
        dialogPointsAdapter = new DialogPointsAdapter(getContext(), currentNewPoints);
        rvDialogPoints.setAdapter(dialogPointsAdapter);

        ItemTouchHelper.SimpleCallback dialogCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                dialogPointsAdapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    currentNewPoints.remove(position);
                    dialogPointsAdapter.notifyItemRemoved(position);
                }
            }
        };
        new ItemTouchHelper(dialogCallback).attachToRecyclerView(rvDialogPoints);
    }

    private void loadFromDb() {
        if (getContext() == null) return;
        List<TaskModel> loaded = DatabaseHelper.getInstance(getContext()).getAllTasks();
        tasks.clear();
        tasks.addAll(loaded);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public boolean isDialogVisible() {
        return dialogAddTask != null && dialogAddTask.getVisibility() == View.VISIBLE;
    }

    public void onSendPressed(String text) {
        if (!isDialogVisible()) {
            if (text.isEmpty()) {
                showCooldownTip("Введите название задачи");
                return;
            }
            if (text.length() <= 3) {
                showCooldownTip("Название слишком короткое");
                return;
            }
            currentTaskTitle = text;
            currentNewPoints.clear();
            dialogPointsAdapter.notifyDataSetChanged();
            tvNameTask.setText(currentTaskTitle);
            etBookMarkInner.setText("");
            showDialog();
        } else {
            if (text.isEmpty()) {
                if (currentNewPoints.isEmpty()) {
                    showCooldownTip("Добавьте хотя бы один пункт");
                    return;
                }
                saveCurrentTaskAndCloseDialog();
            } else {
                currentNewPoints.add(new TaskPointModel(text, false));
                dialogPointsAdapter.notifyItemInserted(currentNewPoints.size() - 1);
                etBookMarkInner.setText("");
                rvDialogPoints.smoothScrollToPosition(currentNewPoints.size() - 1);
            }
        }
    }

    private void showCooldownTip(String message) {
        if (cooldown) return;
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
        PopTip.show(message).iconWarning();
    }

    private void showDialog() {
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }

        if (globalTouchBlocker != null) {
            globalTouchBlocker.setVisibility(View.VISIBLE);
        }

        dialogAddTask.setScaleX(0.8f);
        dialogAddTask.setScaleY(0.8f);
        dialogAddTask.setAlpha(0f);
        dialogAddTask.setVisibility(View.VISIBLE);
        dialogAddTask.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
        
        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(true);
        }
    }

    private void hideDialog() {
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        if (globalTouchBlocker != null) {
            globalTouchBlocker.setVisibility(View.GONE);
        }

        dialogAddTask.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0f)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    dialogAddTask.setVisibility(View.GONE);
                    etBookMarkInner.setText("");
                    currentTaskTitle = "";
                    currentNewPoints.clear();
                    dialogPointsAdapter.notifyDataSetChanged();
                })
                .start();

        if (backPressedCallback != null) {
            backPressedCallback.setEnabled(false);
        }
    }

    private void saveCurrentTaskAndCloseDialog() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String dateTime = sdf.format(now);

        TaskModel newTask = new TaskModel(currentTaskTitle, "Дата создания: " + dateTime, new ArrayList<>(currentNewPoints));
        DatabaseHelper.getInstance(getContext()).addTask(newTask);

        hideDialog();
        loadFromDb();
    }
}
