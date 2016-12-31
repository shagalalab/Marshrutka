package com.shagalalab.marshrutka.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.shagalalab.marshrutka.R;
import com.shagalalab.marshrutka.adapter.RouteAdapter;
import com.shagalalab.marshrutka.App;
import com.shagalalab.marshrutka.activity.DetailActivity;
import com.shagalalab.marshrutka.db.DbHelper;

/**
 * Created by aziz on 7/10/15.
 */
public class QueryByNumberFragment extends Fragment {

    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_query_by_number, null);
        mListView = (ListView) view.findViewById(android.R.id.list);

        DbHelper dbHelper = DbHelper.getInstance(getActivity());

        boolean isInterfaceCyrillic = ((App) getActivity().getApplicationContext()).isCurrentLocaleCyrillic();
        RouteAdapter adapter = new RouteAdapter(getActivity(), 0, dbHelper.routes, isInterfaceCyrillic);
        mListView.setAdapter(adapter);
        mListView.setFastScrollEnabled(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(DetailActivity.ROUTE_ID, position);
                startActivity(intent);
            }
        });
        return view;
    }
}
