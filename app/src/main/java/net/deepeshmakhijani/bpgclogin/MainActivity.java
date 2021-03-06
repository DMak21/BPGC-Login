package net.deepeshmakhijani.bpgclogin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final String URL = "https://campnet.bits-goa.ac.in:8090/login.xml";

    public EditText user_data, pass_data;
    CircularProgressButton login_btn;
    Button logout_btn;
    CheckBox rc, sd;

    private SharedPreferences sharedPreferences, shared;
    private SharedPreferences.Editor editor, editor1;

//        Get current ssid
    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            ssid = connectionInfo.getSSID();
        }
        return ssid;
    }

    public void startService() {
        startService(new Intent(getBaseContext(), MyService.class));

    }

    //    Method to stop the service
    public void stopService() {
        stopService(new Intent(getBaseContext(), MyService.class));
    }    //    Method to start the service


    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        Hide anoying cursor on activity start
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        shared = getApplicationContext().getSharedPreferences("MyPref1", 0); // 0 - for private mode
        Boolean bol = shared.getBoolean("Auto", true);
        if (bol) {
            startService();
        } else {
            stopService();
        }

//        Setting the sharedPreferences
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = sharedPreferences.edit();

        editor1 = shared.edit();
        login_btn = (CircularProgressButton) findViewById(R.id.login_btn);
        logout_btn = (Button) findViewById(R.id.logout_btn);
        user_data = (EditText) findViewById(R.id.user_data);
        pass_data = (EditText) findViewById(R.id.pass_data);
        rc = (CheckBox) findViewById(R.id.rc);
        sd = (CheckBox) findViewById(R.id.sd);

//        Set Remember credentials initially to true
        rc.setChecked(true);
        sd.setChecked(true);

//        Set default username/password in edit text
        String user1, pass1;
        user1 = shared.getString("Default", null);
        user_data.clearFocus();
        user_data.setText(user1);
        pass1 = sharedPreferences.getString(user1, null);
        pass_data.setText(pass1);

//        LOGIN BUTTON
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_btn.startAnimation();

                String username = user_data.getText().toString().trim();
                String password = pass_data.getText().toString().trim();

                String wifi = getCurrentSsid(MainActivity.this);

                if (wifi == null) {
                    Toast.makeText(MainActivity.this, "Connect to WiFi Network\nOr check if Mobile Data is turned off", Toast.LENGTH_SHORT).show();
                    login_btn.revertAnimation();
                } else {
                    new Login().execute(username, password);
                }

//                Remember Credentials
                if (rc.isChecked()) {
                    editor.putString(username, password);
                    editor.commit();
                }
                if (sd.isChecked()) {
                    editor1.putString("Default", username);
                    editor1.commit();
                }
            }
        });

//        Set Default and Remember Credentials
        rc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    sd.setChecked(false);
                    sd.setEnabled(false);
                } else {
                    sd.setEnabled(true);
                }
            }
        });

//        LOGOUT BUTTON
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = user_data.getText().toString().trim();
                String password = pass_data.getText().toString().trim();
                new Logout().execute(username, password);
            }
        });

//        Set password automatically when username is written
        pass_data.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                final String username = user_data.getText().toString().trim();
                String pass = sharedPreferences.getString(username, null);
                pass_data.setText(pass);
            }
        });
    }

//        ON RESUME
    @Override
    public void onResume() {
        super.onResume();
        shared = getApplicationContext().getSharedPreferences("MyPref1", 0); // 0 - for private mode
        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
        user_data = (EditText) findViewById(R.id.user_data);
        pass_data = (EditText) findViewById(R.id.pass_data);
        String user1, pass1;
        user1 = shared.getString("Default", null);
        pass1 = sharedPreferences.getString(user1, null);
        user_data.setText(user1);
        pass_data.setText(pass1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(MainActivity.this, SettingsMain.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Login to https://10.1.0.10:8090/httpclient.html
    private class Login extends AsyncTask<String, Void, String> {
        //    Get okhttp client
        private OkHttpClient client = new OkHttpClient();
        private String result;
        private String message;

        @Override
        protected String doInBackground(String... params) {


            try {
//                Generate request body

                RequestBody body = new FormBody.Builder()
                        .add("username", params[0])
                        .add("password", params[1])
                        .add("mode", "191")
                        .build();
//                Make request object
                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();
//                Get response
                Response response = client.newCall(request).execute();
                result = response.body().string();
                return result;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(String result) {
            if (result != null) {
//                Get message using regex
                String pattern = Pattern.quote("<message><![CDATA[") + "(.*?)" + Pattern.quote("]]></message>");
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(result);
                if (m.find()) {
                    message = m.group(1);
                } else {
                    message = "Wait for the issue to be resolved";
                }
            } else {
                message = "Reconnect the WiFi!";
            }
            login_btn.revertAnimation();
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }

    }

    // Logout from https://10.1.0.10:8090/httpclient.html
    private class Logout extends AsyncTask<String, Void, String> {
        private String message;
        private OkHttpClient client = new OkHttpClient();
        private String result;

        @Override
        protected String doInBackground(String... params) {
            try {
                RequestBody body = new FormBody.Builder()
                        .add("username", params[0])
                        .add("password", params[1])
                        .add("mode", "193")
                        .build();

                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                result = response.body().string();
                return result;
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                String pattern = Pattern.quote("<message><![CDATA[") + "(.*?)" + Pattern.quote("]]></message>");
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(result);
                if (m.find()) {
                    message = m.group(1);
                } else {
                    message = "Kindly Reconnect the WiFi";
                }
            } else {
                message = "Kindly Reconnect the WiFi";
            }
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }

    }
}


