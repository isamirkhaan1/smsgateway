package com.samirkhan.apps.autosender;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import data.DownloadService;

public class MainActivity extends AppCompatActivity {

    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // service start/stop button
        btnStart = (Button) findViewById(R.id.btn_start);

        // check if app service is already running, if yes
        // update text and text_color..

        if (DownloadService.isThreadRunning) {
            Toast.makeText(this, "app running..", Toast.LENGTH_SHORT).show();

            btnStart.setText("Stop");
            btnStart.setTextColor(Color.RED);
        }

        // buttonStart actionListener
        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Button btnStart = (Button) v;

                // check whether user have set server URL, if not
                // Alert them, AND ALSO RETURN...
                String url = getSharedPreferences("com.samirkhan.apps.autosender.file", Context.MODE_PRIVATE).getString("url", null);
                if (url == null) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Put Server URL in Settings");
                    dialog.setMessage("\n");
                    dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                    return;
                }

                // check whether network connectivity is available, if not
                // Alert them, AND ALSO RETURN..
                if (!new NetworkConnection(getBaseContext()).isConnected()) {
                    DownloadService.isThreadRunning = false;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Network Connection Error");
                    dialog.setMessage("\n");
                    dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                    return;
                }

                // check if service already running stop it,
                // else start it and change text and textColor accord..
                if (!DownloadService.isThreadRunning) {
                    DownloadService.isThreadRunning = true;
                    startService(new Intent(getBaseContext(), DownloadService.class));
                    btnStart.setText("Stop");
                    btnStart.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    DownloadService.isThreadRunning = false;
                    stopService(new Intent(getBaseContext(), DownloadService.class));
                    btnStart.setText("Start");
                    btnStart.setTextColor(getResources().getColor(android.R.color.white));
                }


            }
        });

    }

    // inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // menuitem actionListener
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        new OptionMenuListener(this).Perform(item.getItemId());
        return super.onOptionsItemSelected(item);
    }
}
