package com.sphericalchickens.libraries.nearestneighbor;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dkhawk on 3/10/17.
 */

public class QuadTree {
  static final int MAX_DEPTH = 11;

  QuadTree children[] = new QuadTree[4];

  static final int NORTHEAST = 0;
  static final int SOUTHEAST = 1;
  static final int SOUTHWEST = 2;
  static final int NORTHWEST = 3;

  final int depth;
  final LatLngBounds bounds;
  final List<AnnotatedLatLng> locations = new ArrayList<>();

  public QuadTree(LatLng latLng, LatLng latLng1, int depth) {
    bounds = LatLngBounds.builder().include(latLng).include(latLng1).build();
    this.depth = depth;
  }

  public LatLngBounds getBounds() {
    return bounds;
  }

  public boolean contains(LatLng ll) {
    return bounds.contains(ll);
  }

  public boolean contains(AnnotatedLatLng ll) {
    return contains(ll.location);
  }

  @Nullable
  public QuadTree getNorthEast() {
    return children[NORTHEAST];
  }

  @Nullable
  public QuadTree getNorthWest() {
    return children[NORTHWEST];
  }

  @Nullable
  public QuadTree getSouthEast() {
    return children[SOUTHEAST];
  }

  @Nullable
  public QuadTree getSouthWest() {
    return children[SOUTHWEST];
  }

  public List<AnnotatedLatLng> getLocations() {
    return locations;
  }

  // Test only method...
  public List<AnnotatedLatLng> getAllLocations() {
    List<AnnotatedLatLng> result = new ArrayList<>();

    result.addAll(locations);

    if (children[0] != null) {
      for (QuadTree child : children) {
        result.addAll(child.getAllLocations());
      }
    }

    return result;
  }

  public AnnotatedLatLng findClosestLocation(LatLng testLocation) {
    if (!locations.isEmpty()) {
      return locations.get(0);
    }

    if (children[0] != null) {
      for (QuadTree child : children) {
        if (child.contains(testLocation)) {
          return child.findClosestLocation(testLocation);
        }
      }
    }

    return null;
  }

  static class AnnotatedLatLng {
    final LatLng location;
    private Object tag;

    AnnotatedLatLng(LatLng location) {
      this.location = location;
    }

    void setTag(Object tag) {
      this.tag = tag;
    }

    @Nullable
    Object getTag() {
      return tag;
    }
  }

  public QuadTree(LatLngBounds bounds) {
    this(bounds, 0);
  }

  private QuadTree(LatLngBounds bounds, int depth) {
    this.depth = depth;
    this.bounds = bounds;
  }

  void addLatLng(AnnotatedLatLng annotatedLatLng) {
    LatLng location = annotatedLatLng.location;
    if (!bounds.contains(location)) {
      throw new IllegalArgumentException("Location was out of bounds");
    }

    // Check if this node has children
    if (children[0] == null && (locations.isEmpty() || depth == MAX_DEPTH)) {
      locations.add(annotatedLatLng);
      return;
    }

    if (children[0] == null) {
      // Create all children
      int nextDepth = this.depth + 1;
      children[0] = new QuadTree(bounds.getCenter(), bounds.northeast, nextDepth);
      children[1] = new QuadTree(bounds.getCenter(),
          new LatLng(bounds.southwest.latitude, bounds.northeast.longitude), nextDepth);
      children[2] = new QuadTree(bounds.getCenter(), bounds.southwest, nextDepth);
      children[3] = new QuadTree(bounds.getCenter(),
          new LatLng(bounds.northeast.latitude, bounds.southwest.longitude), nextDepth);

      // Move the location(s) to the correct child
      for (AnnotatedLatLng latLng : locations) {
        for (QuadTree child : children) {
          if (child.contains(latLng)) {
            child.addLatLng(latLng);
          }
        }
      }

      locations.clear();
    }

    for (QuadTree child : children) {
      if (child.contains(location)) {
        child.addLatLng(annotatedLatLng);
      }
    }
  }
}
