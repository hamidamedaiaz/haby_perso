package com.heavyclient.utils;

import com.soap.generated.*;
import jakarta.xml.bind.JAXBElement;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

public class RouteUtils {

    public static List<GeoPosition> extractRoute(ItineraryData data) {
        List<GeoPosition> list = new ArrayList<>();

        Geometry geometry = data.getGeometry() != null ? data.getGeometry().getValue() : null;
        if (geometry == null) return list;

        JAXBElement<ArrayOfArrayOfdouble> coordsElement = geometry.getCoordinates();
        if (coordsElement == null || coordsElement.getValue() == null) return list;

        ArrayOfArrayOfdouble arrays = coordsElement.getValue();
        List<ArrayOfdouble> arrayList = arrays.getArrayOfdouble();

        for (ArrayOfdouble array : arrayList) {
            List<Double> doubles = array.getDouble();
            if (doubles.size() >= 2) {
                list.add(new GeoPosition(doubles.get(0), doubles.get(1)));
            }
        }

        return list;
    }
}
