package com.shagalalab.marshrutka.data;

import java.util.ArrayList;

/**
 * Created by aziz on 7/10/15.
 */
public class Route {
    public int ID;
    public boolean isBus;
    public int displayNo;
    public String descriptionCyr, descriptionLat;
    public ArrayList<DestinationPoint> pathPoints;

    public Route() {
    }

    public Route(boolean isBus,
                 int displayNo,
                 String descriptionCyr,
                 String descriptionLat,
                 ArrayList<DestinationPoint> pathPoints) {

        this.isBus = isBus;
        this.displayNo = displayNo;
        this.descriptionCyr = descriptionCyr;
        this.descriptionLat = descriptionLat;
        this.pathPoints = pathPoints;
    }

    public String getDescription(boolean isInterfaceCyrillic) {
        return isInterfaceCyrillic ? descriptionCyr : descriptionLat;
    }
}
