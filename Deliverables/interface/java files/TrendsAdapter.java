package com.example.google_el8alaba;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TrendsAdapter extends RecyclerView.Adapter<TrendsAdapter.ViewHolder> {
    private ArrayList<TrendItem> trendItems;
    private Context context;

    public TrendsAdapter(ArrayList<TrendItem> trendItems, Context context) {
        this.trendItems = trendItems;
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
     * @see #onBindViewHolder(TrendsAdapter.ViewHolder, int)
     */
    @NonNull
    @Override
    public TrendsAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.trend_item, parent, false);
        return new TrendsAdapter.ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should update
     * the contents of the {@link TrendsAdapter.ViewHolder#itemView} to reflect the item at the given position.
     *
     * <p>Note that unlike {@link ListView}, RecyclerView will not call this method again if the
     * position of the item changes in the data set unless the item itself is invalidated or the new
     * position cannot be determined. For this reason, you should only use the <code>position</code>
     * parameter while acquiring the related data item inside this method and should not keep a copy
     * of it. If you need the position of an item later on (e.g. in a click listener), use {@link
     * TrendsAdapter.ViewHolder#getAdapterPosition()} which will have the updated adapter position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at
     *                 the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull TrendsAdapter.ViewHolder holder, int position) {
        TrendItem item = trendItems.get(position);
        holder.name.setText(item.getName());
        holder.count.setText(item.getCount());

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return trendItems.size();
    }

    /**
     * ******************************************************************************
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView count;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.trendName);
            count = itemView.findViewById(R.id.trendCount);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TrendItem itemClicked = trendItems.get(position);
            //do nothing TODO :: try to do something
        }
    }
}

