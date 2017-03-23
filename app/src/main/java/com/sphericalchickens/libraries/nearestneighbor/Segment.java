package com.sphericalchickens.libraries.nearestneighbor;

import android.support.v4.util.Pair;

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
  private LatLngBounds bounds;
  private QuadTree quadTree;

  public double getLatRange() {
    LatLngBounds bounds = quadTree.getBounds();
    return bounds.northeast.latitude - bounds.southwest.latitude;
  }

  public double getLngRange() {
    LatLngBounds bounds = quadTree.getBounds();
    return bounds.northeast.longitude - bounds.southwest.longitude;
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

    quadTree = new QuadTree(bounds, QuadTree.MAX_DEPTH);
    for (int i = 0; i < locations.size(); ++i) {
      LatLng location = locations.get(i);
      QuadTree.AnnotatedLatLng annotatedLatLng = new QuadTree.AnnotatedLatLng(location);
      annotatedLatLng.setTag(i);
      quadTree.addLatLng(annotatedLatLng);
    }
  }

  public int nearestLocationIndexed(LatLng location) {
    if (quadTree == null) {
      buildIndex();
    }
    Pair<QuadTree.AnnotatedLatLng, Double> p = quadTree.findClosestLocation(location);
    if (p == null) {
      return -1;
    }
    return (Integer) p.first.getTag();
  }

  public LatLngBounds getBounds() {
    return bounds;
  }
}
