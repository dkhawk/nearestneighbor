package com.sphericalchickens.libraries.nearestneighbor;

import static com.google.common.truth.Truth.assertThat;
import static com.sphericalchickens.libraries.nearestneighbor.Segment.NUMBER_OF_STEPS;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SegmentTest {

  @Test
  public void distance_isCorrect() throws Exception {
    double lat = 0;
    double lng = 0;

    Segment segment = new Segment();

    InputStream stream = TestFileUtils.openAssetAsInputStream("track.csv");
    BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

    String line;
    int count = 0;
    do {
      line = br.readLine();
      if (line == null) {
        break;
      }
      String[] parts = line.split(",");
      if (parts.length >= 2) {
        lat = Double.parseDouble(parts[0]);
        lng = Double.parseDouble(parts[1]);
        LatLng location = new LatLng(lat, lng);
        segment.add(location);
      }
    } while (line != null);

    assertThat(segment.get(0).latitude).isWithin(0.0001).of(39.19613);
    assertThat(segment.get(0).longitude).isWithin(0.0001).of(-120.23565);

    double distance = SphericalUtil.computeDistanceBetween(segment.get(0), segment.get(1));
    assertThat(distance).isWithin(0.0001).of(9.798770);

    int nearestIndex = segment.nearestLocationBruteForce(new LatLng(39.19613, -120.23565));
    assertThat(nearestIndex).isEqualTo(0);
    nearestIndex = segment.nearestLocationIndexed(new LatLng(39.19613, -120.23565));
    assertThat(nearestIndex).isEqualTo(0);

    nearestIndex = segment.nearestLocationBruteForce(new LatLng(39.195908, -120.23592));
    assertThat(nearestIndex).isEqualTo(4);
    nearestIndex = segment.nearestLocationIndexed(new LatLng(39.195908, -120.23592));
    assertThat(nearestIndex).isEqualTo(4);

    nearestIndex = segment.nearestLocationBruteForce(new LatLng(39.195602, -120.236176));
    assertThat(nearestIndex).isEqualTo(9);
    nearestIndex = segment.nearestLocationIndexed(new LatLng(39.195602, -120.236176));
    assertThat(nearestIndex).isEqualTo(9);

    // A location not on the line
    nearestIndex = segment.nearestLocationBruteForce(new LatLng(39.196, -120.2358));
    assertThat(nearestIndex).isEqualTo(2);
    nearestIndex = segment.nearestLocationIndexed(new LatLng(39.196, -120.2358));
    assertThat(nearestIndex).isEqualTo(2);


    LatLngBounds bounds = segment.getBounds();

    LatLng location = new LatLng(39.196655, -120.31726);

    location = new LatLng(39.19613, -120.23565);

    location = new LatLng(39.09449, -120.6701);

    location = new LatLng(38.89586,-121.06744);

    nearestIndex = segment.nearestLocationIndexed(new LatLng(39.196, -120.2358));
    assertThat(nearestIndex).isEqualTo(2);

    location = new LatLng(39.09449,-120.6701);
    nearestIndex = segment.nearestLocationIndexed(location);
    int nearestBrute = segment.nearestLocationBruteForce(location);
    assertThat(nearestIndex).isEqualTo(nearestBrute);


    location = new LatLng(lat = 38.921534, lng = -120.254736);
    nearestIndex = segment.nearestLocationIndexed(location);
    nearestBrute = segment.nearestLocationBruteForce(location);

    System.out.println(String.format("%f, %f", lat, lng));

    LatLng wrong = new LatLng(39.18513, -120.25067);
    LatLng correct = new LatLng(39.133038, -120.45203);

    double d1 = SphericalUtil.computeDistanceBetween(location, wrong);
    double d2 = SphericalUtil.computeDistanceBetween(location, correct);

    System.out.println(String.format("Dist: %f, %f", d1, d2));

    assertThat(nearestIndex).isEqualTo(nearestBrute);

    Random generator = new Random(System.currentTimeMillis());

    for (int i = 0; i < 100; i++) {
      lat = generator.nextDouble() * segment.getLatRange() + segment.getBounds().southwest.latitude;
      lng = generator.nextDouble() * segment.getLngRange() + segment.getBounds().southwest.longitude;

      location = new LatLng(lat, lng);
      nearestIndex = segment.nearestLocationIndexed(location);
      nearestBrute = segment.nearestLocationBruteForce(location);
      if (nearestIndex != nearestBrute) {
        System.out.println(String.format("%f, %f", lat, lng));
      } else {
        System.out.println(String.format("Success: %f, %f", lat, lng));
      }
      assertThat(nearestIndex).isEqualTo(nearestBrute);
    }
  }
}