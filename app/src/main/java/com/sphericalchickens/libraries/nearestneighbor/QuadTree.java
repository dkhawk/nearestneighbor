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
    // If this is a leave quad, find the closest location regardless of the containment!
    if (!locations.isEmpty()) {
      return checkAllLocations(testLocation);
    }

    Pair<AnnotatedLatLng, Double> closestPair = null;

    // For each child, if the child contains the testLocation, then have that child check the point
    int closestDirection = -1;
    if (children[0] != null) {
      for (int i = 0; i < children.length; ++i) {
        if (children[i].contains(testLocation)) {
          closestPair = children[i].findClosestLocation(testLocation);
          closestDirection = i;
        }
      }
    }

    // Now we know the quad with the closest location found so far.
    // See if any of the other quads could possibly contain a closer location
    if (closestPair != null) {
      boolean[] checkedQuads = new boolean[4];
      checkedQuads[closestDirection] = true;

      // Check the distance to the nearest border...
      // TODO(dkhawk): this is approximately correct...
      LatLngBounds childBounds = children[closestDirection].getBounds();

      double distNorth = SphericalUtil.computeDistanceBetween(testLocation,
          new LatLng(childBounds.northeast.latitude, testLocation.longitude));

      double distSouth = SphericalUtil.computeDistanceBetween(testLocation,
          new LatLng(childBounds.southwest.latitude, testLocation.longitude));

      double distEast = SphericalUtil.computeDistanceBetween(testLocation,
          new LatLng(testLocation.latitude, childBounds.northeast.longitude));

      double distWest = SphericalUtil.computeDistanceBetween(testLocation,
          new LatLng(testLocation.latitude, childBounds.southwest.longitude));

      // Must check both quads north if the distance to the north is closer
      if (distNorth <= closestPair.second
          && (closestDirection == SOUTHEAST || closestDirection == SOUTHWEST)) {
        if (!checkedQuads[NORTHWEST]) {
          checkedQuads[NORTHWEST] = true;
          Pair<AnnotatedLatLng, Double> p = children[NORTHWEST].findClosestLocation(testLocation);
          if ((p != null) && (p.second < closestPair.second)) {
            closestPair = p;
          }
        }

        if (!checkedQuads[NORTHEAST]) {
          checkedQuads[NORTHEAST] = true;
          Pair<AnnotatedLatLng, Double> p = children[NORTHEAST].findClosestLocation(testLocation);
          if ((p != null) && (p.second < closestPair.second)) {
            closestPair = p;
          }
        }
      }

      // Must check both quads south if the distance to the south is closer
      if (distSouth <= closestPair.second
          && (closestDirection == NORTHEAST || closestDirection == NORTHWEST)) {
        if (!checkedQuads[SOUTHWEST]) {
          checkedQuads[SOUTHWEST] = true;
          Pair<AnnotatedLatLng, Double> p = children[SOUTHWEST].findClosestLocation(testLocation);
          if ((p != null) && (p.second < closestPair.second)) {
            closestPair = p;
          }
        }

        if (!checkedQuads[SOUTHEAST]) {
          checkedQuads[SOUTHEAST] = true;
          Pair<AnnotatedLatLng, Double> p = children[SOUTHEAST].findClosestLocation(testLocation);
          if ((p != null) && (p.second < closestPair.second)) {
            closestPair = p;
          }
        }
      }

      // Must check both quads east if the distance to the east is closer
      if (distEast <= closestPair.second
          && (closestDirection == NORTHWEST || closestDirection == SOUTHWEST)) {
        if (!checkedQuads[NORTHEAST]) {
          checkedQuads[NORTHEAST] = true;
          Pair<AnnotatedLatLng, Double> p = children[NORTHEAST].findClosestLocation(testLocation);
          if ((p != null) && (p.second < closestPair.second)) {
            closestPair = p;
          }
        }

        if (!checkedQuads[SOUTHEAST]) {
          checkedQuads[SOUTHEAST] = true;
          Pair<AnnotatedLatLng, Double> p = children[SOUTHEAST].findClosestLocation(testLocation);
          if ((p != null) && (p.second < closestPair.second)) {
            closestPair = p;
          }
        }
      }

      // Must check both quads west if the distance to the west is closer
      if (distWest <= closestPair.second
          && (closestDirection == NORTHEAST || closestDirection == SOUTHEAST)) {
        if (!checkedQuads[NORTHWEST]) {
          checkedQuads[NORTHWEST] = true;
          Pair<AnnotatedLatLng, Double> p = children[NORTHWEST].findClosestLocation(testLocation);
          if ((p != null) && (p.second < closestPair.second)) {
            closestPair = p;
          }
        }

        if (!checkedQuads[SOUTHWEST]) {
          checkedQuads[SOUTHWEST] = true;
          Pair<AnnotatedLatLng, Double> p = children[SOUTHWEST].findClosestLocation(testLocation);
          if ((p != null) && (p.second < closestPair.second)) {
            closestPair = p;
          }
        }
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
