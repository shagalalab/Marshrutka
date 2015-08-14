package com.shagalalab.marshrutka;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.shagalalab.marshrutka.data.DestinationPoint;
import com.shagalalab.marshrutka.data.Route;
import com.shagalalab.marshrutka.db.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by aziz on 7/10/15.
 */
public class QueryByDestinationsFragment extends Fragment {

    private static final String TAG = "marshrutka";

    private ListView mListView;
    ArrayList<DestinationPoint> mStartDestinationPoints, mEndDestinationPoints;
    DbHelper mDbHelper;
    AutoCompleteTextView mStartPoint, mEndPoint;
    boolean mIsInterfaceCyrillic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_query_by_destination, null);
        mListView = (ListView) view.findViewById(android.R.id.list);

        mDbHelper = DbHelper.getInstance(getActivity());
        mIsInterfaceCyrillic = ((App)getActivity().getApplicationContext()).isCurrentLocaleCyrillic();

        mStartDestinationPoints = new ArrayList<DestinationPoint>(Arrays.asList(mDbHelper.destinationPoints));
        Comparator<DestinationPoint> comparator = mIsInterfaceCyrillic ? DestinationPoint.QQ_CYR_COMPARATOR
                                                                       : DestinationPoint.QQ_LAT_COMPARATOR;
        Collections.sort(mStartDestinationPoints, comparator);
        //mStartDestinationPoints.add(0, new DestinationPoint(-1, getString(R.string.choose_destination), getString(R.string.choose_destination)));

        mStartPoint = (AutoCompleteTextView) view.findViewById(R.id.spinner_start_point);
        mEndPoint = (AutoCompleteTextView) view.findViewById(R.id.spinner_end_point);

        DestinationPointsAdapter startPointAdapter = new DestinationPointsAdapter(getActivity(),
                0, mStartDestinationPoints);


/*
        String[] months = new String[] {
                "January", "February", "March", "April", "May", "June"
        };
        ArrayAdapter<String> monthsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, months);
        ArrayAdapter<DestinationPoint> destPoints = new ArrayAdapter<DestinationPoint>(getActivity(), android.R.layout.simple_list_item_1, mStartDestinationPoints);
*/

        mStartPoint.setAdapter(startPointAdapter);
        mStartPoint.setThreshold(1);

        mStartPoint.setOnItemSelectedListener(new StartPointItemSelectedListener());
        mEndPoint.setOnItemSelectedListener(new EndPointItemSelectedListener());
        mEndPoint.setThreshold(1);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.NEED_REFRESH_AUTOCOMPLETETEXT) {
            Utils.NEED_REFRESH_AUTOCOMPLETETEXT = false;
            mEndPoint.setText("");
            mStartPoint.setText("");
        }
    }

    private class StartPointItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            String selection = mStartPoint.getText().toString();
            Integer destinationID = mDbHelper.destinationToIdMapping.get(selection);
            //int startPointID = mStartDestinationPoints.get(startPointPosition).ID;
            if (destinationID == null) {
                mEndPoint.setAdapter(null);
                mListView.setAdapter(null);
            } else {
                mEndDestinationPoints = getDestinationListFromArray(mDbHelper.reachableDestinations[destinationID].reachableDestinationIds);
                DestinationPointsAdapter endPointAdapter = new DestinationPointsAdapter(getActivity(),
                        0, mEndDestinationPoints);
                mEndPoint.setAdapter(endPointAdapter);
                setListAdapter();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    private class EndPointItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            setListAdapter();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    private void setListAdapter() {
        Integer startPointID = mDbHelper.destinationToIdMapping.get(mStartPoint.getText().toString());
        Integer endPointID = mDbHelper.destinationToIdMapping.get(mEndPoint.getText().toString());

        int[] routeIds;
        /*if (startPointID == -1) {
            routeIds = mDbHelper.reverseRoutes[endPointID].routeIds;
        } else if (endPointID == -1) {
            routeIds = mDbHelper.reverseRoutes[startPointID].routeIds;
        } else {
            routeIds = mergeRoutes(mDbHelper.reverseRoutes[endPointID].routeIds,
                    mDbHelper.reverseRoutes[startPointID].routeIds);
        }*/
        if (startPointID == null || endPointID == null) {
            routeIds = new int[0];
        } else {
            routeIds = mergeRoutes(mDbHelper.reverseRoutes[startPointID].routeIds,
                                   mDbHelper.reverseRoutes[endPointID].routeIds);
        }
        int routesCount = routeIds.length;
        final Route[] filteredRoutes = new Route[routesCount];
        for (int i = 0; i < routesCount; i++) {
            filteredRoutes[i] = mDbHelper.routes[routeIds[i]];
        }
        DestinationsAdapter adapter = new DestinationsAdapter(getActivity(), 0, filteredRoutes, mIsInterfaceCyrillic);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(DetailActivity.ROUTE_ID, filteredRoutes[position].ID);
                startActivity(intent);
            }
        });
    }

    private ArrayList<DestinationPoint> getDestinationListFromArray(int[] destIds) {
        ArrayList<DestinationPoint> destinationPointList = new ArrayList<>();
        for (int destId : destIds) {
            destinationPointList.add(mDbHelper.destinationPoints[destId]);
        }
        Comparator<DestinationPoint> comparator = mIsInterfaceCyrillic ? DestinationPoint.QQ_CYR_COMPARATOR
                : DestinationPoint.QQ_LAT_COMPARATOR;
        Collections.sort(destinationPointList, comparator);
        //destinationPointList.add(0, new DestinationPoint(-1, getString(R.string.choose_destination), getString(R.string.choose_destination)));
        return destinationPointList;
    }

    private int[] mergeRoutes(int[] routeIds1, int[] routeIds2) {
        Arrays.sort(routeIds1);
        Arrays.sort(routeIds2);
        ArrayList<Integer> merged = new ArrayList<>();
        int pointer1 = 0;
        int pointer2 = 0;
        while (pointer1 < routeIds1.length && pointer2 < routeIds2.length) {
            if (routeIds1[pointer1] == routeIds2[pointer2]) {
                merged.add(routeIds1[pointer1]);
                pointer1++;
                pointer2++;
            } else if (routeIds1[pointer1] > routeIds2[pointer2]) {
                pointer2++;
            } else {
                pointer1++;
            }
        }
        int mergedSize = merged.size();
        int[] mergedRoutes = new int[mergedSize];
        for (int i = 0; i < mergedSize; i++) {
            mergedRoutes[i] = merged.get(i);
        }
        return mergedRoutes;
    }

    private class DestinationPointsAdapter extends ArrayAdapter<DestinationPoint> implements Filterable {
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

            String text = getItem(position).name;
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
                        /*for (DestinationPoint point : mOriginalValues) {
                            list.add(point.getName(mIsInterfaceCyrillic));
                        }*/
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
                        String destName = destinationPoint.name.toLowerCase();
                        String destAlternativeName = destinationPoint.nameAlternative.toLowerCase();

                        if (destName.startsWith(prefixString) || destAlternativeName.startsWith(prefixString)) {
                            newValues.add(destinationPoint);
                        } else {
                            String[] destNameWords = trimSymbols(destName).split(" ");
                            String[] destNameAlternativeWords = trimSymbols(destAlternativeName).split(" ");
                            final int wordCount = destNameWords.length;

                            // Start at index 0, in case valueText starts with space(s)
                            for (int k = 0; k < wordCount; k++) {
                                if (destNameWords[k].startsWith(prefixString) ||
                                        destNameAlternativeWords[k].startsWith(prefixString)) {
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
}