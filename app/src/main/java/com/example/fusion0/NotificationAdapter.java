package com.example.fusion0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_STANDARD = 0;
    private static final int TYPE_LOTTERY = 1;

    private Context context;
    private List<NotificationItem> notificationList;

    public NotificationAdapter(@NonNull Context context, @NonNull List<NotificationItem> notifications) {
        this.context = context;
        this.notificationList = notifications;
    }

    @Override
    public int getItemViewType(int position) {
        // Determine the type based on the flag in NotificationItem
        NotificationItem item = notificationList.get(position);
        return item.getFlag() == "1" ? TYPE_LOTTERY: TYPE_STANDARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_LOTTERY) {
            View view = LayoutInflater.from(context).inflate(R.layout.notification_two, parent, false);
            return new LotteryNotificationViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.notification_one, parent, false);
            return new StandardNotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        if (holder instanceof LotteryNotificationViewHolder) {
            LotteryNotificationViewHolder lotteryNotificationViewHolder = (LotteryNotificationViewHolder) holder;
            lotteryNotificationViewHolder.notificationTitle.setText(item.getTitle());
            lotteryNotificationViewHolder.notificationBody.setText(item.getBody());

            lotteryNotificationViewHolder.acceptButton.setOnClickListener(v -> {
                Toast.makeText(context, "Event Accepted", Toast.LENGTH_SHORT).show();
                // Handle accept action
            });

            lotteryNotificationViewHolder.declineButton.setOnClickListener(v -> {
                Toast.makeText(context, "Event Declined", Toast.LENGTH_SHORT).show();
                // Handle decline action
            });

        } else if (holder instanceof StandardNotificationViewHolder) {
            StandardNotificationViewHolder standardHolder = (StandardNotificationViewHolder) holder;
            standardHolder.notificationTitle.setText(item.getTitle());
            standardHolder.notificationBody.setText(item.getBody());
        }
    }

    @Override
    public int getItemCount() {
        return (notificationList != null) ? notificationList.size() : 0;
    }

    // ViewHolder for standard notifications
    public static class StandardNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTitle;
        TextView notificationBody;

        public StandardNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationBody = itemView.findViewById(R.id.notificationBody);
        }
    }

    // ViewHolder for accept/decline notifications
    public static class LotteryNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTitle;
        TextView notificationBody;
        ImageView acceptButton;
        ImageView declineButton;

        public LotteryNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationBody = itemView.findViewById(R.id.notificationBody);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}