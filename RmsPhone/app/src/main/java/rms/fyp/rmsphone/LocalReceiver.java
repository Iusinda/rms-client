package rms.fyp.rmsphone;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

/**
 * Created by lohris on 20/3/15.
 */
public class LocalReceiver extends BroadcastReceiver{

    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;
    @Override
    public void onReceive(Context context,Intent intent)
    {
        Log.i(this.getClass().toString()," alarm Triggered");
        String msg = intent.getStringExtra("message");
        sendNotification(context,msg);
    }

    private void sendNotification(Context context,String msg) {
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, ViewTicket.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

    }

}
