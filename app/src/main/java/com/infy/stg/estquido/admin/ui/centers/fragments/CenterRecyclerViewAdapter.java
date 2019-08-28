package com.infy.stg.estquido.admin.ui.centers.fragments;

import androidx.cardview.widget.CardView;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.ui.centers.CentersActivity;
import com.infy.stg.estquido.admin.ui.centers.fragments.CenterFragment.OnListFragmentInteractionListener;

import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Map} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CenterRecyclerViewAdapter extends RecyclerView.Adapter<CenterRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = CenterRecyclerViewAdapter.class.getName();
    private final ObservableList<Map> mValues;
    private final OnListFragmentInteractionListener mListener;

    public CenterRecyclerViewAdapter(ObservableList<Map> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mValues.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<Map>>() {
            @Override
            public void onChanged(ObservableList<Map> sender) {
                CenterRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(ObservableList<Map> sender, int positionStart, int itemCount) {
                CenterRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(ObservableList<Map> sender, int positionStart, int itemCount) {
                CenterRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeMoved(ObservableList<Map> sender, int fromPosition, int toPosition, int itemCount) {
                CenterRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeRemoved(ObservableList<Map> sender, int positionStart, int itemCount) {
                CenterRecyclerViewAdapter.this.notifyDataSetChanged();
            }
        });

        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_center, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.tvCenterName.setText((String) mValues.get(position).get("name"));
        holder.tvCenterID.setText((String) mValues.get(position).get("id"));
        holder.tvCity.setText((String) mValues.get(position).get("city"));

        if (((String) mValues.get(position).get("id")).equalsIgnoreCase(This.GPS_CENTER.get())) {
            holder.cardCenter.setCardBackgroundColor(Color.parseColor("#defde0"));
        }


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "CBL " + "CLICK");
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
        holder.ivCentersDelete.setOnClickListener(view -> Toast.makeText(This.CONTEXT.get(), "Long press to delete", Toast.LENGTH_SHORT).show());

        holder.ivCentersDelete.setOnLongClickListener(view -> {
            try {
                This.CBL_CENTERS.get().getDatabase().delete(This.CBL_CENTERS.get().getDatabase().getDocument("center_" + mValues.get(position).get("id")));
                CentersActivity.refresh();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tvCity;
        public final TextView tvCenterName;
        private final TextView tvCenterID;
        private final ImageView ivCentersDelete;
        private final CardView cardCenter;
        public Map mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvCenterID = view.findViewById(R.id.tvCenterID);
            tvCenterName = view.findViewById(R.id.tvCenterName);
            tvCity = view.findViewById(R.id.tvCity);
//            tvCity = view.findViewById(R.id.tv);
            ivCentersDelete = view.findViewById(R.id.ivCentersDelete);
            cardCenter = view.findViewById(R.id.cardCenter);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tvCenterName.getText() + "'";
        }
    }
}
