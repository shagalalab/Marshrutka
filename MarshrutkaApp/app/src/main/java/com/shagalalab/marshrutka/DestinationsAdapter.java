package com.shagalalab.marshrutka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.shagalalab.marshrutka.data.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by aziz on 7/11/15.
 */
public class DestinationsAdapter extends ArrayAdapter<Route> implements SectionIndexer {

    private LayoutInflater mInflater;
    private String[] sections;
    private LinkedHashMap<String, Integer> sectionIndexer;

    public DestinationsAdapter(Context context, int resource, Route[] objects) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);

        int len = objects.length;
        sectionIndexer = new LinkedHashMap<String, Integer>();
        for (int i=0; i<len; i++) {
            Route current = objects[i];
            if (current.isBus) {
                if (current.displayNo == 1) {
                    sectionIndexer.put("АВ-1", i);
                } else if (current.displayNo == 10) {
                    sectionIndexer.put("АВ-10", i);
                }
            } else {
                if (current.displayNo == 1 || current.displayNo % 10 == 0) {
                    sectionIndexer.put(Integer.toString(current.displayNo), i);
                }
            }
        }
        sections = new String[sectionIndexer.size()];
        sections = new ArrayList<String>(sectionIndexer.keySet()).toArray(sections);
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

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public int getPositionForSection(int section) {
        return sectionIndexer.get(sections[section]);
    }
}
