package com.tilatina.guardmonitor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tilatina.guardmonitor.Utilities.Preferences;
import com.tilatina.guardmonitor.Utilities.WebService;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    public static Activity loginActivity;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Context me = this;
        loginActivity = this;

        sharedPreferences = getSharedPreferences(Preferences.MYPREFERENCES, MODE_PRIVATE);
        String token = Preferences.getPreference(sharedPreferences, Preferences.TOKEN, null);
        if (null != token) {
            Intent intent = new Intent();
            intent.setClass(me, MainActivity.class);
            try {
                startActivity(intent);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        Button login = (Button) findViewById(R.id.loginButton);
        final EditText phoneToken = (EditText) findViewById(R.id.phoneToken);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatePhone(phoneToken)) {

                    //Progress Dialog
                    final ProgressDialog loginDialog = new ProgressDialog(me);
                    loginDialog.setMessage("Cargando");
                    loginDialog.show();

                    WebService.loginAction(me, phoneToken.getText().toString(),
                            new WebService.LoginSuccessListener() {
                                @Override
                                public void onSuccess(String response) {
                                    try {
                                        JSONObject service = new JSONObject(response);
                                        if (service.getInt("id") != -1) {
                                            Preferences.putPreference(sharedPreferences,
                                                    Preferences.TOKEN, service.getString("id"));
                                            Intent intent = new Intent();
                                            intent.setClass(me, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(me, "El teléfono no existe", Toast.LENGTH_SHORT).show();
                                        }
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    loginDialog.hide();
                                }
                            },
                            new WebService.LoginErroListener() {
                                @Override
                                public void onError(String error) {
                                    loginDialog.hide();
                                    Toast.makeText(me, "Error de comunicaciones", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

    }

    private boolean validatePhone(EditText phoneToken) {
        if (phoneToken.getText().toString().length() == 0) {

            phoneToken.setError("El campo no puede estar vacio");
            return false;
        }

        return true;
    }
}
