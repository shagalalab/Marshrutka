package com.shagalalab.marshrutka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.shagalalab.marshrutka.data.Route;

/**
 * Created by aziz on 7/11/15.
 */
public class DestinationsAdapter extends ArrayAdapter<Route> {

    private LayoutInflater mInflater;

    public DestinationsAdapter(Context context, int resource, Route[] objects) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.query_list_item, null);
            holder = new ViewHolder();
            holder.txtTransportNo = (TextView) convertView.findViewById(R.id.txt_transport_no);
            holder.pointA = (TextView) convertView.findViewById(R.id.txt_pointA);
            holder.pointB = (TextView) convertView.findViewById(R.id.txt_pointB);
            holder.pointC = (TextView) convertView.findViewById(R.id.txt_pointC);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
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

    private class ViewHolder {
        TextView txtTransportNo, pointA, pointB, pointC;
    }
}
