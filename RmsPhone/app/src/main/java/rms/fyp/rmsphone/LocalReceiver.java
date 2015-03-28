package rms.fyp.rmsphone;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by lohris on 20/3/15.
 */
public class LocalReceiver extends BroadcastReceiver{

    private Context context;
    private Intent intent;
    private String msg;
    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;
    @Override
    public void onReceive(Context context,Intent intent)
    {
        this.context = context;
        this.intent = intent;
        Log.i(this.getClass().toString()," alarm Triggered");
        msg = intent.getStringExtra("message");
        String wsForGettingCallTime = intent.getStringExtra("webService");
        new GetTicketInfo().execute(wsForGettingCallTime);


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
    private class GetTicketInfo  extends AsyncTask<String, Void, String> {
        // Required initialization
        private String Error = null;
        // Call after onPreExecute method
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.wtf("response", response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            if (Error != null)
                Log.wtf("error : ", Error);
            else
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String callTime = jsonObject.optString("callTime");
                    if(callTime.isEmpty() ||callTime== null)
                    {
                        sendNotification(context,msg);
                    }

                } catch (JSONException e) {
                    Log.wtf("json problems",e.toString());
                }
            }
        }

    }
}
