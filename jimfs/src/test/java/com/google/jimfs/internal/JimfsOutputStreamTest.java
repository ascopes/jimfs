/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jimfs.internal;

import static com.google.jimfs.testing.TestUtils.bytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.IOException;

/**
 * Tests for {@link JimfsOutputStream}.
 *
 * @author Colin Decker
 */
public class JimfsOutputStreamTest {

  @Test
  public void testWrite_singleByte() throws IOException {
    JimfsOutputStream out = newOutputStream(false);
    out.write(1);
    out.write(2);
    out.write(3);
    assertStoreContains(out, 1, 2, 3);
  }

  @Test
  public void testWrite_wholeArray() throws IOException {
    JimfsOutputStream out = newOutputStream(false);
    out.write(new byte[]{1, 2, 3, 4});
    assertStoreContains(out, 1, 2, 3, 4);
  }

  @Test
  public void testWrite_partialArray() throws IOException {
    JimfsOutputStream out = newOutputStream(false);
    out.write(new byte[]{1, 2, 3, 4, 5, 6}, 1, 3);
    assertStoreContains(out, 2, 3, 4);
  }

  @Test
  public void testWrite_partialArray_invalidInput() throws IOException {
    JimfsOutputStream out = newOutputStream(false);

    try {
      out.write(new byte[3], -1, 1);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }

    try {
      out.write(new byte[3], 0, 4);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }

    try {
      out.write(new byte[3], 1, 3);
      fail();
    } catch (IndexOutOfBoundsException expected) {
    }
  }

  @Test
  public void testWrite_singleByte_appendMode() throws IOException {
    JimfsOutputStream out = newOutputStream(true);
    addBytesToStore(out, 9, 8, 7);
    out.write(1);
    out.write(2);
    out.write(3);
    assertStoreContains(out, 9, 8, 7, 1, 2, 3);
  }

  @Test
  public void testWrite_wholeArray_appendMode() throws IOException {
    JimfsOutputStream out = newOutputStream(true);
    addBytesToStore(out, 9, 8, 7);
    out.write(new byte[]{1, 2, 3, 4});
    assertStoreContains(out, 9, 8, 7, 1, 2, 3, 4);
  }

  @Test
  public void testWrite_partialArray_appendMode() throws IOException {
    JimfsOutputStream out = newOutputStream(true);
    addBytesToStore(out, 9, 8, 7);
    out.write(new byte[]{1, 2, 3, 4, 5, 6}, 1, 3);
    assertStoreContains(out, 9, 8, 7, 2, 3, 4);
  }

  @Test
  public void testWrite_singleByte_overwriting() throws IOException {
    JimfsOutputStream out = newOutputStream(false);
    addBytesToStore(out, 9, 8, 7, 6, 5, 4, 3);
    out.write(1);
    out.write(2);
    out.write(3);
    assertStoreContains(out, 1, 2, 3, 6, 5, 4, 3);
  }

  @Test
  public void testWrite_wholeArray_overwriting() throws IOException {
    JimfsOutputStream out = newOutputStream(false);
    addBytesToStore(out, 9, 8, 7, 6, 5, 4, 3);
    out.write(new byte[]{1, 2, 3, 4});
    assertStoreContains(out, 1, 2, 3, 4, 5, 4, 3);
  }

  @Test
  public void testWrite_partialArray_overwriting() throws IOException {
    JimfsOutputStream out = newOutputStream(false);
    addBytesToStore(out, 9, 8, 7, 6, 5, 4, 3);
    out.write(new byte[]{1, 2, 3, 4, 5, 6}, 1, 3);
    assertStoreContains(out, 2, 3, 4, 6, 5, 4, 3);
  }

  @Test
  public void testClosedOutputStream_throwsException() throws IOException {
    JimfsOutputStream out = newOutputStream(false);
    out.close();

    try {
      out.write(1);
      fail();
    } catch (IOException expected) {
    }

    try {
      out.write(new byte[3]);
      fail();
    } catch (IOException expected) {
    }

    try {
      out.write(new byte[10], 1, 3);
      fail();
    } catch (IOException expected) {
    }

    try {
      out.flush();
      fail();
    } catch (IOException expected) {
    }

    out.close(); // does nothing
  }

  private static JimfsOutputStream newOutputStream(boolean append) {
    File file = new File(1, new StubByteStore(0));
    return new JimfsOutputStream(file, append);
  }

  private static void addBytesToStore(JimfsOutputStream out, int... bytes) {
    ByteStore store = out.file.bytes();
    long pos = store.currentSize();
    for (int b : bytes) {
      store.write(pos++, (byte) b);
    }
  }

  private static void assertStoreContains(JimfsOutputStream out, int... bytes) {
    byte[] actualBytes = new byte[bytes.length];
    out.file.bytes().read(0, actualBytes);
    assertArrayEquals(bytes(bytes), actualBytes);
  }
}
