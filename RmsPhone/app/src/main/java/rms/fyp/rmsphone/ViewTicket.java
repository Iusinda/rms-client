package rms.fyp.rmsphone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class ViewTicket extends ActionBarActivity {

    public static final String PROPERTY_CUSTOMER_ID = "customer_id", PROPERTY_NOTICE_TIME = "notice_time";
    private int noticeTimePref, duration;
    private long getTime;
    private String customerId, restaurantId, wsForticket, wsForRestaurantInfo, wsForRemoveTicket, serverHost;
    private Spinner notificationTimePicker;
    private TextView restaurantNameField, partySize, queuePosition, waitingTimeField, ticketNumberField, status;
    private Button homeBtn, refreshBtn, cancelBtn;
    private ArrayList<String> notificationTimesList;

    private void initialization() {

        serverHost = this.getResources().getString(R.string.serverHost);
        customerId = getCustomerId(getApplicationContext());
        status = (TextView) findViewById(R.id.status);
        ticketNumberField = (TextView) findViewById(R.id.restaurantTicketNumber);
        homeBtn = (Button) findViewById(R.id.homeBtn2);
        refreshBtn = (Button) findViewById(R.id.refreshBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        restaurantNameField = (TextView) findViewById(R.id.restaurantNameFIeld3);
        partySize = (TextView) findViewById(R.id.partySizeLabel1);
        queuePosition = (TextView) findViewById(R.id.positionField);
        waitingTimeField = (TextView) findViewById(R.id.waitingTimeField2);
        notificationTimePicker = (Spinner) findViewById(R.id.notificationPicker);
        wsForticket = serverHost + "/rms/ticket?customerId=" + customerId;
        wsForRemoveTicket = serverHost + "/rms/ticket/remove?customerId=" + customerId;
        wsForRestaurantInfo = serverHost + "/rms/restaurant?id=";
        cancelBtn.setVisibility(View.INVISIBLE);
        status.setVisibility(View.GONE);
        ticketNumberField.setText("Ticket Number : N/A");
        waitingTimeField.setText("Waiting Time : N/A");
        restaurantNameField.setText("Restaurant Name : N/A");
        partySize.setText("Party size : N/A");
        queuePosition.setText("Position : N/A");

        addItemsToSpinner();

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ChooseRestaurant.class);
                startActivity(intent);
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialization();
                new GetTicketInfo().execute(wsForticket);
            }


        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RemoveTicket().execute(wsForRemoveTicket);
                ticketNumberField.setText("Ticket Number : N/A");
                waitingTimeField.setText("Waiting Time : N/A");
                restaurantNameField.setText("Restaurant Name : N/A");
                partySize.setText("Party size : N/A");
                queuePosition.setText("Position : N/A");
                status.setVisibility(View.GONE);

                Log.i(this.getClass().toString() + "-cancelBtnClicked : ", wsForRemoveTicket);
                waitingTimeField.setVisibility(View.GONE);
                queuePosition.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                cancelAlarm();
                Toast.makeText(getApplicationContext(), "Ticket has been cancelled.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addItemsToSpinner() {
        notificationTimesList = new ArrayList<String>();
        for (int i = 1; i < 5; i++) {
            notificationTimesList.add(5 * i + "");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewTicket.this, android.R.layout.simple_spinner_item, notificationTimesList);
        getNoticePrefs();

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notificationTimePicker.setAdapter(adapter);
        notificationTimePicker.setSelection(noticeTimePref);
        notificationTimePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                storeNoticePrefs(getApplicationContext(), position);
                noticeTimePref = position;
                new GetTicketInfo().execute(wsForticket);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
    }

    private SharedPreferences getSharedPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(ChooseRestaurant.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private String getCustomerId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(context);
        String customerId = prefs.getString(PROPERTY_CUSTOMER_ID, "");
        if (customerId.isEmpty()) {
            Log.i(this.getClass().toString(), "customer id not found.");
            return "";
        }
        return customerId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ticket);
        initialization();
        new GetTicketInfo().execute(wsForticket);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_ticket, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeNoticePrefs(Context context, int noticeTimePref) {
        final SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PROPERTY_NOTICE_TIME, noticeTimePref);
        editor.commit();
    }

    private void getNoticePrefs() {
        final SharedPreferences prefs = getSharedPreferences(getApplicationContext());
        noticeTimePref = prefs.getInt(PROPERTY_NOTICE_TIME, 0);
    }

    public void setAlarm() {
        cancelAlarm();
        int noticeTimeInMin = (noticeTimePref+1) * 5;
        int timeDiff =duration - noticeTimeInMin;

        if(timeDiff <= 0)
        {
            Toast.makeText(getApplicationContext(),"Your table will be ready in less than "+noticeTimeInMin+" mins",Toast.LENGTH_SHORT).show();
        }
        else
        {
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, LocalReceiver.class);
            intent.putExtra("Message","Your table will be ready in  " + noticeTimeInMin + " mins");
            Log.i("",noticeTimeInMin+"");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar current = Calendar.getInstance();
            current.setTimeInMillis(System.currentTimeMillis()+ timeDiff * 60 * 1000);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ timeDiff * 60 * 1000, pendingIntent);
            Toast.makeText(getApplicationContext(),"Notification time is updated to " + noticeTimeInMin + " mins",Toast.LENGTH_SHORT).show();
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(System.currentTimeMillis()+ timeDiff * 60 * 1000);
            Log.i("Alarm set at ",alarmTime.getTime()+"");
        }

    }

    public void cancelAlarm() {
        Log.i("clear", "alarm");
        Intent intent = new Intent(this, LocalReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(ViewTicket.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private class GetTicketInfo extends AsyncTask<String, Void, String> {
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
            Log.i(this.getClass().toString(), response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            if (Error != null)
                Log.e(this.getClass().toString(), Error);
            else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (!jsonObject.optString("size").isEmpty() || jsonObject != null) {
                        cancelBtn.setVisibility(View.VISIBLE);
                    }

                    String unit = " mins";
                    char ticketType = (char) ('A' - 1 + jsonObject.getInt("type"));
                    ticketNumberField.setText("Ticket Number : " + ticketType + jsonObject.optString("number"));
                    partySize.setText("Party Size : " + jsonObject.optString("size"));
                    queuePosition.setText("Position : " + jsonObject.optString("position"));
                    restaurantId = jsonObject.optString("restaurantId");
                    getTime = jsonObject.optLong("getTime");
                    duration = jsonObject.getInt("duration");
                    wsForRestaurantInfo = serverHost + "/rms/restaurant?id="+ restaurantId;
                    Timestamp current = new Timestamp(new Date().getTime());
                    if (duration == 1) {
                        unit = " min";
                        waitingTimeField.setText("Waiting Time : " + duration+ unit);
                    } else if (duration <= 0) {
                        waitingTimeField.setText("Waiting Time : Soon");
                    } else {
                        waitingTimeField.setText("Waiting Time : " + duration + unit);
                    }
                    Log.i(this.getClass().toString() + "RestaurantID ", restaurantId);
                    Log.i(this.getClass().toString() + "wsForRestaurantGetting", wsForRestaurantInfo);
                    if (!jsonObject.isNull("callTime")) {
                        waitingTimeField.setVisibility(View.GONE);
                        queuePosition.setVisibility(View.GONE);
                        status.setVisibility(View.VISIBLE);
                        cancelAlarm();
                    } else {
                        waitingTimeField.setVisibility(View.VISIBLE);
                        queuePosition.setVisibility(View.VISIBLE);
                        status.setVisibility(View.GONE);
                        getNoticePrefs();
                        setAlarm();
                    }
                    new GetRestaurantName().execute(wsForRestaurantInfo);
                } catch (JSONException e) {
                    Log.e(this.getClass().toString(), e.toString());
                }
            }
        }
    }

    private class GetRestaurantName extends AsyncTask<String, Void, String> {
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
            Log.i(this.getClass().toString(), response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            if (Error != null)
                Log.e(this.getClass().toString(), Error);
            else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    restaurantNameField.setText("Restaurant Name : " + jsonObject.optString("name"));
                } catch (JSONException e) {
                    Log.e(this.getClass().toString(), "json problems" + e.toString());
                }
            }
        }
    }

    private class RemoveTicket extends AsyncTask<String, Void, String> {
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
            Log.i(this.getClass().toString(), response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            if (Error != null)
                Log.e("error : ", Error);
            else {
                Log.i(this.getClass().toString(), "Tickets are removed");
            }
        }
    }
}
