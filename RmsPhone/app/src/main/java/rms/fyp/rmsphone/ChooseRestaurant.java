package rms.fyp.rmsphone;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChooseRestaurant extends ActionBarActivity {
        private ArrayList<String> areaItems,districtItems,areaIds,districtIds;
        private ArrayList<HashMap<String,String>> restaurantItems;
        private Button ticketBtn,submitBtn;
        private Spinner areaDropdown,districtDropdown,districtDropDownId,areaDropdownId;
        private ListView restaurantList;
        private EditText searchField;
        private Context context = this;
        private String wsForCustomerReg,listRestaurants,listDistricts,listAreas,customerId,areaId,districtId,hostName,regId,projectNumber;
        //Customer related
        public static final String PROPERTY_CUSTOMER_ID = "customer_id";
        //GCM related var
        private GoogleCloudMessaging gcm;
        public static final String PROPERTY_REG_ID = "registration_id";
        private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        private static final String PROPERTY_APP_VERSION = "appVersion";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_restaurant);
        initialization();
        getRegId();
        getCustomerId();

    }

    //customer getting fucntions
    private void getCustomerId()
    {
        customerId = getCustomerId(context);
        if(customerId.isEmpty())
        {
            new registerCustomer().execute(wsForCustomerReg);
        }


    }

    private void storeCustomerId(Context context, String customerId) {
        final SharedPreferences prefs = getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_CUSTOMER_ID, customerId);
        editor.commit();
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
        wsForCustomerReg = hostName +"/rms/customer/create";
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
        ticketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context,ViewTicket.class);
                startActivity(intent);
            }
        });
        projectNumber = "836861541346";
    }

    //web service related function
    private class getAreas  extends AsyncTask<String, Void, String> {
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
                    areaItems.add("Area");
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
                    districtItems.add("District");
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

    private class registerCustomer  extends AsyncTask<String, Void, String> {
        // Required initialization

        private static final int REGISTRATION_TIMEOUT = 3 * 1000;
        private static final int WAIT_TIMEOUT = 30 * 1000;
        private final HttpClient httpclient = new DefaultHttpClient();

        final HttpParams params = httpclient.getParams();
        HttpResponse response;
        private String content =  null;
        private boolean error = false;
        // Call after onPreExecute method
        protected String doInBackground(String... urls) {
            String URL = wsForCustomerReg;
            try {

                //URL passed to the AsyncTask
                URL = urls[0];
                HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, WAIT_TIMEOUT);
                ConnManagerParams.setTimeout(params, WAIT_TIMEOUT);


                HttpPost httpPost = new HttpPost(URL);

                //Any other parameters you would like to set
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("regId",regId));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //Response from the Http Request
                response = httpclient.execute(httpPost);

                StatusLine statusLine = response.getStatusLine();
                //Check the Http Request for success
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    content = out.toString();
                }
                else{
                    //Closes the connection.
                    Log.w("HTTP1:",statusLine.getReasonPhrase());
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }


            } catch (ClientProtocolException e) {
                Log.w("HTTP2:",e );
                content = e.getMessage();
                error = true;
                cancel(true);
            } catch (IOException e) {
                Log.w("HTTP3:",e );
                content = e.getMessage();
                error = true;
                cancel(true);
            }catch (Exception e) {
                Log.w("HTTP4:",e );
                content = e.getMessage();
                error = true;
                cancel(true);
            }

            return content;
        }

        protected void onPostExecute(String content) {
            // NOTE: You can call UI Element here.


                try {
                    JSONObject jsonObj = new JSONObject(content);
                    Log.wtf("Object from post is : ",jsonObj.toString());
                    customerId = jsonObj.optString("id");
                    storeCustomerId(context,customerId);
                    Log.wtf("Customer id = :",customerId);
                } catch (JSONException e) {
                    Log.wtf("json problems",e.toString());
                }
            }


    }
    //GCM function method
    //function for getting the RegId , if not stored ,register instead
    private void getRegId()
    {
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(context);

            if (regId.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(this.getClass().toString() + " GCM", "No valid Google Play Services APK found.");
        }

    }

    //register the device for the GCM
    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regId = gcm.register(projectNumber);
                    msg = "Device registered, registration ID=" + regId;
                    Log.wtf("GCM",  msg);
                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.wtf("error from GCM : ",msg);

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }
    //check the availablity for the Play service from google
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(this.getClass().toString()+" GCM : ", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    //getRegistrationId from system
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(this.getClass().toString()+ " GCM", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(this.getClass().toString() + " GCM", "App version changed.");
            return "";
        }
        return registrationId;
    }

    //get the share preference from system
    private SharedPreferences getSharedPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(ChooseRestaurant.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    //get version from system
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    //store the regId
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(this.getClass().toString() + " GCM ", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
}
