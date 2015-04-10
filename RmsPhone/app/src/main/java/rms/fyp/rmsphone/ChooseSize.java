package rms.fyp.rmsphone;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class ChooseSize extends ActionBarActivity {
    private String restaurantId, wsForRestaurant, serverHost, wsForTicketType;
    private Intent intent;
    private JSONArray jsonArr;
    private TextView rName, rAddress, rPhoneNo, rOpening, rDesc;
    private Button homeBtn, ticketBtn;
    private ListView sizePicker;
    private ArrayList<HashMap<String, String>> ticketTypeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_size);
        initialization();
        new getRestaurantInfo().execute(wsForRestaurant);
        new getTicketType().execute(wsForTicketType);
        sizePicker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int lowerRange = 0, upperRange = 0;
                intent = new Intent(getApplicationContext(), ConfirmTicket.class);
                intent.putExtra("ticketType", (position + 1) + "");

                try {
                    upperRange = ((JSONObject) jsonArr.get(position)).optInt("maxSize");
                    if (position == 0) {
                        lowerRange = 1;
                    } else {
                        lowerRange = ((JSONObject) jsonArr.get(position - 1)).optInt("maxSize") + 1;
                    }
                } catch (JSONException e) {
                    Log.e(this.getClass().toString(), e.toString());
                }
                intent.putExtra("lowerRange", lowerRange);
                intent.putExtra("upperRange", upperRange);
                intent.putExtra("restaurantId", (restaurantId));
                startActivity(intent);
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getApplicationContext(), ChooseRestaurant.class);
                startActivity(intent);
            }
        });
    }

    private void initialization() {
        intent = getIntent();
        restaurantId = intent.getStringExtra("restaurantId");
        homeBtn = (Button) findViewById(R.id.homeBtn1);
        ticketBtn = (Button) findViewById(R.id.ticketBtn2);
        rName = (TextView) findViewById(R.id.restaurantNameField);
        rAddress = (TextView) findViewById(R.id.restaurantAddressField);
        rPhoneNo = (TextView) findViewById(R.id.restaurantNumberField);
        rOpening = (TextView) findViewById(R.id.restaurantOpeningField);
        rDesc = (TextView) findViewById(R.id.restaurantDescriptionField);
        sizePicker = (ListView) findViewById(R.id.ticketTypeList);
        serverHost = this.getResources().getString(R.string.serverHost);
        wsForRestaurant = serverHost + "/rms/restaurant?id=" + restaurantId;
        wsForTicketType = serverHost + "/rms/tickettypes?id=" + restaurantId;
        ticketBtn.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             intent = new Intent();
                                             intent.setClass(getApplicationContext(), ViewTicket.class);
                                             startActivity(intent);
                                         }
                                     }
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_size, menu);
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

    private class getRestaurantInfo extends AsyncTask<String, Void, String> {

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
                    Log.i("JSON = ", jsonObject.toString());
                    rName.setText(jsonObject.optString("name"));
                    rOpening.setText(jsonObject.optString("openingHours"));
                    rDesc.setText(jsonObject.optString("description"));
                    rPhoneNo.setText(jsonObject.optString("phoneNo"));
                    rAddress.setText(jsonObject.optString("address"));
                } catch (JSONException e) {
                    Log.e("json problems", e.toString());

                }
            }
        }

    }

    private class getTicketType extends AsyncTask<String, Void, String> {

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
            ticketTypeList = new ArrayList<HashMap<String, String>>();

            if (Error != null)
                Log.e("error : ", Error);
            else {
                try {
                    JSONArray jsonArray = jsonArr = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject tmpObject = (JSONObject) jsonArray.get(i);
                        HashMap<String, String> ticketTypeMap = new HashMap<String, String>();
                        String typeChar = "Ticket " + (char) (tmpObject.optInt("type") + 64);
                        String displaySize = "Size : ";
                        if (i == 0) {
                            displaySize += "1 - " + tmpObject.optString("maxSize");
                        } else {
                            JSONObject preObject = (JSONObject) jsonArray.get(i - 1);
                            displaySize += (preObject.optInt("maxSize") + 1) + " - " + tmpObject.optString("maxSize");
                        }

                        ticketTypeMap.put("restaurantId", (tmpObject.optInt("restaurantId") + ""));
                        ticketTypeMap.put("type", typeChar);
                        ticketTypeMap.put("maxSize", displaySize);
                        ticketTypeList.add(ticketTypeMap);

                    }
                } catch (JSONException e) {
                    Log.e("json problems", e.toString());

                }
            }
            ListAdapter adapter = new SimpleAdapter(ChooseSize.this, ticketTypeList,
                    R.layout.list_partysize,
                    new String[]{"type", "maxSize"},
                    new int[]{R.id.type, R.id.maxSize});
            sizePicker.setAdapter(adapter);

        }

    }
}
