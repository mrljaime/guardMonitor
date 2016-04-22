package com.tilatina.guardmonitor;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.tilatina.guardmonitor.Utilities.Person;
import com.tilatina.guardmonitor.Utilities.PersonAdapter;
import com.tilatina.guardmonitor.Utilities.Preferences;
import com.tilatina.guardmonitor.Utilities.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RollCallActivity extends AppCompatActivity{

    ArrayList<Person> persons = new ArrayList<>();
    ListView listView;
    PersonAdapter personAdapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll_call);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            LoginActivity.loginActivity.finish();
        }catch (Exception e) {
            e.printStackTrace();
        }


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
                Log.d(Preferences.MYPREFERENCES, String.format("Conteo arraylist = %s", persons.size()));
                if (0 != persons.size()) {
                    JSONArray object = getRollCallObject(persons);
                    object.toString();
                    String service = Preferences
                            .getPreference(getSharedPreferences(Preferences.MYPREFERENCES, MODE_PRIVATE),
                                    Preferences.TOKEN, null);
                    if (null != service) {
                        progressDialog = new ProgressDialog(RollCallActivity.this);
                        progressDialog.setMessage("Enviando datos");
                        progressDialog.show();
                        WebService.rollCallAction(RollCallActivity.this, service, object, new WebService.RollCallListener() {
                            @Override
                            public void onSuccess(String response) {
                                try {
                                    JSONObject jresponse = new JSONObject(response);
                                    if (404 == jresponse.getInt("error")) {
                                        Toast.makeText(RollCallActivity.this, "El número de" +
                                                        " servicio no existe. debe ingresar nuevamente o verificarlo son su supervisor",
                                                Toast.LENGTH_SHORT);
                                        Preferences.deletePreference(getSharedPreferences(Preferences.MYPREFERENCES,
                                                MODE_PRIVATE), Preferences.TOKEN);
                                        Intent intent = new Intent();
                                        intent.setClass(RollCallActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }catch (Exception e) {
                                }
                                try {
                                    JSONObject jsonResponde = new JSONObject(response);
                                    String date = jsonResponde.getString("nextDueDate");
                                    if ("null" != date) {
                                        Log.d(Preferences.MYPREFERENCES, date);
                                        Preferences.putPreference(getSharedPreferences(Preferences.MYPREFERENCES, MODE_PRIVATE),
                                                "date", date);
                                        Preferences.setAlarmReceiver(getApplicationContext());
                                    }
                                }catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                progressDialog.hide();
                                persons.clear();
                                personAdapter.notifyDataSetChanged();
                                Toast.makeText(RollCallActivity.this, "Datos enviados", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }, new WebService.RollCallErrorListener() {
                            @Override
                            public void onError(String error) {
                                progressDialog.hide();
                                Toast.makeText(RollCallActivity.this, "Error de comunicaciones", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(RollCallActivity.this, "No se puede mandar asistencia sin capturar información.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private JSONArray getRollCallObject(ArrayList<Person> persons) {
        JSONArray object = new JSONArray();
        for (int i = 0; i < persons.size(); i++) {
            Log.d(Preferences.MYPREFERENCES, persons.get(i).getTitle());
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", persons.get(i).getTitle());
                object.put(jsonObject);
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return object;
    }

}
