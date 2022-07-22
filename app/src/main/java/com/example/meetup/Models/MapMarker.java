package com.example.meetup.Models;
import com.parse.ParseClassName;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;


@ParseClassName("MapMarker")
public class MapMarker extends ParseObject {
    public static final String KEY_NAME = "name";
    public static final String KEY_LOCATION = "location";

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public void setLocation(ParseGeoPoint latLng) { put(KEY_LOCATION, latLng); }

    public ParseGeoPoint getLocation() { return getParseGeoPoint(KEY_LOCATION); }
}

