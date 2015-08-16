package com.shagalalab.marshrutka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.shagalalab.marshrutka.data.DestinationPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aziz on 8/16/15.
 */
public class DestinationPointsAdapter extends ArrayAdapter<DestinationPoint> implements Filterable {
    LayoutInflater mInflater;
    ArrayFilter mFilter;
    List<DestinationPoint> mValues;
    List<DestinationPoint> mOriginalValues;

    public DestinationPointsAdapter(Context context, int resource, List<DestinationPoint> objects) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mValues = objects;
        mOriginalValues = new ArrayList<DestinationPoint>(objects);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        String text = getItem(position).getName();
        ((TextView) convertView).setText(text);
        return convertView;
    }

    @Override
    public int getCount() {
        return mValues.size();
    }

    @Override
    public DestinationPoint getItem(int position) {
        return mValues.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public long getItemId(int position) {
        DestinationPoint point = getItem(position);
        return point.ID;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {
        private Object lock = new Object();

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (lock) {
                    mOriginalValues = new ArrayList<DestinationPoint>(mValues);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                synchronized (lock) {
                    ArrayList<DestinationPoint> list = new ArrayList<DestinationPoint>(mOriginalValues);
                    results.values = list;
                    results.count = list.size();
                }
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                List<DestinationPoint> values = mOriginalValues;
                int count = values.size();

                ArrayList<DestinationPoint> newValues = new ArrayList<DestinationPoint>(count);

                for (int i = 0; i < count; i++) {
                    DestinationPoint destinationPoint = values.get(i);
                    String destNameCyr = destinationPoint.nameCyr.toLowerCase();
                    String destAlternativeNameCyr = destinationPoint.nameCyrAlternative.toLowerCase();

                    String destNameLat = destinationPoint.nameLat.toLowerCase();
                    String destAlternativeNameLat = destinationPoint.nameLatAlternative.toLowerCase();

                    if (destNameCyr.startsWith(prefixString) || destAlternativeNameCyr.startsWith(prefixString)
                            || destNameLat.startsWith(prefixString) || destAlternativeNameLat.startsWith(prefixString)) {
                        newValues.add(destinationPoint);
                    } else {
                        String[] destNameCyrWords = trimSymbols(destNameCyr).split(" ");
                        String[] destNameCyrAlternativeWords = trimSymbols(destAlternativeNameCyr).split(" ");
                        String[] destNameLatWords = trimSymbols(destNameLat).split(" ");
                        String[] destNameLatAlternativeWords = trimSymbols(destAlternativeNameLat).split(" ");

                        final int wordCount = destNameCyrWords.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (destNameCyrWords[k].startsWith(prefixString) ||
                                    destNameCyrAlternativeWords[k].startsWith(prefixString) ||
                                    destNameLatWords[k].startsWith(prefixString) ||
                                    destNameLatAlternativeWords[k].startsWith(prefixString)) {
                                newValues.add(destinationPoint);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        private String trimSymbols(String text) {
            return text.replace("(", "").replace(")", "").replace("-", " ");
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            if (results.values != null){
                mValues = (ArrayList<DestinationPoint>) results.values;
            } else {
                mValues = new ArrayList<DestinationPoint>();
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
