package com.sphericalchickens.libraries.nearestneighbor;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkhawk on 3/9/17.
 */
public class Segment {

  public static final int NUMBER_OF_STEPS = 20;
  List<LatLng> locations = new ArrayList<>();
  private LatLng center;
  private LatLngBounds bounds;
  private double yRange;
  private double xRange;
  private double xStep;
  private double yStep;
  private double latRange;
  private double lngRange;
  private double latStep;
  private double lngStep;

  List<List<LatLngIdx>> latIndexedLocations;
  List<List<LatLngIdx>> lngIndexedLocations;

  public double getLatRange() {
    return latRange;
  }

  public double getLngRange() {
    return lngRange;
  }

  static class LatLngIdx {
    final LatLng location;
    final int index;

    LatLngIdx(LatLng location, int index) {
      this.location = location;
      this.index = index;
    }
  }

  public void add(LatLng location) {
    locations.add(location);
  }

  public LatLng get(int i) {
    return locations.get(i);
  }

  public int nearestLocationBruteForce(LatLng location) {
    double closestDistance = Double.MAX_VALUE;
    int closestLocationIndex = -1;

    for (int i = 0; i < locations.size(); i++) {
      double dist = SphericalUtil.computeDistanceBetween(location, locations.get(i));
      if (dist < closestDistance) {
        closestDistance = dist;
        closestLocationIndex = i;
      }
    }

    return closestLocationIndex;
  }

  public void buildIndex() {
    LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
    for (LatLng location : locations) {
      boundsBuilder.include(location);
    }

    bounds = boundsBuilder.build();

    latRange = bounds.northeast.latitude - bounds.southwest.latitude;
    lngRange = bounds.northeast.longitude - bounds.southwest.longitude;

    latStep = latRange / NUMBER_OF_STEPS;
    lngStep = lngRange / NUMBER_OF_STEPS;

    latIndexedLocations = new ArrayList<>(NUMBER_OF_STEPS);
    lngIndexedLocations = new ArrayList<>(NUMBER_OF_STEPS);

    for (int i = 0; i < NUMBER_OF_STEPS; i++) {
      latIndexedLocations.add(new ArrayList<LatLngIdx>());
      lngIndexedLocations.add(new ArrayList<LatLngIdx>());
    }

    for (int i = 0; i < locations.size(); ++i) {
      LatLng location = locations.get(i);
      int latIdx = getLatIndex(location);
      int lngIdx = getLngIndex(location);
      latIndexedLocations.get(latIdx).add(new LatLngIdx(location, i));
      lngIndexedLocations.get(lngIdx).add(new LatLngIdx(location, i));
    }
  }

  int getLatIndex(LatLng location) {
    double lat = location.latitude - bounds.southwest.latitude;
    return Math.max(0, Math.min((int) Math.floor((lat / latRange) * NUMBER_OF_STEPS), NUMBER_OF_STEPS - 1));
  }

  int getLngIndex(LatLng location) {
    double lng = location.longitude - bounds.southwest.longitude;
    return Math.max(0, Math.min((int) Math.floor((lng / lngRange) * NUMBER_OF_STEPS), NUMBER_OF_STEPS - 1));
  }

  public int nearestLocationIndexed(LatLng location) {
    int latIdx = getLatIndex(location);
    int lngIdx = getLngIndex(location);

    // Check all nearest neighbors
    int minLat = Math.max(0, latIdx - 1);
    int minLng = Math.max(0, lngIdx - 1);

    int maxLat = Math.min(NUMBER_OF_STEPS - 1, latIdx + 1);
    int maxLng = Math.min(NUMBER_OF_STEPS - 1, lngIdx + 1);

    double closestDistance = Double.MAX_VALUE;
    int closestLocationIndex = -1;

    int latError = Integer.MAX_VALUE;
    int lngError = Integer.MAX_VALUE;

    for (int i = minLat; i <= maxLat; ++i) {
      for (LatLngIdx latLngIdx : latIndexedLocations.get(i)) {
        double dist = SphericalUtil.computeDistanceBetween(location, latLngIdx.location);
        if (dist < closestDistance) {
          closestDistance = dist;
          closestLocationIndex = latLngIdx.index;
          lngError = lngIdx - getLngIndex(latLngIdx.location);
        }
      }
    }

    for (int i = minLng; i <= maxLng; ++i) {
      for (LatLngIdx latLngIdx : lngIndexedLocations.get(i)) {
        double dist = SphericalUtil.computeDistanceBetween(location, latLngIdx.location);
        if (dist < closestDistance) {
          closestDistance = dist;
          closestLocationIndex = latLngIdx.index;
          latError = lngIdx - getLatIndex(latLngIdx.location);
        }
      }
    }

    System.out.println("closest distance " + closestDistance);
    System.out.println("closest index " + closestLocationIndex);
    System.out.println("latError " + latError);
    System.out.println("lngError " + lngError);

    return closestLocationIndex;
  }

  public double getXRange() {
    return xRange;
  }

  public double getYRange() {
    return yRange;
  }

  public LatLngBounds getBounds() {
    return bounds;
  }
}
