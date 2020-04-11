package aivie.developer.aivie.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = Constant.NOTIFICATION;
    public static String NOTIFICATION = Constant.NOTIFICATION_ID;

    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);

        assert notification != null;
        notification.flags = Notification.FLAG_NO_CLEAR;

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        if(Constant.DEBUG) Log.d(Constant.TAG, "NOTIFICATION_ID: " + id);
        notificationManager.notify(id, notification);
    }

}
