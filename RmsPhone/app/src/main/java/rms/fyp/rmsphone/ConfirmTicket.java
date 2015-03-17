package rms.fyp.rmsphone;

import android.content.Context;
  import android.content.Intent;
  import android.os.AsyncTask;
  import android.support.v7.app.ActionBarActivity;
  import android.os.Bundle;
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
import java.lang.reflect.Array;
import java.util.ArrayList;


public class ConfirmTicket extends ActionBarActivity {
    private Button ticketBtn, confirmBtn, homeBtn;
    private TextView restaurantName, ticketAhead, waitingTime;
    private String restaurantId, ticketType, wsForGetTicketType, wsForGetRestaurant, serverHost;
    private Spinner partySizePicker;
    private ArrayList<String> partySizeItems;
    private int lowerRange, upperRange;
    private Intent intent;
    private Context context = this;

    private void initialization() {
        serverHost = this.getResources().getString(R.string.serverHost);
        homeBtn = (Button) findViewById(R.id.homeBtn);
        ticketBtn = (Button) findViewById(R.id.ticketBtn);
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
        }
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(context, ChooseRestaurant.class);
                startActivity(intent);
            }
        });
        addItemsToSpinner();
    }

    private void addItemsToSpinner() {
        partySizeItems = new ArrayList<String>();
        for(int i = lowerRange;i <= upperRange;i++)
        {
            partySizeItems.add(i+"");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, partySizeItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        partySizePicker.setAdapter(adapter);

    }



       @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_confirm_ticket);
           initialization();
           new getRestaurantInfo().execute(wsForGetRestaurant);
           new getQueueInfo().execute(wsForGetTicketType);
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

      private class getRestaurantInfo  extends AsyncTask<String, Void, String> {

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
                             restaurantName.setText(jsonObject.optString("name"));

                         } catch (JSONException e) {
                             Log.wtf("json problems",e.toString());

                         }
                     }
                 }

             }

     private class getQueueInfo  extends AsyncTask<String, Void, String> {

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
                       JSONObject jsonObj = null;
                       try {
                           jsonObj = new JSONObject(result);
                           waitingTime.setText(waitingTime.getText()+" "+jsonObj.optString("duration"));
                           ticketAhead.setText(ticketAhead.getText()+" "+jsonObj.optString("position"));

                       } catch (JSONException e) {
                           Log.e(this.getClass().toString(), e.toString());
                       }

                   }


               }

           }
   }
