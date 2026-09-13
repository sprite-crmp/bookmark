package com.spritelab.bookmark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spritelab.bookmark.R;
import com.spritelab.bookmark.model.TaskModel;
import com.spritelab.bookmark.model.TaskPointModel;
import com.spritelab.bookmark.utils.DatabaseHelper;

import java.util.List;

public class InnerPointsAdapter extends RecyclerView.Adapter<InnerPointsAdapter.ViewHolder> {
    private final List<TaskPointModel> items;
    private final LayoutInflater inflater;
    private final TaskModel parentTask;
    private final Runnable onStatusChanged;

    public InnerPointsAdapter(Context context, TaskModel parentTask, Runnable onStatusChanged) {
        this.items = parentTask.getPoints();
        this.parentTask = parentTask;
        this.inflater = LayoutInflater.from(context);
        this.onStatusChanged = onStatusChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_point, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskPointModel point = items.get(position);
        holder.tvPointTitle.setText(point.getTitle());
        holder.imageView.setImageResource(point.isDone() ? R.drawable.img_yes : R.drawable.img_no);

        holder.imageView.setOnClickListener(v -> {
            point.setDone(!point.isDone());
            holder.imageView.setImageResource(point.isDone() ? R.drawable.img_yes : R.drawable.img_no);
            DatabaseHelper.getInstance(inflater.getContext()).updateTask(parentTask);
            if (onStatusChanged != null) {
                onStatusChanged.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvPointTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvPointTitle = itemView.findViewById(R.id.tvPointTitle);
        }
    }
}
