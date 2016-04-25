package com.tilatina.guardmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.tilatina.guardmonitor.Utilities.Preferences;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    Context me = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        try {
            startService(new Intent(this, ScheduleNotifierService.class));
            LoginActivity.loginActivity.finish();
        }catch (Exception e) {
            e.printStackTrace();
        }

        final Context me = this;
        sharedPreferences = getSharedPreferences(Preferences.MYPREFERENCES, MODE_PRIVATE);
        Button novelty = (Button) findViewById(R.id.novelty);
        Button rollCall = (Button) findViewById(R.id.rollCall);

        novelty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(me, NoveltyActivity.class);
                startActivity(intent);
            }
        });


        rollCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent();
                intent.setClass(me, RollCallActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();

        switch (item.getItemId()) {
            case  R.id.logout :
                Preferences.deletePreference(sharedPreferences, Preferences.TOKEN);
                intent.setClass(me, LoginActivity.class);
                try {
                    startActivity(intent);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                finish();
                return false;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
