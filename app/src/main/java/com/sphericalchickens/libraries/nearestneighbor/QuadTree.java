package com.sphericalchickens.libraries.nearestneighbor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

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
  private final int maxDepth;

  public QuadTree(LatLng latLng, LatLng latLng1, int depth, int maxDepth) {
    bounds = LatLngBounds.builder().include(latLng).include(latLng1).build();
    this.depth = depth;
    this.maxDepth = maxDepth;
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

  @Nullable
  public Pair<AnnotatedLatLng, Double> findClosestLocation(LatLng testLocation) {
    if (!locations.isEmpty()) {
      return checkAllLocations(testLocation);
    }

    Pair<AnnotatedLatLng, Double> closestPair = null;

    if (children[0] != null) {
      int closestDirection = -1;
      for (QuadTree child : children) {
        if (child.contains(testLocation)) {
          closestPair = child.findClosestLocation(testLocation);
          closestDirection++;
        }
      }

      if (closestPair != null) {
        // Check the distance to the nearest border...
        // TODO(dkhawk): this is approximately correct...
        double distNorth = SphericalUtil.computeDistanceBetween(testLocation,
            new LatLng(testLocation.latitude, bounds.northeast.longitude));

        double distSouth = SphericalUtil.computeDistanceBetween(testLocation,
            new LatLng(testLocation.latitude, bounds.southwest.longitude));

        double distEast = SphericalUtil.computeDistanceBetween(testLocation,
            new LatLng(bounds.northeast.latitude, testLocation.longitude));

        double distWest = SphericalUtil.computeDistanceBetween(testLocation,
            new LatLng(bounds.southwest.latitude, testLocation.longitude));

        // For each of the distances, if any are less than the closest distance found so far, check that quad
        if (closestDirection == NORTHEAST) {
          if (distSouth <= closestPair.second) {
            Pair<AnnotatedLatLng, Double> sep = checkLocations(testLocation,
                children[SOUTHEAST].getAllLocations());
            if (sep != null && (sep.second < closestPair.second)) {
              closestPair = sep;
            }
          }

          if (distEast <= closestPair.second) {
            Pair<AnnotatedLatLng, Double> nwp = checkLocations(testLocation,
                children[NORTHWEST].getAllLocations());
            if (nwp != null && (nwp.second < closestPair.second)) {
              closestPair = nwp;
            }
          }

          if (distEast <= closestPair.second && distSouth <= closestPair.second) {
            Pair<AnnotatedLatLng, Double> swp = checkLocations(testLocation,
                children[SOUTHWEST].getAllLocations());
            if (swp != null && (swp.second < closestPair.second)) {
              closestPair = swp;
            }
          }
        }  // NORTHEAST

        if (closestDirection == SOUTHEAST) {
          // TODO
        }  // SOUTHEAST

      }
    }
    return closestPair;
  }

  @Nullable
  private Pair<AnnotatedLatLng, Double> checkAllLocations(LatLng testLocation) {
    return checkLocations(testLocation, getAllLocations());
  }

  @Nullable
  private Pair<AnnotatedLatLng, Double> checkLocations(LatLng testLocation,
                                                       List<AnnotatedLatLng> allLocations) {
    if (allLocations.isEmpty()) {
      return null;
    }

    AnnotatedLatLng closestLocation = null;
    double closestDistance = Double.MAX_VALUE;

    for (AnnotatedLatLng annotatedLatLng : allLocations) {
      double dist = SphericalUtil.computeDistanceBetween(testLocation, annotatedLatLng.location);
      if (dist < closestDistance) {
        closestDistance = dist;
        closestLocation = annotatedLatLng;
      }
    }
    return new Pair<>(closestLocation, closestDistance);
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

  public QuadTree(LatLngBounds bounds, int maxDepth) {
    this(bounds, 0, maxDepth);
  }

  private QuadTree(LatLngBounds bounds, int depth, int maxDepth) {
    this.bounds = bounds;
    this.depth = depth;
    this.maxDepth = maxDepth;
  }

  void addLatLng(AnnotatedLatLng annotatedLatLng) {
    LatLng location = annotatedLatLng.location;
    if (!bounds.contains(location)) {
      throw new IllegalArgumentException("Location was out of bounds");
    }

    // Check if this node has children
    if (children[0] == null && (locations.isEmpty() || depth >= maxDepth)) {
      locations.add(annotatedLatLng);
      return;
    }

    if (children[0] == null) {
      // Create all children
      int nextDepth = this.depth + 1;
      children[0] = new QuadTree(bounds.getCenter(), bounds.northeast, nextDepth, maxDepth);
      children[1] = new QuadTree(bounds.getCenter(),
          new LatLng(bounds.southwest.latitude, bounds.northeast.longitude), nextDepth, maxDepth);
      children[2] = new QuadTree(bounds.getCenter(), bounds.southwest, nextDepth, maxDepth);
      children[3] = new QuadTree(bounds.getCenter(),
          new LatLng(bounds.northeast.latitude, bounds.southwest.longitude), nextDepth, maxDepth);

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
        break;
      }
    }
  }
}
