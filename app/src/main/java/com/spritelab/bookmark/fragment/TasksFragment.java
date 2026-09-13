package com.spritelab.bookmark.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private static final String TAG = "class:TasksFragment";

    private RecyclerView rvTasks;
    private ConstraintLayout dialogAddTask;
    private TextView tvNameTask;
    private RecyclerView rvDialogPoints;
    private EditText etBookMarkInner;
    private ImageView btnSendInner;

    private TasksAdapter adapter;
    private DialogPointsAdapter dialogPointsAdapter;
    private ItemTouchHelper itemTouchHelper;

    private final List<TaskModel> tasks = new ArrayList<>();
    private final List<TaskPointModel> currentNewPoints = new ArrayList<>();
    private String currentTaskTitle = "";

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

        setupRecyclerView();
        setupDialogRecyclerView();
        loadFromDb();

        HelpUtils.setupDropAnimation(btnSendInner, false, () -> {
            String text = etBookMarkInner.getText().toString().trim();
            onSendPressed(text);
        }, () -> {});
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
            if (text.isEmpty()) return;
            currentTaskTitle = text;
            currentNewPoints.clear();
            dialogPointsAdapter.notifyDataSetChanged();
            tvNameTask.setText(currentTaskTitle);
            dialogAddTask.setVisibility(View.VISIBLE);
        } else {
            if (text.isEmpty()) {
                saveCurrentTaskAndCloseDialog();
            } else {
                currentNewPoints.add(new TaskPointModel(text, false));
                dialogPointsAdapter.notifyItemInserted(currentNewPoints.size() - 1);
                etBookMarkInner.setText("");
            }
        }
    }

    private void saveCurrentTaskAndCloseDialog() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        String dateTime = sdf.format(now);

        TaskModel newTask = new TaskModel(currentTaskTitle, "Дата создания: " + dateTime, new ArrayList<>(currentNewPoints));
        DatabaseHelper.getInstance(getContext()).addTask(newTask);

        dialogAddTask.setVisibility(View.GONE);
        etBookMarkInner.setText("");
        currentTaskTitle = "";
        currentNewPoints.clear();

        loadFromDb();
    }
}
