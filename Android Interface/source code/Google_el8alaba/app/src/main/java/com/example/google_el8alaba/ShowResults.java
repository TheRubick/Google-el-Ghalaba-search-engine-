package com.example.google_el8alaba;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowResults extends AppCompatActivity {
  private LinksResultsAdapter adapter;
  private ArrayList<LinkItem> linkItems = new ArrayList<>();
  private TextView counter;
  private int length;
  private final int pageCapacity = 15;
  private int currentStart = 0;
  private int currentEnd = length - 1;
  private JSONArray array;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_results);
    RecyclerView recyclerView = findViewById(R.id.linksrecyclerView);
    counter = findViewById(R.id.counter2);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter = new LinksResultsAdapter(linkItems, this);
    recyclerView.setAdapter(adapter);

    Intent intent = getIntent();
      String jsonArray = intent.getStringExtra("jsonArray");

    try {
      array = new JSONArray(jsonArray);
      length = array.length();
    } catch (JSONException e) {
      e.printStackTrace();
    }
    currentStart = 0;
    currentEnd = Math.min(length, pageCapacity);
    showDataBetweenIndex12(currentStart, currentEnd); // initially
  }

  /** show next "pageCapacity" links */
  public void nextLinks(View view) {
    // TODO:: put in recycler view next 10 links
    currentStart = Math.min(currentStart + pageCapacity, length - 1);
    currentEnd = Math.min(currentEnd + pageCapacity, length);
    showDataBetweenIndex12(currentStart, currentEnd);
  }

  /** show next "pageCapacity" links */
  public void backLinks(View view) {
    // TODO:: put in recycler view past 10 links
    currentStart = Math.max(currentStart - pageCapacity, 0);
    currentEnd = Math.min(currentStart + pageCapacity, length);
    showDataBetweenIndex12(currentStart, currentEnd);
  }

  /**
   * show data from json file between index "from" -> "to"
   *
   * @param from : starting index of showing
   * @param to : end index to show = to-1
   */
  private void showDataBetweenIndex12(int from, int to) {
    try {
        String counterText = "Displaying from result \n " + from + " to " + to + " out of " + length;
      counter.setText(counterText);
      JSONObject[] linksData = new JSONObject[length];
      linkItems.clear();
      for (int i = from; i <= to - 1; i++) {
        linksData[i] = array.getJSONObject(i);
        String title = linksData[i].getString("title");
        String link = linksData[i].getString("link");
        String snippet = linksData[i].getString("snippet");
        linkItems.add(new LinkItem(title, link, snippet));
      }
      adapter.notifyDataSetChanged();

    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
