package com.spritelab.bookmark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.model.TaskModel;
import com.spritelab.bookmark.utils.DatabaseHelper;
import com.spritelab.bookmark.utils.HelpUtils;

import java.util.Collections;
import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {
    private final List<TaskModel> items;
    private final LayoutInflater inflater;
    private ItemTouchHelper touchHelper;

    public TasksAdapter(Context context, List<TaskModel> items) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_tasks, parent, false);

        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setTranslationY(100f);

        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(viewType * 50)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskModel task = items.get(position);
        holder.tvName.setText(task.getTitle());
        holder.tvDate.setText(task.getDate());

        holder.rvTaskPoints.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        InnerPointsAdapter innerPointsAdapter = new InnerPointsAdapter(inflater.getContext(), task, null);
        holder.rvTaskPoints.setAdapter(innerPointsAdapter);

        HelpUtils.setupDropAnimation(holder.itemView, false,
                () -> {
                    MessageDialog.show("Подтверждение", "Удалить задачу из списка?",
                            "Да", "Нет").setOkButton((dialog, v) -> {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            TaskModel removed = items.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                            DatabaseHelper.getInstance(inflater.getContext()).deleteTask(removed.getId());
                        }
                        return false;
                    });
                },
                () -> {
                    if (touchHelper != null) {
                        touchHelper.startDrag(holder);
                    }
                }
        );
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDate;
        RecyclerView rvTaskPoints;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            rvTaskPoints = itemView.findViewById(R.id.rvTaskPoints);
        }
    }
}
