package com.example.google_el8alaba;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Starter extends AppCompatActivity {
    public static String serverIP;
    EditText ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        ip = findViewById(R.id.ip);
    }

    public void goToSearch(View view) {
        serverIP = ip.getText().toString();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void goToTrends(View view) {
        serverIP = ip.getText().toString();
        startActivity(new Intent(this, ShowTrends.class));
    }
}
