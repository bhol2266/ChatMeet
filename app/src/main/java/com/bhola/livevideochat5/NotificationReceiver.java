package com.bhola.livevideochat5;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {


        String userName = intent.getStringExtra("USERNAME");
        this.context = context;
        showNotification(context, userName);
    }

    private void showNotification(Context context, String userName) {

        Intent notificationIntent = new Intent(context, MainActivity.class); // Replace TargetActivity with your desired activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);



        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "notify_001");
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("\uD83D\uDC8B Your 20% offer is expiring soon!!⏰⏰");
        mBuilder.setContentText("Hi! " + userName + " \uD83D\uDC59 your 20% offer will expiring in 30 minutes \uD83D\uDC8B \nBuy Desi Girls Premium at 79 only now and talk to unlimited girls");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }
}
