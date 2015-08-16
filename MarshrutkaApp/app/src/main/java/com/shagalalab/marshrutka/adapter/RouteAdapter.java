package com.shagalalab.marshrutka.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.shagalalab.marshrutka.R;
import com.shagalalab.marshrutka.data.Route;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by aziz on 7/11/15.
 */
public class RouteAdapter extends ArrayAdapter<Route> implements SectionIndexer {

    private LayoutInflater mInflater;
    private String[] sections;
    private LinkedHashMap<String, Integer> sectionIndexer;
    private String mBusShortenedLabel;
    private boolean mIsInterfaceCyrillic;

    public RouteAdapter(Context context, int resource, Route[] objects, boolean isInterfaceCyrillic) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mIsInterfaceCyrillic = isInterfaceCyrillic;

        int len = objects.length;
        sectionIndexer = new LinkedHashMap<String, Integer>();
        mBusShortenedLabel = context.getString(R.string.bus_shortened_label);
        for (int i=0; i<len; i++) {
            Route current = objects[i];
            if (current.isBus) {
                if (current.displayNo == 1) {
                    sectionIndexer.put(mBusShortenedLabel, i);
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
            holder.points = new TextView[5];
            holder.points[0] = (TextView) convertView.findViewById(R.id.txt_point1);
            holder.points[1] = (TextView) convertView.findViewById(R.id.txt_point2);
            holder.points[2] = (TextView) convertView.findViewById(R.id.txt_point3);
            holder.points[3] = (TextView) convertView.findViewById(R.id.txt_point4);
            holder.points[4] = (TextView) convertView.findViewById(R.id.txt_point5);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Route route = getItem(position);
        holder.txtTransportNo.setText((route.isBus ? mBusShortenedLabel + "-" : "") + route.displayNo);
        String description = route.getDescription(mIsInterfaceCyrillic);
        String[] descriptionChunks = description.split(" - ");
        int chunksLength = descriptionChunks.length;
        for (int i=0; i<chunksLength; i++) {
            holder.points[i].setVisibility(View.VISIBLE);
            holder.points[i].setText(descriptionChunks[i]);
        }
        for (int i=chunksLength; i<5; i++) {
            holder.points[i].setVisibility(View.GONE);
        }
        return convertView;
    }

    private class ViewHolder {
        TextView txtTransportNo;
        TextView[] points;
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
