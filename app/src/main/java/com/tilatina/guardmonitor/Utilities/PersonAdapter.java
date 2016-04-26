package com.tilatina.guardmonitor.Utilities;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.tilatina.guardmonitor.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by jaime on 19/04/16.
 */
public class PersonAdapter extends BaseAdapter implements ListAdapter{

    private ArrayList<Person> persons;
    private Context context;

    public PersonAdapter(Context context, ArrayList<Person> persons) {
        this.persons = persons;
        this.context = context;
    }

    @Override
    public int getCount() {
        return persons.size();
    }

    @Override
    public Object getItem(int position) {
        return persons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater layoutInflater =(LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.roll_call_row, null);
        }

        final EditText name = (EditText) row.findViewById(R.id.personName);
        final Button sendButton =(Button)
                ((Activity)context).getWindow().findViewById(R.id.sendRollCall);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (0 != name.getText().length()) {
                    persons.get(position).setTile(name.getText().toString());
                    sendButton.setEnabled(true);
                }
            }
        });


        Button button = (Button) row.findViewById(R.id.deletePerson);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                persons.remove(position);
                notifyDataSetChanged();
            }
        });

        return row;
    }
}
