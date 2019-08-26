package com.infy.stg.estquido.admin.ui.main.fragments;

import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.ui.main.fragments.CenterFragment.OnListFragmentInteractionListener;

import java.util.List;
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
        holder.mIdView.setText((String) mValues.get(position).get("name"));
        holder.mContentView.setText((String) mValues.get(position).get("city"));

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
        public Map mItem;

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
