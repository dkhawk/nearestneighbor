package com.sphericalchickens.libraries.nearestneighbor;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import static com.sphericalchickens.libraries.nearestneighbor.LatLngBoundsSubject.assertThat;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dkhawk on 3/11/17.
 */
public class QuadTreeTest {
  @Test
  public void addLatLng() throws Exception {
    int level = 0;
    LatLngBounds.Builder builder = LatLngBounds.builder();
    builder.include(new LatLng(0, 0));
    LatLngBounds bounds = builder.build();
    QuadTree qt = new QuadTree(level, bounds);
    assertThat(qt.getBounds()).isEqualTo(bounds);
  }
}