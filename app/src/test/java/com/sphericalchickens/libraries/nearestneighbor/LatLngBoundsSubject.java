package com.sphericalchickens.libraries.nearestneighbor;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.common.truth.Truth;
import com.sphericalchickens.libraries.nearestneighbor.LatLngSubject;

import static com.google.common.truth.Truth.assertAbout;

public class LatLngBoundsSubject extends Subject<LatLngBoundsSubject, LatLngBounds> {

    // User-defined entry point
    public static LatLngBoundsSubject assertThat(@Nullable LatLngBounds latLngBounds) {
        return assertAbout(LAT_LNG_BOUNDS_SUBJECT_FACTORY).that(latLngBounds);
    }

    private static final SubjectFactory<LatLngBoundsSubject, LatLngBounds> LAT_LNG_BOUNDS_SUBJECT_FACTORY =
            new SubjectFactory<LatLngBoundsSubject, LatLngBounds>() {
                @Override
                public LatLngBoundsSubject getSubject(FailureStrategy failureStrategy,
                                                      @Nullable LatLngBounds target) {
                    return new LatLngBoundsSubject(failureStrategy, target);
                }
            };

    LatLngBoundsSubject(FailureStrategy failureStrategy, @Nullable LatLngBounds latLngBounds) {
        super(failureStrategy, latLngBounds);
    }

    public void matches(LatLngBounds other) {
        Truth.assertThat(getSubject()).isNotNull();
        Truth.assertThat(other).isNotNull();
        LatLngSubject.assertThat(getSubject().northeast).matchesLocation(other.northeast);
        LatLngSubject.assertThat(getSubject().southwest).matchesLocation(other.southwest);
    }
}
