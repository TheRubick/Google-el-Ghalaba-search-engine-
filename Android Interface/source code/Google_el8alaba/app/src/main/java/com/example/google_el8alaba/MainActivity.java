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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.jsoup.Jsoup;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
  boolean mic_approved = false;
  public static final int REQUEST_MICROPHONE = 200;
  private SpeechRecognizer sr;
  EditText query;
  TextView countryDisp;
  Button voice_btn;
  RequestQueue queue;
  Thread thread;
  String CountryDomain;
  RadioButton WebLinksRadio;
  RadioButton ImgsRadio;
  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    query = findViewById(R.id.query_et);
    voice_btn = findViewById(R.id.search_voice_btn);
    countryDisp = findViewById(R.id.CountryCode);
    WebLinksRadio =findViewById(R.id.radioWeb);
    ImgsRadio =findViewById(R.id.radioImgs);
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
            }
            return false;
          }
        });
    /** *********************************queue setup********************************************** */
    // Instantiate the cache
    Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

    // Set up the network to use HttpURLConnection as the HTTP client.
    Network network = new BasicNetwork(new HurlStack());

    // Instantiate the RequestQueue with the cache and network.
    queue = new RequestQueue(cache, network);

    // Start the queue
    queue.start();
  }

  /** on clicking search button */
  public void searchQuery(View view) {

    String queryText = query.getText().toString();
    final ProgressDialog progress = new ProgressDialog(this);
    progress.setTitle("Loading");
    progress.setMessage("Wait while loading...");
    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
    progress.show();

    final String[] type = new String[1];
    if(ImgsRadio.isChecked())
      type[0] = "Imgs";
    else
      type[0] = "Web";
    String url = getUrl(queryText, CountryDomain, type[0]);

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
                Intent passResult ;
                 if(type[0].equals("Web"))
                   passResult= new Intent(getApplicationContext(), ShowResults.class);
                 else
                   passResult= new Intent(getApplicationContext(), ShowImgsResults.class);
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

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null) {
                  Log.e("Status code", String.valueOf(networkResponse.statusCode));
                }
              }
            });

    // Add the request to the RequestQueue.
    queue.add(jsonArrayRequest); // no need for singleton as there is no continuous use for network in different activities
  }

  /**
   * build url containing all parameters needed to be sent to the host
   * @param query : text to be searched on
   * @param CountryDomain : two letters code representing the internet service provider of user
   * @param type : type of requested search (web / images)
   * @return : String containing all parameters needed to be sent to the host all concatenated
   */
  private String getUrl(String query, String CountryDomain,String type) {
    String[] words=query.split(" ");
    StringBuilder URL= new StringBuilder("localhost://" + "&text=");
    for (String word : words)
      URL.append(word).append("%");

    URL.append("&CountryCode=").append(CountryDomain);
    URL.append("&type=").append(type);
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
      /*
      String str = "";
      ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      if (data != null) {
        for (int i = 0; i < data.size(); i++)
        {
          str += data.get(i)+"/";
        }
      }
      query.setText(str);
      */
      ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      String word = "null";
      if (data != null) {
        word = (String) data.get(data.size() - 1);
      }
      String text = query.getText().toString() + " " + word;
      query.setText(text);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
      /*
      ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      String word = "null";
      if (data != null) {
        word = (String) data.get(data.size() - 1);
      }
      String text=query.getText().toString()+" "+word;
      query.setText(text);
      */
    }

    @Override
    public void onEvent(int i, Bundle bundle) {}
  }
}
