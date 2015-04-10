package rms.fyp.rmsphone;

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
import java.util.ArrayList;


public class ConfirmTicket extends ActionBarActivity {
    public static final String PROPERTY_CUSTOMER_ID = "customer_id";
    private Button ticketBtn, confirmBtn, homeBtn;
    private TextView restaurantName, ticketAhead, waitingTime;
    private String restaurantId, ticketType, wsForGetTicketType, wsForGetRestaurant, wsForCheckTicketExist, wsForCreateTicket, serverHost, customerId;
    private Spinner partySizePicker;
    private ArrayList<String> partySizeItems;
    private boolean ticketExist = false;
    private int lowerRange, upperRange;
    private Intent intent;

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
            Log.d(this.getClass().toString(), "customer id not found.");
            return "";
        }
        return customerId;
    }

    private void initialization() {
        serverHost = this.getResources().getString(R.string.serverHost);
        homeBtn = (Button) findViewById(R.id.homeBtn);
        ticketBtn = (Button) findViewById(R.id.ticketBtn3);
        confirmBtn = (Button) findViewById(R.id.getTicketBtn);
        restaurantName = (TextView) findViewById(R.id.restaurantNameField2);
        ticketAhead = (TextView) findViewById(R.id.ticketAheadField);
        waitingTime = (TextView) findViewById(R.id.waitingTimeField);
        partySizePicker = (Spinner) findViewById(R.id.partySizeSpinner);
        intent = getIntent();
        if (intent != null) {
            restaurantId = intent.getStringExtra("restaurantId");
            ticketType = intent.getStringExtra("ticketType");
            lowerRange = intent.getIntExtra("lowerRange", 0);
            upperRange = intent.getIntExtra("upperRange", 0);
            wsForGetRestaurant = serverHost + "/rms/restaurant?id=" + restaurantId;
            wsForGetTicketType = serverHost + "/rms/ticket?id=" + restaurantId + "&type=" + ticketType;
            wsForCheckTicketExist = serverHost + "/rms/ticket?customerId=";
            wsForCreateTicket = serverHost + "/rms/ticket/create";
        }
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), ChooseRestaurant.class);
                startActivity(intent);
            }
        });
        ticketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent();
                intent.setClass(getApplicationContext(), ViewTicket.class);
                startActivity(intent);
            }
        });
        addItemsToSpinner();
        customerId = getCustomerId(getApplicationContext());
        wsForCheckTicketExist += customerId;
        confirmBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("customerID", customerId);
                Log.i("restaurantname", restaurantName.getText().toString());
                Log.i("ticket Type ", ticketType);
                Log.i("ws :", wsForCreateTicket);
                wsForCreateTicket = serverHost + "/rms/ticket/create?customerId="
                        + customerId + "&id=" + restaurantId + "&type=" + ticketType + "&size=" + partySizePicker.getSelectedItem().toString();
                new CreateTicket().execute(wsForCreateTicket);
                intent = new Intent();
                intent.setClass(getApplicationContext(), ViewTicket.class);
                startActivity(intent);
            }
        });

        confirmBtn.setVisibility(View.GONE);

    }

    private void addItemsToSpinner() {
        partySizeItems = new ArrayList<String>();
        for (int i = lowerRange; i <= upperRange; i++) {
            partySizeItems.add(i + "");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ConfirmTicket.this, android.R.layout.simple_spinner_item, partySizeItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        partySizePicker.setAdapter(adapter);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_ticket);
        initialization();
        new GetRestaurantInfo().execute(wsForGetRestaurant);
        new GetQueueInfo().execute(wsForGetTicketType);
        new CheckTicketExist().execute(wsForCheckTicketExist);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm_ticket, menu);
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

    private class GetRestaurantInfo extends AsyncTask<String, Void, String> {

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
            Log.i("response", response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            if (Error != null)
                Log.e("error : ", Error);
            else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    restaurantName.setText(jsonObject.optString("name"));

                } catch (JSONException e) {
                    Log.i("json problems", e.toString());

                }
            }
        }

    }

    private class GetQueueInfo extends AsyncTask<String, Void, String> {

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
            Log.i("response", response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.

            if (Error != null)
                Log.e("error : ", Error);
            else {
                JSONObject jsonObj = null;
                try {
                    jsonObj = new JSONObject(result);
                    String unit = "mins";
                    if (jsonObj.getString("duration").equalsIgnoreCase("0") || jsonObj.getString("duration").equalsIgnoreCase("1"))
                        unit = "min";
                    waitingTime.setText(waitingTime.getText() + " " + jsonObj.optString("duration") + " min");
                    ticketAhead.setText(ticketAhead.getText() + " " + jsonObj.optString("position"));

                } catch (JSONException e) {
                    Log.e(this.getClass().toString(), e.toString());
                }

            }


        }

    }

    private class CheckTicketExist extends AsyncTask<String, Void, String> {
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
                    Log.e("Error:checkTicketExist", e.toString());
                }
            }
            Log.i("response:TIcketexist", response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            if (Error != null) {
                Log.e("Error", Error);
            } else {
                if (result.isEmpty() || result.trim() == "") {
                    ticketExist = false;
                } else
                    ticketExist = true;
                if (ticketExist) {
                    confirmBtn.setVisibility(View.GONE);
                } else {
                    confirmBtn.setVisibility(View.VISIBLE);
                }
            }

        }

    }

    private class CreateTicket extends AsyncTask<String, Void, String> {
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
            Log.i("response", response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            if (Error != null)
                Log.e("error : ", Error);
            else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    Log.i(this.getClass().toString(), "Ojbect have been inserted" + jsonObject.toString());
                } catch (JSONException e) {
                    Log.e("json problems", e.toString());

                }
            }
        }

    }
}
