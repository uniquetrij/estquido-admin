package com.infy.stg.estquido.admin.ui.buildings.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.MutableDocument;
import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.ui.buildings.BuildingsActivity;
import com.infy.stg.estquido.admin.ui.buildings.fragments.BuildingFragment.OnListFragmentInteractionListener;

import java.util.List;
import java.util.Map;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Map.Entry} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BuildingRecyclerViewAdapter extends RecyclerView.Adapter<BuildingRecyclerViewAdapter.ViewHolder> {

    private final ObservableList<Map.Entry<String, Object>> mValues;
    private final OnListFragmentInteractionListener mListener;

    public BuildingRecyclerViewAdapter(ObservableList<Map.Entry<String, Object>> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mValues.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<Map.Entry<String, Object>>>() {
            @Override
            public void onChanged(ObservableList<Map.Entry<String, Object>> sender) {
                BuildingRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(ObservableList<Map.Entry<String, Object>> sender, int positionStart, int itemCount) {
                BuildingRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(ObservableList<Map.Entry<String, Object>> sender, int positionStart, int itemCount) {
                BuildingRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeMoved(ObservableList<Map.Entry<String, Object>> sender, int fromPosition, int toPosition, int itemCount) {
                BuildingRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onItemRangeRemoved(ObservableList<Map.Entry<String, Object>> sender, int positionStart, int itemCount) {
                BuildingRecyclerViewAdapter.this.notifyDataSetChanged();
            }
        });
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_building, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.tvBuildingID.setText(mValues.get(position).getKey());
        holder.tvBuildingLatitude.setText(((List<Double>) ((Map<String, Object>) (holder.mItem.getValue())).get("location")).get(0) + "");
        holder.tvBuildingLongitude.setText(((List<Double>) ((Map<String, Object>) (holder.mItem.getValue())).get("location")).get(1) + "");
        holder.tvBuildingName.setText((((Map<String, Object>) (holder.mItem.getValue())).getOrDefault("name", holder.tvBuildingID.getText()))+ "");

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        holder.ivBuildingDelete.setOnClickListener(view -> Toast.makeText(This.CONTEXT.get(), "Long press to delete", Toast.LENGTH_SHORT).show());

        holder.ivBuildingDelete.setOnLongClickListener(view -> {
            MutableDocument document = This.CBL_DATABASE.get().getDatabase().getDocument("buildings_" + This.CENTER.get()).toMutable();
            document.remove(holder.mItem.getKey());
            try {
                This.CBL_DATABASE.get().getDatabase().save(document);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            } finally {
                BuildingsActivity.refresh();
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
        public final TextView tvBuildingID;
        public final TextView tvBuildingName;
        private final TextView tvBuildingLatitude;
        private final TextView tvBuildingLongitude;
        private final ImageView ivBuildingDelete;
        public Map.Entry<String, Object> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tvBuildingID = view.findViewById(R.id.tvBuildingID);
            tvBuildingName = view.findViewById(R.id.tvBuildingName);
            tvBuildingLatitude = view.findViewById(R.id.tvBuildingLatitude);
            tvBuildingLongitude = view.findViewById(R.id.tvBuildingLongitude);
            ivBuildingDelete = view.findViewById(R.id.ivBuildingDelete);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tvBuildingName.getText() + "'";
        }
    }
}
