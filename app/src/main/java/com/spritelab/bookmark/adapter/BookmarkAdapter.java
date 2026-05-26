package com.spritelab.bookmark.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.kongzue.dialogx.dialogs.MessageDialog;
import com.spritelab.bookmark.R;
import com.spritelab.bookmark.activity.MainActivity;
import com.spritelab.bookmark.model.BookmarkModel;
import com.spritelab.bookmark.utils.HelpUtils;

import java.util.Collections;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {
    private final List<BookmarkModel> items;
    private final LayoutInflater inflater;
    private ItemTouchHelper touchHelper;

    public BookmarkAdapter(Context context, List<BookmarkModel> items) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_bookmark, parent, false);

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
        BookmarkModel b = items.get(position);
        holder.title.setText(b.getTitle());
        holder.date.setText(b.getDate());
        HelpUtils.setupDropAnimation(holder.itemView, false,
                () -> {
                    MessageDialog.show("Подтверждение", "Удалить закладку из списка?",
                            "Да", "Нет").setOkButton((dialog, v) -> {
                        int currentPosition = holder.getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            items.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                            MainActivity.saveToJson(inflater.getContext(), items);
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
        TextView title;
        TextView date;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            date = itemView.findViewById(R.id.tvDate);
        }
    }
}