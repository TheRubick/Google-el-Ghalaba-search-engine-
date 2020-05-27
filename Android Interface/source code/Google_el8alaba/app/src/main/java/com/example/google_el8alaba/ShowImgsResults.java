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

public class ShowImgsResults extends AppCompatActivity {
  private ImageResultsAdapter adapter;
  private ArrayList<ImageItem> imgItems = new ArrayList<>();
  private String jsonArray;
  private TextView counter;
  private int length;
  private final int pageCapacity = 10;
  private int currentStart = 0;
  private int currentEnd = length - 1;
  private JSONArray array;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_imgs_results);
    RecyclerView recyclerView = findViewById(R.id.imgsrecyclerView);
    counter = findViewById(R.id.counter);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter = new ImageResultsAdapter(imgItems, this);
    recyclerView.setAdapter(adapter);

    Intent intent = getIntent();
    jsonArray = intent.getStringExtra("jsonArray");
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

  /** show previous "pageCapacity" images */
  public void backIMGS(View view) {
    // DONE :: put in recycler view past 10 images
    currentStart = Math.max(currentStart - pageCapacity, 0);
    currentEnd = Math.min(currentStart + pageCapacity, length);
    showDataBetweenIndex12(currentStart, currentEnd);
  }

  /** show next "pageCapacity" images */
  public void nextIMGS(View view) {
    // DONE:: put in recycler view next 10 images
    currentStart = Math.min(currentStart + pageCapacity, length - 1);
    currentEnd = Math.min(currentEnd + pageCapacity, length);
    showDataBetweenIndex12(currentStart, currentEnd);
  }

  /**
   * show data from json file between index "from" -> "to"
   *
   * @param from : starting index of showing
   * @param to : end index to show = to-1
   */
  public void showDataBetweenIndex12(int from, int to) {
    try {
        String counterText = "Displaying from image \n " + from + " to " + to + " out of " + length;
      counter.setText(counterText);
      JSONObject[] imagesData = new JSONObject[length];
      imgItems.clear();
      for (int i = from; i <= to - 1; i++) {
        imagesData[i] = array.getJSONObject(i);
        String title = imagesData[i].getString("title");
        String imgUrl = imagesData[i].getString("link");
        imgItems.add(new ImageItem(imgUrl, title));
      }
      adapter.notifyDataSetChanged();

    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
