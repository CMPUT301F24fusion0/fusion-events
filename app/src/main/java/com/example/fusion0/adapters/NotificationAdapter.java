package com.example.fusion0.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fusion0.helpers.AppNotifications;
import com.example.fusion0.helpers.NotificationHelper;
import com.example.fusion0.helpers.Waitlist;
import com.example.fusion0.models.NotificationItem;
import com.example.fusion0.R;

import java.util.List;

/**
 * @author Nimi Akinroye
 *
 * Adapter for displaying notifications in a RecyclerView.
 * It supports two types of notifications: standard and lottery (accept/decline).
 */
public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_STANDARD = 0;
    private static final int TYPE_LOTTERY = 1;

    private Context context;
    private List<NotificationItem> notificationList;
    private Waitlist waitlist;
    private String userId;

    /**
     * Constructor for the NotificationAdapter.
     *
     * @param context       The context of the activity where the adapter is used.
     * @param notifications The list of NotificationItem objects to display.
     */
    public NotificationAdapter(@NonNull Context context, @NonNull List<NotificationItem> notifications, String userId) {
        this.context = context;
        this.notificationList = notifications;
        this.waitlist = new Waitlist();
        this.userId = userId;
    }

    /**
     * Determines the type of notification item based on the flag in NotificationItem.
     *
     * @param position The position of the item in the list.
     * @return The view type (TYPE_STANDARD or TYPE_LOTTERY).
     */
    @Override
    public int getItemViewType(int position) {
        NotificationItem item = notificationList.get(position);
        return item.getFlag().equals("1") ? TYPE_LOTTERY : TYPE_STANDARD;
    }

    /**
     * Creates the appropriate ViewHolder based on the view type.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The type of view (TYPE_STANDARD or TYPE_LOTTERY).
     * @return The ViewHolder for the corresponding view type.
     */
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

    /**
     * Binds data to the ViewHolder based on the item type.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        if (holder instanceof LotteryNotificationViewHolder) {
            LotteryNotificationViewHolder lotteryHolder = (LotteryNotificationViewHolder) holder;
            lotteryHolder.notificationTitle.setText(item.getTitle());
            lotteryHolder.notificationBody.setText(item.getBody());

            lotteryHolder.acceptButton.setOnClickListener(v -> {
                Toast.makeText(context, "Event Accepted", Toast.LENGTH_SHORT).show();
                waitlist.changeStatus(item.getEventId(), userId, "accept");
            });

            lotteryHolder.declineButton.setOnClickListener(v -> {
                waitlist.changeStatus(item.getEventId(), userId, "cancel");
                AppNotifications.sendNotification(userId, "Lottery Results",
                        "Unfortunately, you have not been chosen for the lottery. " +
                                "If someone declines the event, you may be selected for the lottery.",
                        "0", item.getEventId());

                NotificationHelper.deleteNotification(userId, item, new NotificationHelper.Callback() {
                    @Override
                    public void onNotificationsUpdated(List<NotificationItem> updatedNotificationList) {}

                    @Override
                    public void onError(String error) {
                        Log.e("Error with deleting notification item", error);
                    }
                });
            });

        } else if (holder instanceof StandardNotificationViewHolder) {
            StandardNotificationViewHolder standardHolder = (StandardNotificationViewHolder) holder;
            standardHolder.notificationTitle.setText(item.getTitle());
            standardHolder.notificationBody.setText(item.getBody());
        }
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The size of the notification list.
     */
    @Override
    public int getItemCount() {
        return (notificationList != null) ? notificationList.size() : 0;
    }

    /**
     * ViewHolder for standard notifications.
     */
    public static class StandardNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTitle;
        TextView notificationBody;

        /**
         * Constructor for the StandardNotificationViewHolder.
         *
         * @param itemView The view associated with the ViewHolder.
         */
        public StandardNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationBody = itemView.findViewById(R.id.notificationBody);
        }
    }

    /**
     * ViewHolder for accept/decline notifications (lottery notifications).
     */
    public static class LotteryNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTitle;
        TextView notificationBody;
        ImageView acceptButton;
        ImageView declineButton;

        /**
         * Constructor for the LotteryNotificationViewHolder.
         *
         * @param itemView The view associated with the ViewHolder.
         */
        public LotteryNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationBody = itemView.findViewById(R.id.notificationBody);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}