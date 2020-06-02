package com.example.google_el8alaba;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.ArrayList;

import static com.example.google_el8alaba.Starter.serverIP;

public class LinksResultsAdapter extends RecyclerView.Adapter<LinksResultsAdapter.ViewHolder> {
    private ArrayList<LinkItem> linkItems;
    private Context context;
    private static final String PersonalizedRoute = "personalized";
    private static final String[] PersonalizedParams = {"link"};

    public LinksResultsAdapter(ArrayList<LinkItem> linkItems, Context context) {
        this.linkItems = linkItems;
        this.context = context;
    }

    /**
     * Called when RecyclerView needs a new {@link LinksResultsAdapter.ViewHolder} of the given type to represent an item.
     * This new ViewHolder should be constructed with a new View that can represent the items of the
     * given type. You can either create a new View manually or inflate it from an XML layout file.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an
     *                 adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(LinksResultsAdapter.ViewHolder, int)
     */
    @NonNull
    @Override
    public LinksResultsAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.link_item, parent, false);
        return new LinksResultsAdapter.ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should update
     * the contents of the {@link LinksResultsAdapter.ViewHolder#itemView} to reflect the item at the given position.
     *
     * <p>Note that unlike {@link ListView}, RecyclerView will not call this method again if the
     * position of the item changes in the data set unless the item itself is invalidated or the new
     * position cannot be determined. For this reason, you should only use the <code>position</code>
     * parameter while acquiring the related data item inside this method and should not keep a copy
     * of it. If you need the position of an item later on (e.g. in a click listener), use {@link
     * LinksResultsAdapter.ViewHolder#getAdapterPosition()} which will have the updated adapter position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at
     *                 the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull LinksResultsAdapter.ViewHolder holder, int position) {
        LinkItem item = linkItems.get(position);
        holder.titleLINK.setText(item.getTitle());
        holder.urlLINK.setText(item.getLink());
        holder.snippetLINK.setText(item.getSnippet());

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return linkItems.size();
    }

    /**
     * ******************************************************************************
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleLINK;
        TextView urlLINK;
        TextView snippetLINK;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleLINK = itemView.findViewById(R.id.titleLINK);
            urlLINK = itemView.findViewById(R.id.urlLINK);
            snippetLINK = itemView.findViewById(R.id.snippetLINK);
            //snippetLINK.setMovementMethod(new ScrollingMovementMethod());
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            LinkItem itemClicked = linkItems.get(position);
            String url = itemClicked.getLink();
            //TODO :: send put request
            sendPersonalized(url);
            if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);
            Toast.makeText(context, "opening link in browser", Toast.LENGTH_SHORT).show();
        }

        /**
         * build url containing all parameters needed to be sent to the host
         *
         * @param link  : link to add to parameters
         * @param Route : specification for request type to modify parameters sent with request
         * @return : String containing all parameters needed to be sent to the host all concatenated
         */
        private String getUrl(String link, String Route) {
            String host = "http://" + serverIP + "/";
            StringBuilder URL = new StringBuilder(host + Route + "?");
            if (Route.equals(PersonalizedRoute)) {
                URL.append(PersonalizedParams[0]).append("=").append(link);
            }
            return URL.toString();
        }

        /**
         * send request to add link as favoured to get better priority next time
         *
         * @param link : link pressed
         */
        private void sendPersonalized(String link) {
            String url = getUrl(link, PersonalizedRoute);
            JsonArrayRequest jsonArrayRequest =
                    new JsonArrayRequest(
                            Request.Method.PUT,
                            url,
                            null,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    // response
                                    Log.d("personalized Response :", response.toString());
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    Log.d("personalized Error:", error.toString());
                                }
                            });

            // Add the request to the RequestQueue.
            VolleySingelton.getInstance(context).addToRequestQueue(jsonArrayRequest);
        }
    }
}
