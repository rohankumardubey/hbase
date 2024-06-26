/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.io.hfile;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ExtendedCell;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.io.encoding.EncodingState;
import org.apache.hadoop.hbase.io.encoding.HFileBlockDecodingContext;
import org.apache.hadoop.hbase.io.encoding.HFileBlockDefaultDecodingContext;
import org.apache.hadoop.hbase.io.encoding.HFileBlockDefaultEncodingContext;
import org.apache.hadoop.hbase.io.encoding.HFileBlockEncodingContext;
import org.apache.hadoop.hbase.io.encoding.NoneEncoder;
import org.apache.yetus.audience.InterfaceAudience;

/**
 * Does not perform any kind of encoding/decoding.
 */
@InterfaceAudience.Private
public class NoOpDataBlockEncoder implements HFileDataBlockEncoder {

  public static final NoOpDataBlockEncoder INSTANCE = new NoOpDataBlockEncoder();

  private static class NoneEncodingState extends EncodingState {
    NoneEncoder encoder = null;
  }

  /** Cannot be instantiated. Use {@link #INSTANCE} instead. */
  private NoOpDataBlockEncoder() {
  }

  @Override
  public void encode(ExtendedCell cell, HFileBlockEncodingContext encodingCtx, DataOutputStream out)
    throws IOException {
    NoneEncodingState state = (NoneEncodingState) encodingCtx.getEncodingState();
    NoneEncoder encoder = state.encoder;
    int size = encoder.write(cell);
    state.postCellEncode(size, size);
  }

  @Override
  public boolean useEncodedScanner() {
    return false;
  }

  @Override
  public void saveMetadata(HFile.Writer writer) {
  }

  @Override
  public DataBlockEncoding getDataBlockEncoding() {
    return DataBlockEncoding.NONE;
  }

  @Override
  public DataBlockEncoding getEffectiveEncodingInCache(boolean isCompaction) {
    return DataBlockEncoding.NONE;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public HFileBlockEncodingContext newDataBlockEncodingContext(Configuration conf,
    byte[] dummyHeader, HFileContext meta) {
    return new HFileBlockDefaultEncodingContext(conf, null, dummyHeader, meta);
  }

  @Override
  public HFileBlockDecodingContext newDataBlockDecodingContext(Configuration conf,
    HFileContext meta) {
    return new HFileBlockDefaultDecodingContext(conf, meta);
  }

  @Override
  public void startBlockEncoding(HFileBlockEncodingContext blkEncodingCtx, DataOutputStream out)
    throws IOException {
    if (blkEncodingCtx.getClass() != HFileBlockDefaultEncodingContext.class) {
      throw new IOException(this.getClass().getName() + " only accepts "
        + HFileBlockDefaultEncodingContext.class.getName() + " as the " + "encoding context.");
    }

    HFileBlockDefaultEncodingContext encodingCtx =
      (HFileBlockDefaultEncodingContext) blkEncodingCtx;
    encodingCtx.prepareEncoding(out);

    NoneEncoder encoder = new NoneEncoder(out, encodingCtx);
    NoneEncodingState state = new NoneEncodingState();
    state.encoder = encoder;
    blkEncodingCtx.setEncodingState(state);
  }

  @Override
  public void endBlockEncoding(HFileBlockEncodingContext encodingCtx, DataOutputStream out,
    byte[] uncompressedBytesWithHeader, BlockType blockType) throws IOException {
    encodingCtx.postEncoding(BlockType.DATA);
  }
}
