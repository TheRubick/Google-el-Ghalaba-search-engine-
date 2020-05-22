package com.example.google_el8alaba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;

public class ShowResults extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_results);
        Intent intent = getIntent();
        String jsonArray = intent.getStringExtra("jsonArray");

        try {
            JSONArray array = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
