package com.infy.stg.estquido.admin.ui.buildings.fragments;

import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.ui.buildings.fragments.BuildingFragment.OnListFragmentInteractionListener;
import com.infy.stg.estquido.admin.ui.centers.fragments.CenterRecyclerViewAdapter;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

        holder.mIdView.setText(mValues.get(position).getKey());
        holder.mContentView.setText(mValues.get(position).getValue().toString());

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
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Map.Entry<String, Object> mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
