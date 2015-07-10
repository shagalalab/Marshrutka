package com.shagalalab.marshrutka;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.shagalalab.marshrutka.data.DestinationPoint;
import com.shagalalab.marshrutka.data.Route;

/**
 * Created by aziz on 7/10/15.
 */
public class QueryByNumberFragment extends Fragment {

    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_query_by_number, null);
        mListView = (ListView)view.findViewById(android.R.id.list);

        Route[] routes = generateMockRoutes();
        QueryByNumberAdapter adapter = new QueryByNumberAdapter(getActivity(), 0, routes);
        mListView.setAdapter(adapter);
        return view;
    }

    private Route[] generateMockRoutes() {

        DestinationPoint bazar = new DestinationPoint(1, "Орайлық базар");
        DestinationPoint goneqala = new DestinationPoint(2, "Гөне қала");
        DestinationPoint oraqbalga = new DestinationPoint(3, "Таслақ елаты");
        DestinationPoint samanbay = new DestinationPoint(4, "Саманбай елаты");
        DestinationPoint qumawil = new DestinationPoint(5, "Қум аўыл");
        DestinationPoint vokzal = new DestinationPoint(6, "Темир жол вокзалы");
        DestinationPoint qizketken = new DestinationPoint(7, "Қызкеткен елаты");
        DestinationPoint roddom = new DestinationPoint(8, "Орайлық туўыў үйи");
        DestinationPoint xojanawil = new DestinationPoint(9, "Хожан аўыл");
        DestinationPoint askeriyGarnizon = new DestinationPoint(10, "Әскерий гарнизон");
        DestinationPoint allaniyazqaxarman = new DestinationPoint(11, "Алланияз Қаҳарман елаты");
        DestinationPoint qoskol2 = new DestinationPoint(12, "Қоскөл-2");
        DestinationPoint qoskol3 = new DestinationPoint(13, "Қоскөл-3");
        DestinationPoint maykolxoz = new DestinationPoint(14, "Майколхоз");
        DestinationPoint kebirawil = new DestinationPoint(15, "Кебир аўыл");
        DestinationPoint teleoray = new DestinationPoint(16, "Телеорай");

        Route[] routes = new Route[] {
                new Route(true, 1, vokzal, oraqbalga, null),
                new Route(true, 7, oraqbalga, qizketken, null),
                new Route(true, 15, oraqbalga, bazar, vokzal),
                new Route(false, 1, vokzal, oraqbalga, null),
                new Route(false, 7, bazar, qumawil, null),
                new Route(false, 12, bazar, qizketken, null),
                new Route(false, 16, vokzal, allaniyazqaxarman, null),
                new Route(false, 41, bazar, vokzal, askeriyGarnizon),
                new Route(false, 57, bazar, oraqbalga, null),
                new Route(false, 59, bazar, askeriyGarnizon, null),
                new Route(false, 75, bazar, xojanawil, null),
                new Route(false, 88, bazar, vokzal, oraqbalga),
                new Route(false, 91, bazar, xojanawil, null)
        };
        return routes;
    }

    private class QueryByNumberAdapter extends ArrayAdapter<Route> {

        private LayoutInflater mInflater;

        public QueryByNumberAdapter(Context context, int resource, Route[] objects) {
            super(context, resource, objects);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.query_list_item, null);
                holder = new ViewHolder();
                holder.txtTransportNo = (TextView)convertView.findViewById(R.id.txt_transport_no);
                holder.pointA = (TextView)convertView.findViewById(R.id.txt_pointA);
                holder.pointB = (TextView)convertView.findViewById(R.id.txt_pointB);
                holder.pointC = (TextView)convertView.findViewById(R.id.txt_pointC);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            Route route = getItem(position);
            holder.txtTransportNo.setText((route.isBus ? "АВ-" : "") + route.displayNo);
            holder.pointA.setText(route.pointA.name);
            holder.pointB.setText(route.pointB.name);
            if (route.pointC == null) {
                holder.pointC.setVisibility(View.GONE);
            } else {
                holder.pointC.setVisibility(View.VISIBLE);
                holder.pointC.setText(route.pointC.name);
            }
            return convertView;
        }
    }

    private class ViewHolder {
        TextView txtTransportNo, pointA, pointB, pointC;
    }
}
