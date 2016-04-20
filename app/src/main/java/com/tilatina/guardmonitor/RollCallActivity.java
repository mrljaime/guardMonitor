package com.tilatina.guardmonitor;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.tilatina.guardmonitor.Utilities.Person;
import com.tilatina.guardmonitor.Utilities.PersonAdapter;
import com.tilatina.guardmonitor.Utilities.Preferences;

import java.util.ArrayList;
import java.util.List;

public class RollCallActivity extends AppCompatActivity{

    ArrayList<Person> persons = new ArrayList<>();
    ListView listView;
    PersonAdapter personAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll_call);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.rollCallRecycler);
        listView.setAdapter(personAdapter = new PersonAdapter(this, persons));


        Button addItem = (Button) findViewById(R.id.addItem);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Person person = new Person();
                Log.d(Preferences.MYPREFERENCES, "Creando un nuevo elemento persona.");
                persons.add(person);
                Log.d(Preferences.MYPREFERENCES, String.format("Conteo de personas = %s", persons.size()));
                personAdapter.notifyDataSetChanged();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
