package com.sphericalchickens.libraries.nearestneighbor;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkhawk on 3/10/17.
 */

public class QuadTree {
  static final int MAX_DEPTH = 11;
  QuadTree northEast;
  QuadTree southEast;
  QuadTree southWest;
  QuadTree northWest;

  final int depth;
  final LatLngBounds bounds;
  final List<LatLngIdx> locations = new ArrayList<>();

  public LatLngBounds getBounds() {
    return bounds;
  }

  private static class LatLngIdx {
    final LatLng location;
    final int index;

    LatLngIdx(LatLng location, int index) {
      this.location = location;
      this.index = index;
    }
  }

  public QuadTree(int depth, LatLngBounds bounds) {
    this.depth = depth;
    this.bounds = bounds;
  }

  void addLatLng(LatLng location, int index) {
    if (!bounds.contains(location)) {
      throw new IllegalArgumentException("Location was out of bounds");
    }

    if (locations.isEmpty() || depth == MAX_DEPTH) {
      locations.add(new LatLngIdx(location, index));
      return;
    }

    boolean isNorth = location.latitude > bounds.getCenter().latitude;
    boolean isEast = location.longitude > bounds.getCenter().longitude;

    if (isNorth) {
      if (isEast) {
        if (northEast == null) {
          LatLngBounds.Builder b = new LatLngBounds.Builder();
          b.include(bounds.northeast);
          b.include(bounds.getCenter());
          northEast = new QuadTree(depth + 1, b.build());
        }
        northEast.addLatLng(location, index);
      }
    }
  }
}
