package com.sphericalchickens.libraries.nearestneighbor;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.common.truth.Truth;

import static com.google.common.truth.Truth.assertAbout;

/**
 * Google Truth Subject for {@link LatLng} locations.
 */
public class LatLngSubject extends Subject<LatLngSubject, LatLng> {
    private static final double EPSILON = 0.0000000001;

    // User-defined entry point
    public static LatLngSubject assertThat(@Nullable LatLng latLng) {
        return assertAbout(LAT_LNG_SUBJECT_FACTORY).that(latLng);
    }

    private static final SubjectFactory<LatLngSubject, LatLng> LAT_LNG_SUBJECT_FACTORY =
            new SubjectFactory<LatLngSubject, LatLng>() {
                @Override
                public LatLngSubject getSubject(FailureStrategy failureStrategy,
                                                      @Nullable LatLng target) {
                    return new LatLngSubject(failureStrategy, target);
                }
            };

    LatLngSubject(FailureStrategy failureStrategy, @Nullable LatLng latLng) {
        super(failureStrategy, latLng);
    }

    public void matchesLocation(LatLng expected) {
      if (Math.abs(getSubject().latitude - expected.latitude) > EPSILON
        || Math.abs(getSubject().longitude - expected.longitude) > EPSILON) {
        failWithRawMessage(
            "<%s> and <%s> should have been finite values within <%s> of each other",
            formatLatLng(getSubject()),
            formatLatLng(expected),
            EPSILON);
      }
    }

  private String formatLatLng(LatLng latLng) {
    return String.format("%f, %f", latLng.latitude, latLng.longitude);
  }
}
