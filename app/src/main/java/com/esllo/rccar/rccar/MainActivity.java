package com.esllo.rccar.rccar;

import android.Manifest;
import android.content.Context;
import android.graphics.Point;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private DrawSheet sheet;
    private Point size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                0);
        getSupportActionBar().hide();
        StrictMode.enableDefaults();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Display display = getWindowManager().getDefaultDisplay();
         size = new Point();
        display.getSize(size);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_main, null);
        setContentView(view);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheet = new DrawSheet(MainActivity.this, size, ((EditText)findViewById(R.id.input)).getText().toString());
                setContentView(sheet);
            }
        });
        Toast.makeText(getApplicationContext(), "Gateway : "+getGateWay(), Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), "Server : "+getServerAddr(), Toast.LENGTH_SHORT).show();
    }

    public String getGateWay() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        return String.valueOf(dhcp.gateway);
    }

    @Override
    protected void onDestroy() {
        sheet.stop();
        super.onDestroy();
    }

    public String getServerAddr(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        return String.valueOf(dhcp.serverAddress);
    }

    class NetworkClient extends Thread{

    }
}
