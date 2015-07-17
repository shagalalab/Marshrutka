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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.shagalalab.marshrutka.data.DestinationPoint;
import com.shagalalab.marshrutka.data.Route;
import com.shagalalab.marshrutka.db.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by aziz on 7/10/15.
 */
public class QueryByDestinationsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private ListView mListView;
    ArrayList<DestinationPoint> mDestinationPoints;
    DbHelper mDbHelper;
    Spinner mStartPoint, mEndPoint;
    private View mEmptyView, mClearedView;
    private DestinationsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_query_by_destination, null);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        mClearedView = view.findViewById(R.id.clear_list);

        mDbHelper = DbHelper.getInstance(getActivity());

        mDestinationPoints = new ArrayList<DestinationPoint>(Arrays.asList(mDbHelper.destinationPoints));
        Collections.sort(mDestinationPoints);
        mDestinationPoints.add(0, new DestinationPoint(-1, getString(R.string.choose_destination)));

        mStartPoint = (Spinner) view.findViewById(R.id.spinner_start_point);
        mEndPoint = (Spinner) view.findViewById(R.id.spinner_end_point);

        DestinationPointsAdapter startPointAdapter = new DestinationPointsAdapter(getActivity(),
                0, mDestinationPoints);
        DestinationPointsAdapter endPointAdapter = new DestinationPointsAdapter(getActivity(),
                0, mDestinationPoints);

        mStartPoint.setAdapter(startPointAdapter);
        mEndPoint.setAdapter(endPointAdapter);

        mStartPoint.setOnItemSelectedListener(this);
        mEndPoint.setOnItemSelectedListener(this);


        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        int startPointPosition = mStartPoint.getSelectedItemPosition();
        int endPointPosition = mEndPoint.getSelectedItemPosition();
        int startPointID = mDestinationPoints.get(startPointPosition).ID;
        int endPointID = mDestinationPoints.get(endPointPosition).ID;
        int[] routeIds;

        if (startPointID == -1 && endPointID == -1) {
            if (adapter != null) {
                adapter = new DestinationsAdapter(getActivity(), 0, new Route[0]);
                mListView.setEmptyView(mClearedView);
                mListView.setAdapter(adapter);
            }
            return;
        } else if (startPointID == -1) {
            routeIds = mDbHelper.reverseRoutes[endPointID - 1].routeIds;
        } else if (endPointID == -1) {
            routeIds = mDbHelper.reverseRoutes[startPointID - 1].routeIds;
        } else {
            routeIds = mergeRoutes(mDbHelper.reverseRoutes[endPointID - 1].routeIds,
                    mDbHelper.reverseRoutes[startPointID - 1].routeIds);
        }
        int routesCount = routeIds.length;
        final Route[] filteredRoutes = new Route[routesCount];
        for (int i = 0; i < routesCount; i++) {
            filteredRoutes[i] = mDbHelper.routes[routeIds[i] - 1];
        }
        adapter = new DestinationsAdapter(getActivity(), 0, filteredRoutes);
        mListView.setEmptyView(mEmptyView);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(DetailActivity.ROUTE_ID, filteredRoutes[position].ID-1);
                startActivity(intent);
            }
        });
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

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }


    private class DestinationPointsAdapter extends ArrayAdapter<DestinationPoint> {
        LayoutInflater mInflater;

        public DestinationPointsAdapter(Context context, int resource, List<DestinationPoint> objects) {
            super(context, resource, objects);
            mInflater = LayoutInflater.from(context);
        }

        private View getCustomView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            ((TextView) convertView).setText(getItem(position).name);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }
    }
}