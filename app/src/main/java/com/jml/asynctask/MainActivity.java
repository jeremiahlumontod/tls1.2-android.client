package com.jml.asynctask;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


//public class MainActivity extends AppCompatActivity {
public class MainActivity extends Activity {

    TextView output;
    Button button;
    List<MyTask> tasks;
    ProgressBar pb;
    static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        tasks = new ArrayList<>();

        output = (TextView) findViewById(R.id.textView);
        output.setMovementMethod(new ScrollingMovementMethod());

        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);


        button = (Button)findViewById(R.id.login);

        button.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        System.out.println("OnClickListener");

                        //String uri = "http://10.0.2.2:8080";
                        String uri = "https://10.0.2.2:8443";
                        //String uri = "https://littlesvr.ca";
                        //String uri = "http://www.google.com";
                        if(isOnline()) {
                            requestData(uri);
                        }else{
                            showNoNetworkMessage();
                        }

                    }
                }
        );

    }

    private void showNoNetworkMessage() {
        Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
    }

    private void requestData(String uri) {
        MyTask task = new MyTask();
        task.execute(uri);
    }

    protected void updateDisplay(String message) {
        output.append(message + "\n");
    }


    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private class MyTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            updateDisplay("Starting task");

            if (tasks.size() == 0) {
                pb.setVisibility(View.VISIBLE);
            }
            tasks.add(this);
        }

        @Override
        protected String doInBackground(String... params) {
            //DefaultHttpClient client = new MyHttpsClient(context);
            /**try {
                Socket s = getSocketConnection("https://localhost",8443);
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            //String content = HttpManager.getData(params[0]);
            //String content = HttpsManager.getData(context, params[0]);
            //String content = HttpsManagerLocalhost.getData(context, params[0]);
            //String content = HttpsManagerLocalhost.getDataBks(context, params[0]); //one way authentication
            //String content = HttpsManagerApacheLib.getData(context, params[0]); //two way authentication
            String content = HttpsManagerAndroid.getData(context, params[0]); //one way authentication
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            updateDisplay(result);

            tasks.remove(this);
            if (tasks.size() == 0) {
                pb.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            updateDisplay(values[0]);
        }

    }



}
