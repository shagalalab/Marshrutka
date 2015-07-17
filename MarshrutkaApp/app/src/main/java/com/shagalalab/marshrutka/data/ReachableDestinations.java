package com.shagalalab.marshrutka.data;

/**
 * Created by aziz on 7/17/15.
 */
public class ReachableDestinations {
    public int destinationID;
    public int[] reachableDestinationIds;

    public ReachableDestinations(int destinationID, String destinationIdsAsStr) {
        this.destinationID = destinationID;
        String[] chunks = destinationIdsAsStr.split(",");
        int len = chunks.length;
        reachableDestinationIds = new int[len];
        for (int i=0; i<len; i++) {
            reachableDestinationIds[i] = Integer.parseInt(chunks[i]);
        }
    }
}
