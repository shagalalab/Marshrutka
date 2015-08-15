package com.shagalalab.marshrutka;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import com.shagalalab.marshrutka.data.DestinationPoint;
import com.shagalalab.marshrutka.data.Route;
import com.shagalalab.marshrutka.db.DbHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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

        mStartPoint = (AutoCompleteTextView) view.findViewById(R.id.spinner_start_point);
        mEndPoint = (AutoCompleteTextView) view.findViewById(R.id.spinner_end_point);

        DestinationPointsAdapter startPointAdapter = new DestinationPointsAdapter(getActivity(),
                0, mStartDestinationPoints);

        mStartPoint.setAdapter(startPointAdapter);
        mStartPoint.setThreshold(1);
        mStartPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStartPoint.hasFocus()) {
                    showDropDownIfPossible(mStartPoint);
                }
            }
        });
        mStartPoint.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = mStartPoint.getText().toString();
                Integer destinationID = mDbHelper.destinationToIdMapping.get(selection);
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
        });
        mStartPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Log.d("autocomplete", "START: charseq="+charSequence+", start="+start+", before="+before+", count="+count);
                if (before > 0 && charSequence.length() == 0) {
                    // reset autocomplete's adapter when text is empty
                    setListAdapter();
                    showDropDownIfPossible(mStartPoint);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        mEndPoint.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setListAdapter();
                hideKeyboard();
            }
        });
        mEndPoint.setThreshold(1);
        mEndPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Log.d("autocomplete", "END: charseq="+charSequence+", start="+start+", before="+before+", count="+count);
                if (before > 0 && charSequence.length() == 0) {
                    // reset autocomplete's adapter when text is empty
                    setListAdapter();
                    showDropDownIfPossible(mEndPoint);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mEndPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEndPoint.hasFocus()) {
                    showDropDownIfPossible(mEndPoint);
                }
            }
        });
        mEndPoint.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    showDropDownIfPossible(mEndPoint);
                }
            }
        });

        return view;
    }

    void showDropDownIfPossible(AutoCompleteTextView view) {
        // showDropDown() method crashes when the given view is not attached to window
        // (which happens when we change locale)
        // isAttachedToWindow() method is available only from API19
        // therefore it is possible to get the same effect by checking whether
        // getWindowToken() returns null or not
        if (view.getWindowToken() != null) {
            view.showDropDown();
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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

    private void setListAdapter() {
        Integer startPointID = mDbHelper.destinationToIdMapping.get(mStartPoint.getText().toString());
        Integer endPointID = mDbHelper.destinationToIdMapping.get(mEndPoint.getText().toString());

        int[] routeIds;
        if (startPointID == null && endPointID == null) {
            routeIds = new int[0];
        } else if (startPointID == null) {
            routeIds = mDbHelper.reverseRoutes[endPointID].routeIds;
        } else if (endPointID == null) {
            routeIds = mDbHelper.reverseRoutes[startPointID].routeIds;
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
}