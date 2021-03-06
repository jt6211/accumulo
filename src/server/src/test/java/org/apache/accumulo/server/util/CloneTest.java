/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.accumulo.server.util;

import java.util.HashSet;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.KeyExtent;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.util.ColumnFQ;
import org.apache.accumulo.server.util.MetadataTable;
import org.apache.accumulo.server.util.TabletIterator;
import org.apache.hadoop.io.Text;

public class CloneTest extends TestCase {
  
  public void testNoFiles() throws Exception {
    MockInstance mi = new MockInstance();
    Connector conn = mi.getConnector("", "");
    
    KeyExtent ke = new KeyExtent(new Text("0"), null, null);
    Mutation mut = ke.getPrevRowUpdateMutation();
    
    ColumnFQ.put(mut, Constants.METADATA_TIME_COLUMN, new Value("M0".getBytes()));
    ColumnFQ.put(mut, Constants.METADATA_DIRECTORY_COLUMN, new Value("/default_tablet".getBytes()));
    
    BatchWriter bw1 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    bw1.addMutation(mut);
    
    bw1.close();
    
    BatchWriter bw2 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    MetadataTable.initializeClone("0", "1", conn, bw2);
    
    int rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(0, rc);
    
    // scan tables metadata entries and confirm the same
    
  }
  
  public void testFilesChange() throws Exception {
    MockInstance mi = new MockInstance();
    Connector conn = mi.getConnector("", "");
    
    KeyExtent ke = new KeyExtent(new Text("0"), null, null);
    Mutation mut = ke.getPrevRowUpdateMutation();
    
    ColumnFQ.put(mut, Constants.METADATA_TIME_COLUMN, new Value("M0".getBytes()));
    ColumnFQ.put(mut, Constants.METADATA_DIRECTORY_COLUMN, new Value("/default_tablet".getBytes()));
    mut.put(Constants.METADATA_DATAFILE_COLUMN_FAMILY.toString(), "/default_tablet/0_0.rf", "1,200");
    
    BatchWriter bw1 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    bw1.addMutation(mut);
    
    bw1.flush();
    
    BatchWriter bw2 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    MetadataTable.initializeClone("0", "1", conn, bw2);
    
    Mutation mut2 = new Mutation(ke.getMetadataEntry());
    mut2.putDelete(Constants.METADATA_DATAFILE_COLUMN_FAMILY.toString(), "/default_tablet/0_0.rf");
    mut2.put(Constants.METADATA_DATAFILE_COLUMN_FAMILY.toString(), "/default_tablet/1_0.rf", "2,300");
    
    bw1.addMutation(mut2);
    bw1.flush();
    
    int rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(1, rc);
    
    rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(0, rc);
    
    Scanner scanner = conn.createScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS);
    scanner.setRange(new KeyExtent(new Text("1"), null, null).toMetadataRange());
    
    HashSet<String> files = new HashSet<String>();
    
    for (Entry<Key,Value> entry : scanner) {
      if (entry.getKey().getColumnFamily().equals(Constants.METADATA_DATAFILE_COLUMN_FAMILY))
        files.add(entry.getKey().getColumnQualifier().toString());
    }
    
