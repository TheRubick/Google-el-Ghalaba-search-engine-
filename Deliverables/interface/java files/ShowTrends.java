package com.example.google_el8alaba;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.google_el8alaba.Starter.serverIP;

public class ShowTrends extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
  ArrayList<String> countries;
  ArrayList<String> codes;
  private TrendsAdapter adapter2;
  private ArrayList<TrendItem> trendItems = new ArrayList<>();
  public String chosenCountry;
  static final String TrendsRoute = "trends";
  static final String[] trendsParams = {"CountryDomain"};
  private ProgressDialog progress;
  private final int pageCapacity = 10;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_show_trends);
    /** ************************setup dropbox to choose country******************************** */
    Spinner spinner = findViewById(R.id.countryspinner);
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(this, R.array.countries_array, R.layout.spinner_item);
    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(this);
    countries =
        new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.countries_array)));
    codes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.codes_array)));
    spinner.setSelection(65); // egypt as default value
    /** *************************setup recycler view to show trends***************************** */
    RecyclerView recyclerView = findViewById(R.id.trends_recyclerview);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter2 = new TrendsAdapter(trendItems, this);
    recyclerView.setAdapter(adapter2);
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
    Toast.makeText(
            getApplicationContext(),
            countries.get(position) + " code : " + codes.get(position),
            Toast.LENGTH_LONG)
        .show();
    chosenCountry = codes.get(position);
    progress = new ProgressDialog(this);
    progress.setTitle("Loading");
    progress.setMessage("Wait while loading...");
    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
    progress.show();
    getTrends();
  }

  private void getTrends() {
    String url = getUrl(TrendsRoute);
    JsonArrayRequest jsonArrayRequest =
        new JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            new Response.Listener<JSONArray>() {
              @Override
              public void onResponse(JSONArray response) {
                // To dismiss the dialog
                progress.dismiss();
                try {
                  JSONObject[] trendsData = new JSONObject[response.length()];
                  trendItems.clear();
                  int length = Math.min(response.length(), pageCapacity);
                  for (int i = 0; i < length; i++) {
                    trendsData[i] = response.getJSONObject(i);
                    String name = trendsData[i].getString("name");
                    if(name.equals("N/A")) break;
                    String count = trendsData[i].getString("count");
                    trendItems.add(new TrendItem(name, count));
                  }
                  adapter2.notifyDataSetChanged();
                } catch (JSONException e) {
                  e.printStackTrace();
                }
              }
            },
            new Response.ErrorListener() {
              @Override
              public void onErrorResponse(VolleyError error) {
                // To dismiss the dialog
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "This didn't work .. ", Toast.LENGTH_LONG)
                    .show();
                Log.e("Volley Error", error.toString());
                error.printStackTrace();
                if (error instanceof NetworkError) {
                  Toast.makeText(getApplicationContext(), "Oops. network error!", Toast.LENGTH_LONG)
                      .show();
                } else if (error instanceof ServerError) {
                  Toast.makeText(getApplicationContext(), "Oops. server error!", Toast.LENGTH_LONG)
                      .show();
                } else if (error instanceof AuthFailureError) {
                  Toast.makeText(
                          getApplicationContext(),
                          "Oops. AuthFailureError error!",
                          Toast.LENGTH_LONG)
                      .show();
                } else if (error instanceof ParseError) {
                  Toast.makeText(getApplicationContext(), "Oops. parse error!", Toast.LENGTH_LONG)
                      .show();
                } else if (error instanceof TimeoutError) {
                  Toast.makeText(getApplicationContext(), "Oops. Timeout error!", Toast.LENGTH_LONG)
                      .show();
                }
                addFakeData();
              }
            });

    jsonArrayRequest.setRetryPolicy(
        new DefaultRetryPolicy(
            30 * 1000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    // Add the request to the RequestQueue.
    VolleySingelton.getInstance(this).addToRequestQueue(jsonArrayRequest);
  }

  private void addFakeData() {
    trendItems.clear();
    for (int i = 0; i < pageCapacity; i++) {
      String name = "fake name";
      String count = "9999";
      trendItems.add(new TrendItem(name, count));
    }
    adapter2.notifyDataSetChanged();
  }

  /**
   * build url containing all parameters needed to be sent to the host
   *
   * @param Route : specification for request type to modify parameters sent with request
   * @return : String containing all parameters needed to be sent to the host all concatenated
   */
  private String getUrl(String Route) {
    String host = "http://" + serverIP + "/";
    StringBuilder URL = new StringBuilder(host + Route + "?");
    if (Route.equals(TrendsRoute)) {
      URL.append(trendsParams[0]).append("=").append(chosenCountry);
    }
    return URL.toString();
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {
    /*do nothing*/
  }
}
