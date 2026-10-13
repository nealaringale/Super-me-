package com.nealaringale.superme;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/** Shows opt-in local reminders scheduled by Super Me. */
public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "super_me_reminders";

    @Override public void onReceive(Context context, Intent intent) {
        String title = intent == null ? "Super Me reminder" : intent.getStringExtra("title");
        String body = intent == null ? "Check your saved plan." : intent.getStringExtra("body");
        if (title == null || title.trim().isEmpty()) title = "Super Me reminder";
        if (body == null || body.trim().isEmpty()) body = "Check your saved plan.";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Super Me reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Optional local reminders for timers and eating-window plans.");
            manager.createNotificationChannel(channel);
        }
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 902, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        manager.notify(title.hashCode(), notification.build());
    }
}
