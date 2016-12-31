package com.shagalalab.marshrutka.data;

/**
 * Created by aziz on 7/10/15.
 */
public class ReverseRoute {
    public int destinationID;
    public int[] routeIds;

    public ReverseRoute(int destinationID, String routeIdsAsStr) {
        this.destinationID = destinationID;
        String[] chunks = routeIdsAsStr.split(",");
        int len = chunks.length;
        routeIds = new int[len];
        for (int i = 0; i < len; i++) {
            routeIds[i] = Integer.parseInt(chunks[i]);
        }
    }
}
