package com.example.google_el8alaba;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
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
import org.jsoup.Jsoup;

import java.util.ArrayList;

import static com.example.google_el8alaba.Starter.serverIP;

public class MainActivity extends AppCompatActivity {
  boolean mic_approved = false;
  public static final boolean TEST_MODE = false;
  public static final int REQUEST_MICROPHONE = 200;
  private SpeechRecognizer sr;
  AutoCompleteTextView query;
  TextView countryDisp;
  TextView voice_result;
  Button voice_btn;
  Thread thread;
  String CountryDomain;
  final String[] type = new String[1];
  RadioButton WebLinksRadio;
  RadioButton ImgsRadio;
    static final String SearchLinksRoute = "searchLinks";
    static final String SearchImagesRoute = "searchImages";
    static final String AutoCompleteRoute = "complete";
    static final String[] searchParams = {"query", "&CountryDomain"};
    static final String[] completeParams = {"part"};
  private ProgressDialog progress;
  private final int AutoCompleteMaxSuggestions = 7;
  final String[] mydata = new String[AutoCompleteMaxSuggestions];

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    query = findViewById(R.id.query_et);
    voice_btn = findViewById(R.id.search_voice_btn);
    countryDisp = findViewById(R.id.CountryCode);
    WebLinksRadio = findViewById(R.id.radioWeb);
    ImgsRadio = findViewById(R.id.radioImgs);
    voice_result = findViewById(R.id.sound_result);

