package com.sphericalchickens.libraries.nearestneighbor;

import static com.google.common.truth.Truth.assertThat;

import android.support.annotation.NonNull;
import java.io.InputStream;

/**
 * Created by dkhawk on 3/9/17.
 */

public class TestFileUtils {
  @NonNull
  static InputStream openAssetAsInputStream(String testDataFileName) {
    InputStream stream;
    stream = ClassLoader.getSystemResourceAsStream(testDataFileName);
    assertThat(stream).isNotNull();
    return stream;
  }
}
