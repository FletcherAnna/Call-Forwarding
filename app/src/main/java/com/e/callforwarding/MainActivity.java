package com.e.callforwarding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.telephony.PhoneNumberUtils;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private int PERMISSION_REQUEST = 10;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Array to hold contacts
    ArrayList<Contact> arr = new ArrayList<Contact>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECEIVE_SMS,Manifest.permission.CALL_PHONE, Manifest.permission.FOREGROUND_SERVICE},
                PERMISSION_REQUEST);

        //build array for contact info
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Map<String, String> contacts = (Map<String, String>) sharedPref.getAll();
        for(Map.Entry<String,String> entry: contacts.entrySet()){
            arr.add(new Contact(entry.getKey(), entry.getValue() ));
        }

        ArrayAdapter<Contact> adapter = new ArrayAdapter<Contact>(this,
                R.layout.activity_listview, arr);

        //populate listView
        ListView listView = (ListView) findViewById(R.id.contactListView);
        listView.setAdapter(adapter);

        //set button listeners
        TextInputLayout nameInputLayout = findViewById(R.id.nameInput);
        TextInputLayout numberInputLayout = findViewById(R.id.numberInput);

        //listView listener
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String str = listView.getItemAtPosition(position).toString();
                String name = new String();
                int i = 0;
                while (i <  str.length() && (str.charAt(i) != (' '))){
                    name += str.charAt(i);
                    i++;
                }
                nameInputLayout.getEditText().setText(name);
            }
        }
        );


        final Button button = (Button) findViewById(R.id.addButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String nameText = nameInputLayout.getEditText().getText().toString();
                String numberText = PhoneNumberUtils.normalizeNumber(numberInputLayout.getEditText().getText().toString());
                nameInputLayout.getEditText().setText("");
                numberInputLayout.getEditText().setText("");
                if (numberText.length() > 10){
                    numberText = numberText.substring(numberText.length()-10);
                }
                //check for duplicate, add to array
                if (nameText.length() > 0 && numberText.length() > 0 ) {
                    int i = 0;
                    for(Contact s:arr){
                        if(s.getContactName().toLowerCase().equals(nameText.toLowerCase())){
                            i++;
                        }
                    }

                    if (i == 0) {
                        arr.add(new Contact(nameText, numberText));

                        //update listView and write change to disk
                        adapter.notifyDataSetChanged();
                        if (!sharedPref.contains(nameText)){
                            editor.putString(nameText, numberText);
                            editor.apply();
                        }
                    }
                }
            }
        });


        final Button removeButton = (Button) findViewById(R.id.removeButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String nameText = nameInputLayout.getEditText().getText().toString();
                if (nameText.length()>0){
                    int i = 0;
                    for(Contact s:arr){
                        if(s.getContactName().toLowerCase().equals(nameText.toLowerCase())){
                            arr.remove(s);
                            adapter.notifyDataSetChanged();
                            editor.remove(nameText);
                            editor.apply();
                            nameInputLayout.getEditText().setText("");
                        }
                    }
                }
            }
        });

        startService(new Intent(this,receiverService.class));
    }
}