    /** ***********************************auto complete process********************************* */
    addDummySuggestions(); // initially
    final ArrayAdapter<String> adapter =
            new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mydata);
    // populate the list to the AutoCompleteTextView controls
    query.setAdapter(adapter);
    query.addTextChangedListener(
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    changeSuggestions(adapter);
                    //query.showDropDown();
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    //changeSuggestions(adapter);
                    query.showDropDown();

                }
            });
    /** **********************************get country code*************************************** */
    thread =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  CountryDomain =
                      Jsoup.connect("https://ipapi.co/country_code/").get().body().text();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });

    thread.start();
    try {
      thread.join();
      countryDisp.setText(CountryDomain);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    /** **********************************speech recognition************************************ */
    sr = SpeechRecognizer.createSpeechRecognizer(this);
    sr.setRecognitionListener(new Listener());

    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

    // voice button logic
    voice_btn.setOnTouchListener(
        new View.OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
              if (check_mic_permission() || mic_approved) {
                RecordAudio();
                voice_result.setVisibility(View.VISIBLE);
                voice_btn.setText(R.string.voice_btn_hold_text);
                voice_btn.setBackgroundColor(getResources().getColor(R.color.googleRed));
                sr.startListening(intent);
              } else {
                Toast.makeText(
                        getApplicationContext(),
                        "permission microphone not available please try again",
                        Toast.LENGTH_SHORT)
                    .show();
              }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
              voice_btn.setText(R.string.search_by_voice);
              voice_btn.setBackgroundColor(getResources().getColor(R.color.googleBlue));
              sr.stopListening();
              voice_result.setVisibility(View.GONE);
              // voice_result.setText("");
            }
            return false;
          }
        });
  }

  private void addDummySuggestions() {
    mydata[0] = "facebook";
    mydata[1] = "google";
    mydata[2] = "wikipedia";
    mydata[3] = "china";
    mydata[4] = "egypt";
    mydata[5] = "mohamad salah";
    for (int i = 6; i < AutoCompleteMaxSuggestions; i++) {
      mydata[i] = " ";
    }
  }

  /**
   * refresh suggestions on every change of query text
   *
   * @param adapter : array adapter of edittext view to update it
   */
  private void changeSuggestions(final ArrayAdapter<String> adapter) {
    String url = getUrl(AutoCompleteRoute);
    JsonArrayRequest jsonArrayRequest =
            new JsonArrayRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            adapter.clear();

                            int loopLength = Math.min(AutoCompleteMaxSuggestions, response.length());
                            for (int i = 0; i < loopLength; i++) {
                                try {
                                    // mydata[i] = response.getString(i);
                                    adapter.insert(response.getString(i), i);
                                    Log.d("Suggestions : ", response.getString(i));
                                    query.showDropDown();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                // adapter.addAll(mydata);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley Error", error.toString());
                            error.printStackTrace();

                          if (error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(), "suggestions: Oops. network error!", Toast.LENGTH_SHORT)
                                    .show();
                          } else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(), "suggestions: Oops. server error!", Toast.LENGTH_SHORT)
                                    .show();
                          } else if (error instanceof AuthFailureError) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "suggestions: Oops. AuthFailureError error!",
                                    Toast.LENGTH_SHORT)
                                    .show();
                          } else if (error instanceof ParseError) {
                            Toast.makeText(getApplicationContext(), "suggestions: Oops. parse error!", Toast.LENGTH_SHORT)
                                    .show();
                          } else if (error instanceof TimeoutError) {
                            Toast.makeText(getApplicationContext(), "suggestions: Oops. Timeout error!", Toast.LENGTH_SHORT)
                                    .show();
                          }
                          addDummySuggestions();
                        }
                    });

    // Add the request to the RequestQueue.
    jsonArrayRequest.setRetryPolicy(
            new DefaultRetryPolicy(
                    3 * 1000,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
      VolleySingelton.getInstance(this).addToRequestQueue(jsonArrayRequest);
  }

  /** on clicking search button */
  public void searchQuery(View view) {
    // String queryText = query.getText().toString();
    // mydbAdapter.addNewQuery(queryText);
    progress = new ProgressDialog(this);
    progress.setTitle("Loading");
    progress.setMessage("Wait while loading...");
    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
    progress.show();
    if (ImgsRadio.isChecked()) type[0] = "Imgs";
    else type[0] = "Web";
    String url = type[0].equals("Web") ? getUrl(SearchLinksRoute) : getUrl(SearchImagesRoute);
    /** *******************************get search results to display***************************** */
      sendRealRequest(url);
  }

    /**
     * send dummy json array to display to test interface
     */
  private void TestJSON() {
    try {
      if (type[0].equals("Web")) sendTestwebJSONArray();
      else sendTestimageJSONArray();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  /**
   * send request to host asking for results (links/images)
   *
   * @param url : url to send the request to .. contains the route and parameters
   */
  private void sendRealRequest(String url) {
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
                Intent passResult;
                if (type[0].equals("Web"))
                  passResult = new Intent(getApplicationContext(), ShowResults.class);
                else passResult = new Intent(getApplicationContext(), ShowImgsResults.class);
                passResult.putExtra("jsonArray", response.toString());
                startActivity(passResult);
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
                  TestJSON();
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

  /** test web results showing */
  private void sendTestwebJSONArray() throws JSONException {
    final int dataMaxSize = 500;
    JSONObject[] Data = new JSONObject[dataMaxSize];
    Data[0] = new JSONObject();
    Data[0].put("title", "wikipedia");
    Data[0].put("link", "https://wikipedia.com");
    Data[0].put(
        "snippet",
        "Wikipedia is hosted by the Wikimedia Foundation, a non-profit organization that also hosts a range of other projects.");

    Data[1] = new JSONObject();
    Data[1].put("title", "google");
    Data[1].put("link", "https://google.com");
    Data[1].put(
        "snippet",
        "Google LLC is an American multinational technology company that specializes in Internet-related services and products, which include online advertising technologies, a search engine, cloud computing, software, and hardware. It is considered one of the Big Four technology companies alongside Amazon, Apple, and Facebook");

    for (int i = 2; i < dataMaxSize; i++) {
      Data[i] = new JSONObject();
      Data[i].put("title", "facebook" + (i + 1));
      Data[i].put("link", "https://facebook.com");
      Data[i].put(
          "snippet",
          "Facebook is an American online social media and social networking service based in Menlo Park,Facebook is an American online social media and social networking service based in Menlo Park,Facebook is an American online social media and social networking service based in Menlo Park California and a flagship service of the namesake company Facebook, Inc.");
    }

    JSONArray array = new JSONArray();
    for (JSONObject datum : Data) {
      array.put(datum);
    }

    Intent passResult;
    passResult = new Intent(getApplicationContext(), ShowResults.class);
    passResult.putExtra("jsonArray", array.toString());
    startActivity(passResult);
  }

  /** test image results showing */
  private void sendTestimageJSONArray() throws JSONException {
    final int dataMaxSize = 500;
    // Creating  JSONObject objects
    JSONObject[] Data = new JSONObject[dataMaxSize];
    Data[0] = new JSONObject();
    Data[0].put("title", "img1");
    Data[0].put("link", "https://i.imgur.com/tGbaZCY.jpg");

    Data[1] = new JSONObject();
      Data[1].put("title", "try");
    Data[1].put(
            "link",
            "upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Mohamed_Salah_2018.jpg/200px-Mohamed_Salah_2018.jpg");

    Data[2] = new JSONObject();
    Data[2].put("title", "img3");
    Data[2].put("link", "https://i.imgur.com/k0aIIHx.png");

    Data[3] = new JSONObject();
    Data[3].put("title", "img4");
    Data[3].put("link", "https://i.imgur.com/F9dYGWA.png");

    for (int i = 4; i < dataMaxSize; i++) {
      Data[i] = new JSONObject();
      Data[i].put("title", "img" + (i + 1));
      Data[i].put(
          "link",
          "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Mohamed_Salah_2018.jpg/200px-Mohamed_Salah_2018.jpg");
    }

    JSONArray array = new JSONArray();
    for (JSONObject datum : Data) {
      array.put(datum);
    }

    Intent passResult;
    passResult = new Intent(getApplicationContext(), ShowImgsResults.class);
    passResult.putExtra("jsonArray", array.toString());
    startActivity(passResult);
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
    if (Route.equals(SearchLinksRoute) || Route.equals(SearchImagesRoute)) {
      String queryText = query.getText().toString();
      URL.append(searchParams[0]).append("=");
      String[] words = queryText.split(" ");
      for (int i = 0; i < words.length; i++) {
        if (i == words.length - 1) {
          URL.append(words[i]); // last word
        } else {
          URL.append(words[i]).append("+");
        }
      }
      URL.append(searchParams[1]).append("=").append(CountryDomain);
    } else if (Route.equals(AutoCompleteRoute)) {
      String queryText = query.getText().toString();
      URL.append(completeParams[0]).append("=");
      String[] words = queryText.split(" ");
      for (String word : words) URL.append(word).append("+");
    }
    return URL.toString();
  }

  /** display hint */
  private void RecordAudio() {
    Toast.makeText(getApplicationContext(), "Keep holding while speaking .. ", Toast.LENGTH_SHORT)
        .show();
  }

  /** @return if permission is given or not and request it if not */
  private boolean check_mic_permission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {

      ActivityCompat.requestPermissions(
          this, new String[] {Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
      return false;
    } else return true;
  }

  /**
   * get user response on asking for permissions sent
   *
   * @param requestCode request defined previously to ensure consistency
   * @param permissions permissions that where asked to be given
   * @param grantResults granted or denied
   */
  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == REQUEST_MICROPHONE) {
      mic_approved =
          grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
  }

  /** listener to voice recognition button */
  class Listener implements RecognitionListener {

    @Override
    public void onReadyForSpeech(Bundle bundle) {}

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onRmsChanged(float v) {}

    @Override
    public void onBufferReceived(byte[] bytes) {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(int i) {}

    @Override
    public void onResults(Bundle results) {

      StringBuilder str = new StringBuilder();
      ArrayList data1 = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      if (data1 != null) {
        for (int i = 0; i < data1.size(); i++) {
          str.append(data1.get(i)).append("/");
        }
      }
      voice_result.setText(str.toString());

      ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      String word = "null";
      if (data != null) {
        word = (String) data.get(0);
      }
      String text = query.getText().toString() + " " + word;
      query.setText(text);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

      ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      String word = "null";
      if (data != null) {
        word = (String) data.get(data.size() - 1);
      }
      String text = voice_result.getText().toString() + " " + word;
      voice_result.setText(text);
    }

    @Override
    public void onEvent(int i, Bundle bundle) {}
  }
}
