package rms.fyp.rmsphone;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class ChooseRestaurant extends ActionBarActivity {

        ArrayList<String> areaItems,districtItems,areaIds,districtIds;
        ArrayList<HashMap<String,String>> restaurantItems;
        Button ticketBtn,submitBtn;
        Spinner areaDropdown,districtDropdown,districtDropDownId,areaDropdownId;
        ListView restaurantList;
        EditText searchField;
        String hostName;
        Context context = this;
        String areaId,districtId;
        //declare the string for web service
        String listRestaurants ,
                listDistricts,
                listAreas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_restaurant);
        initialization();

        submitBtn.setOnClickListener(btnOnCLickHandler);
        new getAreas().execute(listAreas);
        areaDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                areaDropdownId.setSelection(areaDropdown.getSelectedItemPosition());
                areaId = areaDropdownId.getSelectedItem()+"";
                final String listDistrictWithParams  = listDistricts +"?areaId=" + areaId;
                new getDistricts().execute(listDistrictWithParams);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        districtDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                districtDropDownId.setSelection(districtDropdown.getSelectedItemPosition());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        restaurantList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.wtf("Item from rest List :", ((HashMap) parent.getAdapter().getItem(position)).get("id").toString());
                Intent intent = new Intent(context, ChooseSize.class);
                intent.putExtra("areaId",areaId);
                intent.putExtra("districtId",districtId);
                intent.putExtra("restaurantId",((HashMap)parent.getAdapter().getItem(position)).get("id").toString());
                intent.setClass(context, ChooseSize.class);
                startActivity(intent);
            }
        });
        searchField.clearFocus();
    }

    private Button.OnClickListener btnOnCLickHandler = new Button.OnClickListener()
    {
        public void onClick(View v)
        {
            districtId = districtDropDownId.getSelectedItem()+"";
            String areaIdParams = "?areaId="+areaId;
            String districtIdParams = "&districtId="+districtId;
            String nameParams = null;

            try {
                nameParams = "&name=" + URLEncoder.encode(searchField.getText().toString(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String listRestaurantsWithParams = listRestaurants+areaIdParams+districtIdParams+nameParams;
            new getRestaurants().execute(listRestaurantsWithParams);

        }

    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_restaurant, menu);
        return true;
    }



    private class getAreas  extends AsyncTask<String, Void, String> {

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
            Log.wtf("response",response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            if (Error != null)
                Log.wtf("error : ", Error);
            else
            {
                try {
                    areaItems = new ArrayList<String>();
                    areaIds = new ArrayList<String>();
                    areaItems.add("all");
                    areaIds.add("0");
                    JSONArray jsonArray = new JSONArray(result);
                    Log.wtf("Json Array",jsonArray.toString());
                    Log.wtf("Array Length :", jsonArray.length() + "");
                    for(int i = 0;i < jsonArray.length();i++)
                    {
                        areaItems.add(((JSONObject) jsonArray.get(i)).optString("name"));
                        areaIds.add(((JSONObject) jsonArray.get(i)).optInt("id")+"");
                    }



                } catch (JSONException e) {
                  Log.wtf("json problems",e.toString());

                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, areaItems);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            areaDropdown.setAdapter(adapter);
            ArrayAdapter<String> idAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, areaIds);
            idAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            areaDropdownId.setAdapter(idAdapter);
        }

    }

    private class getDistricts  extends AsyncTask<String, Void, String> {

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
            Log.wtf("response",response);
            return response;
        }

        protected void onPostExecute(String result) {

            // NOTE: You can call UI Element here.
            if (Error != null)
                Log.wtf("error : ", Error);
            else
            {
                try {
                    districtItems = new ArrayList<String>();
                    districtItems.add("all");
                    districtIds = new ArrayList<String>();
                    districtIds.add("0");
                    JSONArray jsonArray = new JSONArray(result);
                    Log.wtf("Json Array",jsonArray.toString());
                    Log.wtf("Array Length :", jsonArray.length() + "");
                    for(int i = 0;i < jsonArray.length();i++)
                    {
                        districtItems.add(((JSONObject) jsonArray.get(i)).optString("name"));
                        districtIds.add(((JSONObject) jsonArray.get(i)).optInt("id") + "");
                    }



                } catch (JSONException e) {
                    Log.wtf("json problems",e.toString());

                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, districtItems);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            districtDropdown.setAdapter(adapter);
            ArrayAdapter<String> idAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, districtIds);
            idAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            districtDropDownId.setAdapter(idAdapter);

        }

    }

    private class getRestaurants  extends AsyncTask<String, Void, String> {

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
            Log.wtf("response",response);
            return response;
        }

        protected void onPostExecute(String result) {
            // NOTE: You can call UI Element here.
            restaurantItems = new ArrayList<HashMap<String, String>>();
            if (Error != null)
                Log.wtf("error : ", Error);
            else
            {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    Log.wtf("Json Array",jsonArray.toString());
                    for(int i = 0;i < jsonArray.length();i++)
                    {
                        HashMap<String,String> restaurantMap = new HashMap<String,String>();
                        restaurantMap.put("id",((JSONObject)jsonArray.get(i)).optInt("id")+"");
                        restaurantMap.put("name",((JSONObject)jsonArray.get(i)).optString("name"));
                        restaurantMap.put("address",((JSONObject)jsonArray.get(i)).optString("address"));
                        restaurantItems.add(restaurantMap);
                    }

                } catch (JSONException e) {
                    Log.wtf("json problems",e.toString());

                }
            }
            ListAdapter adapter = new SimpleAdapter(context, restaurantItems,
                    R.layout.list_restaurant,
                    new String[] {"name","address" }, new int[] {
                    R.id.name, R.id.address});
            restaurantList.setAdapter(adapter);
        }

    }

    private void initialization()
    {
        ticketBtn = (Button) findViewById(R.id.ticketBtn);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        searchField = (EditText) findViewById(R.id.searchField);
        areaDropdown = (Spinner) findViewById(R.id.areaDropdown);
        districtDropdown = (Spinner) findViewById(R.id.districtDropdown);
        restaurantList = (ListView) findViewById(R.id.restaurantList);
        districtDropDownId = (Spinner) findViewById(R.id.districtDropdownId);
        areaDropdownId = (Spinner) findViewById(R.id.areaDropdownId);
        hostName = getResources().getString(R.string.serverHost);
        districtDropDownId.setVisibility(View.INVISIBLE);
        areaDropdownId.setVisibility(View.INVISIBLE);
        listRestaurants = hostName +"/rms/restaurants";
        listDistricts = hostName + "/rms/districts";
        listAreas = hostName + "/rms/areas";
    }


}