    assertEquals(1, files.size());
    assertTrue(files.contains("../0/default_tablet/1_0.rf"));
    
  }
  
  // test split where files of children are the same
  public void testSplit1() throws Exception {
    MockInstance mi = new MockInstance();
    Connector conn = mi.getConnector("", "");
    
    BatchWriter bw1 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    bw1.addMutation(createTablet("0", null, null, "/default_tablet", "/default_tablet/0_0.rf"));
    
    bw1.flush();
    
    BatchWriter bw2 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    MetadataTable.initializeClone("0", "1", conn, bw2);
    
    bw1.addMutation(createTablet("0", "m", null, "/default_tablet", "/default_tablet/0_0.rf"));
    bw1.addMutation(createTablet("0", null, "m", "/t-1", "/default_tablet/0_0.rf"));
    
    bw1.flush();
    
    int rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(0, rc);
    
    Scanner scanner = conn.createScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS);
    scanner.setRange(new KeyExtent(new Text("1"), null, null).toMetadataRange());
    
    HashSet<String> files = new HashSet<String>();
    
    int count = 0;
    for (Entry<Key,Value> entry : scanner) {
      if (entry.getKey().getColumnFamily().equals(Constants.METADATA_DATAFILE_COLUMN_FAMILY)) {
        files.add(entry.getKey().getColumnQualifier().toString());
        count++;
      }
    }
    
    assertEquals(1, count);
    assertEquals(1, files.size());
    assertTrue(files.contains("../0/default_tablet/0_0.rf"));
  }
  
  // test split where files of children differ... like majc and split occurred
  public void testSplit2() throws Exception {
    MockInstance mi = new MockInstance();
    Connector conn = mi.getConnector("", "");
    
    BatchWriter bw1 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    bw1.addMutation(createTablet("0", null, null, "/default_tablet", "/default_tablet/0_0.rf"));
    
    bw1.flush();
    
    BatchWriter bw2 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    MetadataTable.initializeClone("0", "1", conn, bw2);
    
    bw1.addMutation(createTablet("0", "m", null, "/default_tablet", "/default_tablet/1_0.rf"));
    Mutation mut3 = createTablet("0", null, "m", "/t-1", "/default_tablet/1_0.rf");
    mut3.putDelete(Constants.METADATA_DATAFILE_COLUMN_FAMILY.toString(), "/default_tablet/0_0.rf");
    bw1.addMutation(mut3);
    
    bw1.flush();
    
    int rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(1, rc);
    
    rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(0, rc);
    
    Scanner scanner = conn.createScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS);
    scanner.setRange(new KeyExtent(new Text("1"), null, null).toMetadataRange());
    
    HashSet<String> files = new HashSet<String>();
    
    int count = 0;
    
    for (Entry<Key,Value> entry : scanner) {
      if (entry.getKey().getColumnFamily().equals(Constants.METADATA_DATAFILE_COLUMN_FAMILY)) {
        files.add(entry.getKey().getColumnQualifier().toString());
        count++;
      }
    }
    
    assertEquals(1, files.size());
    assertEquals(2, count);
    assertTrue(files.contains("../0/default_tablet/1_0.rf"));
  }
  
  private static Mutation deleteTablet(String tid, String endRow, String prevRow, String dir, String file) throws Exception {
    KeyExtent ke = new KeyExtent(new Text(tid), endRow == null ? null : new Text(endRow), prevRow == null ? null : new Text(prevRow));
    Mutation mut = new Mutation(ke.getMetadataEntry());
    ColumnFQ.putDelete(mut, Constants.METADATA_PREV_ROW_COLUMN);
    ColumnFQ.putDelete(mut, Constants.METADATA_TIME_COLUMN);
    ColumnFQ.putDelete(mut, Constants.METADATA_DIRECTORY_COLUMN);
    mut.putDelete(Constants.METADATA_DATAFILE_COLUMN_FAMILY.toString(), file);
    
    return mut;
  }
  
  private static Mutation createTablet(String tid, String endRow, String prevRow, String dir, String file) throws Exception {
    KeyExtent ke = new KeyExtent(new Text(tid), endRow == null ? null : new Text(endRow), prevRow == null ? null : new Text(prevRow));
    Mutation mut = ke.getPrevRowUpdateMutation();
    
    ColumnFQ.put(mut, Constants.METADATA_TIME_COLUMN, new Value("M0".getBytes()));
    ColumnFQ.put(mut, Constants.METADATA_DIRECTORY_COLUMN, new Value(dir.getBytes()));
    mut.put(Constants.METADATA_DATAFILE_COLUMN_FAMILY.toString(), file, "10,200");
    
    return mut;
  }
  
  // test two tablets splitting into four
  public void testSplit3() throws Exception {
    MockInstance mi = new MockInstance();
    Connector conn = mi.getConnector("", "");
    
    BatchWriter bw1 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    bw1.addMutation(createTablet("0", "m", null, "/d1", "/d1/file1"));
    bw1.addMutation(createTablet("0", null, "m", "/d2", "/d2/file2"));
    
    bw1.flush();
    
    BatchWriter bw2 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    MetadataTable.initializeClone("0", "1", conn, bw2);
    
    bw1.addMutation(createTablet("0", "f", null, "/d1", "/d1/file3"));
    bw1.addMutation(createTablet("0", "m", "f", "/d3", "/d1/file1"));
    bw1.addMutation(createTablet("0", "s", "m", "/d2", "/d2/file2"));
    bw1.addMutation(createTablet("0", null, "s", "/d4", "/d2/file2"));
    
    bw1.flush();
    
    int rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(0, rc);
    
    Scanner scanner = conn.createScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS);
    scanner.setRange(new KeyExtent(new Text("1"), null, null).toMetadataRange());
    
    HashSet<String> files = new HashSet<String>();
    
    int count = 0;
    for (Entry<Key,Value> entry : scanner) {
      if (entry.getKey().getColumnFamily().equals(Constants.METADATA_DATAFILE_COLUMN_FAMILY)) {
        files.add(entry.getKey().getColumnQualifier().toString());
        count++;
      }
    }
    
    assertEquals(2, count);
    assertEquals(2, files.size());
    assertTrue(files.contains("../0/d1/file1"));
    assertTrue(files.contains("../0/d2/file2"));
  }
  
  // test cloned marker
  public void testClonedMarker() throws Exception {
    
    MockInstance mi = new MockInstance();
    Connector conn = mi.getConnector("", "");
    
    BatchWriter bw1 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    bw1.addMutation(createTablet("0", "m", null, "/d1", "/d1/file1"));
    bw1.addMutation(createTablet("0", null, "m", "/d2", "/d2/file2"));
    
    bw1.flush();
    
    BatchWriter bw2 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    MetadataTable.initializeClone("0", "1", conn, bw2);
    
    bw1.addMutation(deleteTablet("0", "m", null, "/d1", "/d1/file1"));
    bw1.addMutation(deleteTablet("0", null, "m", "/d2", "/d2/file2"));
    
    bw1.flush();
    
    bw1.addMutation(createTablet("0", "f", null, "/d1", "/d1/file3"));
    bw1.addMutation(createTablet("0", "m", "f", "/d3", "/d1/file1"));
    bw1.addMutation(createTablet("0", "s", "m", "/d2", "/d2/file3"));
    bw1.addMutation(createTablet("0", null, "s", "/d4", "/d4/file3"));
    
    bw1.flush();
    
    int rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(1, rc);
    
    bw1.addMutation(deleteTablet("0", "m", "f", "/d3", "/d1/file1"));
    
    bw1.flush();
    
    bw1.addMutation(createTablet("0", "m", "f", "/d3", "/d1/file3"));
    
    bw1.flush();
    
    rc = MetadataTable.checkClone("0", "1", conn, bw2);
    
    assertEquals(0, rc);
    
    Scanner scanner = conn.createScanner(Constants.METADATA_TABLE_NAME, Constants.NO_AUTHS);
    scanner.setRange(new KeyExtent(new Text("1"), null, null).toMetadataRange());
    
    HashSet<String> files = new HashSet<String>();
    
    int count = 0;
    for (Entry<Key,Value> entry : scanner) {
      if (entry.getKey().getColumnFamily().equals(Constants.METADATA_DATAFILE_COLUMN_FAMILY)) {
        files.add(entry.getKey().getColumnQualifier().toString());
        count++;
      }
    }
    
    assertEquals(3, count);
    assertEquals(3, files.size());
    assertTrue(files.contains("../0/d1/file1"));
    assertTrue(files.contains("../0/d2/file3"));
    assertTrue(files.contains("../0/d4/file3"));
  }
  
  // test two tablets splitting into four
  public void testMerge() throws Exception {
    MockInstance mi = new MockInstance();
    Connector conn = mi.getConnector("", "");
    
    BatchWriter bw1 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    bw1.addMutation(createTablet("0", "m", null, "/d1", "/d1/file1"));
    bw1.addMutation(createTablet("0", null, "m", "/d2", "/d2/file2"));
    
    bw1.flush();
    
    BatchWriter bw2 = conn.createBatchWriter(Constants.METADATA_TABLE_NAME, 10000000, 60000l, 1);
    
    MetadataTable.initializeClone("0", "1", conn, bw2);
    
    bw1.addMutation(deleteTablet("0", "m", null, "/d1", "/d1/file1"));
    Mutation mut = createTablet("0", null, null, "/d2", "/d2/file2");
    mut.put(Constants.METADATA_DATAFILE_COLUMN_FAMILY.toString(), "/d1/file1", "10,200");
    bw1.addMutation(mut);
    
    bw1.flush();
    
    try {
      MetadataTable.checkClone("0", "1", conn, bw2);
      assertTrue(false);
    } catch (TabletIterator.TabletDeletedException tde) {}
    
  }
  
}
