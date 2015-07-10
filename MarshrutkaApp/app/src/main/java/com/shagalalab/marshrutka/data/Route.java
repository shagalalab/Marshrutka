package com.shagalalab.marshrutka.data;

/**
 * Created by aziz on 7/10/15.
 */
public class Route {
    public int ID;
    public boolean isBus;
    public int displayNo;
    public DestinationPoint pointA;
    public DestinationPoint pointB;
    public DestinationPoint pointC;

    public Route() {}

    public Route(boolean isBus,
                 int displayNo,
                 DestinationPoint pointA,
                 DestinationPoint pointB,
                 DestinationPoint pointC) {

        this.isBus = isBus;
        this.displayNo = displayNo;
        this.pointA = pointA;
        this.pointB = pointB;
        this.pointC = pointC;
    }
}
