package com.yogadwin.forex2;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout _swipeRefreshLayout1;
    private RecyclerView _recyclerView1;
    private TextView _timestampTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initSwipeRefreshLayout();
        _recyclerView1 = findViewById(R.id.recyclerView1);
        _timestampTextView = findViewById(R.id.timestampTextView);

        bindRecyclerView();
    }

    private void bindRecyclerView(){
        String ratesUrl = "https://openexchangerates.org/api/latest.json?app_id=5ca8deb7469445ce8e0492d49bbce929";
        String currencyUrl = "https://openexchangerates.org/api/currencies.json";

        AsyncHttpClient client = new AsyncHttpClient();
        Log.d("*tw*","accesing " + ratesUrl);

        client.get(ratesUrl, new AsyncHttpResponseHandler(){
            JSONObject ratesObj;

            long timestamp;

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                try {
                    JSONObject root = new JSONObject(new String(responseBody));
                    ratesObj = root.getJSONObject("rates");
                    timestamp = root.getLong("timestamp");

                    //Ambil kurs IDR (Untuk dijadikan base)
                    double idrRate = ratesObj.getDouble("IDR");

                    client.get(currencyUrl, new AsyncHttpResponseHandler() {
                        @Override
                        public void  onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                            try {
                                JSONObject curriencies = new JSONObject(new String(responseBody));
                                List<ForexModel> forexList = new ArrayList<>();
                                Iterator<String> keys = ratesObj.keys();
                                while (keys.hasNext()){
                                    String code = keys.next();
                                    if(code.equals("IDR")) continue; //IDR sudah jadi base, tidak perlu ditampilkan

                                    try {
                                        double rate = idrRate / ratesObj.getDouble(code);
                                        String name = curriencies.optString(code, "Unknown");
                                        forexList.add(new ForexModel(code, name, rate));
                                    }catch (JSONException e){
                                        Log.e("ForexError", "Gagal parsing untuk " + code, e);
                                    }
                                }

                                setTimestamp(timestamp);
                                ForexAdapter adapter = new ForexAdapter(forexList);
                                _recyclerView1.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                _recyclerView1.setAdapter(adapter);
                            } catch (JSONException e) {
                                Toast.makeText(MainActivity.this, "Gagal parsing data mata uang", Toast.LENGTH_SHORT).show();
                            }
                            _swipeRefreshLayout1.setRefreshing(false);
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(MainActivity.this,"Gagal ambil nama mata uang", Toast.LENGTH_SHORT).show();
                            _swipeRefreshLayout1.setRefreshing(false);
                        }
                    });
            }catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Gagal parsing kurs", Toast.LENGTH_SHORT).show();
                    _swipeRefreshLayout1.setRefreshing(false);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                    Log.e("*tw*", error.getMessage());
                 Toast.makeText(MainActivity.this, "Gagal parsing kurs", Toast.LENGTH_SHORT).show();
                _swipeRefreshLayout1.setRefreshing(false);
            }
        });
    }
    private void setTimestamp(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
        String dateTime = format.format(new Date(timestamp * 1000));
        _timestampTextView.setText("Tanggal dan Waktu (UTC): " + dateTime);
    }

    private void initSwipeRefreshLayout(){
        _swipeRefreshLayout1 = findViewById(R.id.swipeRefreshLayout1);
        _swipeRefreshLayout1.setOnRefreshListener(this::bindRecyclerView);
    }
}