package rms.fyp.rmsphone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Date;


public class ViewTicket extends ActionBarActivity {

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private Intent intent;
    private String noticeTimePref,customerId,restaurantId,wsForticket,wsForRestaurantInfo,wsForRemoveTicket,serverHost;
    private Spinner notificationTimePicker;
    private TextView restaurantNameField,partySize,queuePosition,waitingTimeField;
    private Button homeBtn,refreshBtn,cancelBtn;
    private Timestamp targetTime;
    public static final String PROPERTY_CUSTOMER_ID = "customer_id",PROPERTY_NOTICE_TIME = "notice_time";
    private Context context = this;
    private ArrayList<String> notificationTimesList;

    private void initialization()
    {

        serverHost = this.getResources().getString(R.string.serverHost);
        customerId  = getCustomerId(context);
        homeBtn = (Button) findViewById(R.id.homeBtn2);
        refreshBtn = (Button) findViewById(R.id.refreshBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        restaurantNameField = (TextView) findViewById(R.id.restaurantNameFIeld3);
        partySize = (TextView) findViewById(R.id.partySizeLabel1);
        queuePosition = (TextView) findViewById(R.id.positionField);
        waitingTimeField = (TextView) findViewById(R.id.waitingTimeField2);
        notificationTimePicker = (Spinner) findViewById(R.id.notificationPicker);
        wsForticket = serverHost + "/rms/ticket?customerId="+customerId;
        wsForRemoveTicket = serverHost +"/rms/ticket/remove?customerId="+customerId;
        wsForRestaurantInfo =serverHost+ "/rms/restaurant?id=";
        cancelBtn.setVisibility(View.INVISIBLE);
        waitingTimeField.setText("Waiting Time : N/A");
        restaurantNameField.setText("Restaurant Name : N/A");
        partySize.setText("Party size : N/A");
        queuePosition.setText("Position : N/A");

        addItemsToSpinner();

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent();
                intent.setClass(context,ChooseRestaurant.class);
                startActivity(intent);
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new updatePosition().execute(wsForticket);
                if (targetTime != null)
                {
                    Timestamp current = new Timestamp(new Date().getTime());
                    waitingTimeField.setText( "Waiting Time : " + (targetTime.getTime() - current.getTime())/60000);
                }
            }

        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RemoveTicket().execute(wsForRemoveTicket);
                waitingTimeField.setText("Waiting Time : N/A");
                restaurantNameField.setText("Restaurant Name : N/A");
                partySize.setText("Party size : N/A");
                queuePosition.setText("Position : N/A");
                targetTime =null;
                Log.wtf(this.getClass().toString() + "-cancelBtnClicked : ", wsForRemoveTicket);
                cancelBtn.setVisibility(View.GONE);
            }
        });
    }

    private void addItemsToSpinner() {
        notificationTimesList = new ArrayList<String>();
        notificationTimesList.add("5");
        notificationTimesList.add("10");
        notificationTimesList.add("15");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, notificationTimesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        notificationTimePicker.setAdapter(adapter);
        notificationTimePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Intent alarmIntent = new Intent(context, LocalReceiver.class);
                String selectedVal = (String)parent.getSelectedItem();
                noticeTimePref = selectedVal;
                storeNoticePrefs(context,noticeTimePref);
                if(targetTime != null)
                {
                Log.wtf("Target Time is not null","");

                Timestamp current = new Timestamp(new Date().getTime());
                long timeBeforeNotice = Long.valueOf(selectedVal)*60000;
                long timeDiff = targetTime.getTime() - current.getTime();
                long delay = 0;

                if(timeDiff > 5 * 60000)  {
                    if (timeDiff - timeBeforeNotice <= 0) {
                        delay = 5 * 60000;
                        alarmIntent.putExtra("message","The seat will be ready in 5 mins.");
                    } else {
                        alarmIntent.putExtra("message","The seat will be ready in "+timeBeforeNotice/60000 +" mins.");
                        delay = (timeDiff - timeBeforeNotice);
                    }
                }
                else
                {
                    delay = timeDiff;
                    alarmIntent.putExtra("message","The seat is ready NOW.");
                }

                pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
                alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+delay,pendingIntent);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getNoticePrefs();
                if (noticeTimePref == null || noticeTimePref.isEmpty()) {
                    noticeTimePref = "5";
                    notificationTimePicker.setSelection(0);
                    storeNoticePrefs(context,noticeTimePref);
                } else if (noticeTimePref.contentEquals("10")) {
                    notificationTimePicker.setSelection(1);
                } else {
                    notificationTimePicker.setSelection(2);
                }
                Intent alarmIntent = new Intent(context, LocalReceiver.class);

                if (targetTime != null) {
                    Timestamp current = new Timestamp(new Date().getTime());
                    long timeBeforeNotice = Long.valueOf(noticeTimePref) * 60000;
                    long timeDiff = targetTime.getTime() - current.getTime();
                    long delay = 0;

                    if (timeDiff > 5 * 60000) {
                        if (timeDiff - timeBeforeNotice <= 0) {
                            delay = 5 * 60000;
                            alarmIntent.putExtra("message", "The seat will be ready in 5 mins.");
                        } else {
                            alarmIntent.putExtra("message", "The seat will be ready in " + timeBeforeNotice / 60000 + " mins.");
                            delay = (timeDiff - timeBeforeNotice);
                        }
                    } else {
                        delay = timeDiff;
                        alarmIntent.putExtra("message", "The seat is ready NOW.");
                    }

                    pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
                }
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
            Log.wtf(this.getClass().toString(), "customer id not found.");
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

    private void storeNoticePrefs(Context context, String noticeTimePref) {
        final SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_NOTICE_TIME, noticeTimePref);
        editor.commit();
    }

    private void getNoticePrefs()
    {
        final SharedPreferences prefs = getSharedPreferences(context);
        noticeTimePref = prefs.getString(PROPERTY_NOTICE_TIME, "");
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
                    if(!jsonObject.optString("size").isEmpty() || jsonObject !=null) {
                        cancelBtn.setVisibility(View.VISIBLE);
                    }
                    partySize.setText("Party Size : " + jsonObject.optString("size"));
                    queuePosition.setText("Position : " + jsonObject.optString("position"));
                    restaurantId = jsonObject.optString("restaurantId");
                    targetTime = new Timestamp(jsonObject.optLong("getTime")+jsonObject.getInt("duration")*60000);
                    wsForRestaurantInfo += restaurantId;
                    Timestamp current = new Timestamp(new Date().getTime());
                    waitingTimeField.setText( "Waiting Time : " + (targetTime.getTime() - current.getTime())/60000);
                    Log.i(this.getClass().toString()+"RestaurantID ",restaurantId);
                    Log.i(this.getClass().toString()+"wsForRestaurantGetting",wsForRestaurantInfo);
                    new GetRestaurantName().execute(wsForRestaurantInfo);
                } catch (JSONException e) {
                    Log.wtf("json problems",e.toString());
                }
            }
        }

    }

    private class GetRestaurantName  extends AsyncTask<String, Void, String> {
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
                Log.wtf("error : ", Error);
            else
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    restaurantNameField.setText("Restaurant Name : " + jsonObject.optString("name"));
                } catch (JSONException e) {
                    Log.wtf("json problems",e.toString());

                }
            }
        }

    }

    private class RemoveTicket  extends AsyncTask<String, Void, String> {
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
                Log.wtf("error : ", Error);
            else
            {
                Log.i(this.getClass().toString(),"Tickets are removed");
            }
        }

    }

    private class updatePosition  extends AsyncTask<String, Void, String> {
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
                Log.wtf("error : ", Error);
            else
            {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if(!jsonObject.optString("position").isEmpty())
                        queuePosition.setText("Position : "+jsonObject.optString("position"));
                } catch (JSONException e) {
                    Log.wtf("json problems",e.toString());
                }
            }
        }

    }
}
