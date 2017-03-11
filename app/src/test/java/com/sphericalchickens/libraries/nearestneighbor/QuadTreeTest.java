package com.sphericalchickens.libraries.nearestneighbor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import org.junit.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.sphericalchickens.libraries.nearestneighbor.LatLngBoundsSubject.assertThat;
import static com.sphericalchickens.libraries.nearestneighbor.LatLngSubject.assertThat;
import static com.sphericalchickens.libraries.nearestneighbor.QuadTree.AnnotatedLatLng;

/**
 * Created by dkhawk on 3/11/17.
 */
public class QuadTreeTest {
  @Test
  public void addLatLng_singlePoint() throws Exception {
    LatLngBounds.Builder builder = LatLngBounds.builder();
    builder.include(new LatLng(1, 1));
    builder.include(new LatLng(-1, -1));
    LatLngBounds bounds = builder.build();
    QuadTree qt = new QuadTree(bounds);
    assertThat(qt.getBounds()).isEqualTo(bounds);

    assertThat(qt.getNorthEast()).isNull();
    assertThat(qt.getSouthEast()).isNull();
    assertThat(qt.getSouthWest()).isNull();
    assertThat(qt.getNorthWest()).isNull();

    LatLng testLocation = new LatLng(1, 1);
    assertThat(qt.findClosestLocation(testLocation)).isNull();

    // Northeast
    AnnotatedLatLng ll = new AnnotatedLatLng(new LatLng(0.5, 0.5));
    assertThat(qt.contains(ll)).isTrue();
    qt.addLatLng(ll);

    // First point should not split the quad
    assertThat(qt.getNorthEast()).isNull();
    assertThat(qt.getSouthEast()).isNull();
    assertThat(qt.getSouthWest()).isNull();
    assertThat(qt.getNorthWest()).isNull();

    AnnotatedLatLng closestLocation = qt.findClosestLocation(testLocation);
    assertThat(closestLocation).isNotNull();
    assertThat(closestLocation.location).matchesLocation(new LatLng(0.5, 0.5));

    // Southeast
    ll = new AnnotatedLatLng(new LatLng(-0.5, 0.5));
    // Adding a location outside of the bound should throw
    assertThat(qt.contains(ll)).isTrue();
    qt.addLatLng(ll);

    // Second point should split the quad
    assertThat(qt.getNorthEast().getBounds()).matches(getLatLngBounds(0, 0, 1, 1));
    assertThat(qt.getSouthEast().getBounds()).matches(getLatLngBounds(0, 0, -1, 1));
    assertThat(qt.getSouthWest().getBounds()).matches(getLatLngBounds(0, 0, -1, -1));
    assertThat(qt.getNorthWest().getBounds()).matches(getLatLngBounds(0, 0, 1, -1));

    assertThat(qt.getLocations()).hasSize(0);
    assertThat(qt.getNorthEast().getLocations()).hasSize(1);
    assertThat(qt.getSouthEast().getLocations()).hasSize(1);
    assertThat(qt.getSouthWest().getLocations()).hasSize(0);
    assertThat(qt.getNorthWest().getLocations()).hasSize(0);

    // Add another point
    ll = new AnnotatedLatLng(new LatLng(0.5, -0.5));
    assertThat(qt.contains(ll)).isTrue();
    qt.addLatLng(ll);

    // Same quads
    assertThat(qt.getNorthEast().getBounds()).matches(getLatLngBounds(0, 0, 1, 1));
    assertThat(qt.getSouthEast().getBounds()).matches(getLatLngBounds(0, 0, -1, 1));
    assertThat(qt.getSouthWest().getBounds()).matches(getLatLngBounds(0, 0, -1, -1));
    assertThat(qt.getNorthWest().getBounds()).matches(getLatLngBounds(0, 0, 1, -1));
    assertThat(qt.getLocations()).hasSize(0);

    assertThat(qt.getNorthEast().getLocations()).hasSize(1);
    assertThat(qt.getSouthEast().getLocations()).hasSize(1);
    assertThat(qt.getSouthWest().getLocations()).hasSize(0);
    assertThat(qt.getNorthWest().getLocations()).hasSize(1);

    AnnotatedLatLng bruteForceClosest = bruteForceClosest(qt, testLocation);
    assertThat(bruteForceClosest.location).matchesLocation(new LatLng(0.5, 0.5));

    closestLocation = qt.findClosestLocation(testLocation);
    assertThat(closestLocation).isNotNull();
    assertThat(closestLocation.location).matchesLocation(new LatLng(0.5, 0.5));

    // Try a trickier location
    addLatLng(qt, 0.01, 0.01);
    testLocation = new LatLng(-0.01, 0.01);
    bruteForceClosest = bruteForceClosest(qt, testLocation);
    assertThat(bruteForceClosest.location).matchesLocation(new LatLng(0.01, 0.01));

    closestLocation = qt.findClosestLocation(testLocation);
    assertThat(closestLocation).isNotNull();
    assertThat(closestLocation.location).matchesLocation(bruteForceClosest.location);
  }

  private void addLatLng(QuadTree quadTree, double lat, double lng) {
    quadTree.addLatLng(new AnnotatedLatLng(new LatLng(lat, lng)));
  }

  @Nullable
  private AnnotatedLatLng bruteForceClosest(QuadTree qt, LatLng testLocation) {
    List<AnnotatedLatLng> allLocations = qt.getAllLocations();
    AnnotatedLatLng closestLocation = null;

    double closestDistance = Double.MAX_VALUE;
    for (AnnotatedLatLng annotatedLatLng : allLocations) {
      double dist = SphericalUtil.computeDistanceBetween(testLocation, annotatedLatLng.location);
      if (dist < closestDistance) {
        closestDistance = dist;
        closestLocation = annotatedLatLng;
      }
    }
    return closestLocation;
  }

  @NonNull
  private LatLngBounds getLatLngBounds(double north, double east, double south, double west) {
    return LatLngBounds.builder().include(new LatLng(north, east)).include(new LatLng(south, west))
        .build();
  }
}