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
package org.apache.accumulo.server.test.randomwalk.concurrent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.security.SystemPermission;
import org.apache.accumulo.core.security.TablePermission;
import org.apache.accumulo.server.test.randomwalk.State;
import org.apache.accumulo.server.test.randomwalk.Test;

public class ChangePermissions extends Test {
  
  @Override
  public void visit(State state, Properties props) throws Exception {
    Connector conn = state.getConnector();
    
    Random rand = (Random) state.get("rand");
    
    @SuppressWarnings("unchecked")
    List<String> userNames = (List<String>) state.get("users");
    String userName = userNames.get(rand.nextInt(userNames.size()));
    
    @SuppressWarnings("unchecked")
    List<String> tableNames = (List<String>) state.get("tables");
    String tableName = tableNames.get(rand.nextInt(tableNames.size()));
    
    try {
      if (rand.nextBoolean())
        changeSystemPermission(conn, rand, userName);
      else
        changeTablePermission(conn, rand, userName, tableName);
    } catch (AccumuloSecurityException ex) {
      log.debug("Unable to change user permissions: " + ex.getCause());
    }
  }
  
  private void changeTablePermission(Connector conn, Random rand, String userName, String tableName) throws AccumuloException, AccumuloSecurityException {
    
    EnumSet<TablePermission> perms = EnumSet.noneOf(TablePermission.class);
    for (TablePermission p : TablePermission.values()) {
      if (conn.securityOperations().hasTablePermission(userName, tableName, p))
        perms.add(p);
    }
    
    EnumSet<TablePermission> more = EnumSet.allOf(TablePermission.class);
    more.removeAll(perms);
    
    if (rand.nextBoolean() && more.size() > 0) {
      List<TablePermission> moreList = new ArrayList<TablePermission>(more);
      TablePermission choice = moreList.get(rand.nextInt(moreList.size()));
      log.debug("adding permission " + choice);
      conn.securityOperations().grantTablePermission(userName, tableName, choice);
    } else {
      if (perms.size() > 0) {
        List<TablePermission> permList = new ArrayList<TablePermission>(perms);
        TablePermission choice = permList.get(rand.nextInt(permList.size()));
        log.debug("removing permission " + choice);
        conn.securityOperations().revokeTablePermission(userName, tableName, choice);
      }
    }
  }
  
  private void changeSystemPermission(Connector conn, Random rand, String userName) throws AccumuloException, AccumuloSecurityException {
    EnumSet<SystemPermission> perms = EnumSet.noneOf(SystemPermission.class);
    for (SystemPermission p : SystemPermission.values()) {
      if (conn.securityOperations().hasSystemPermission(userName, p))
        perms.add(p);
    }
    
    EnumSet<SystemPermission> more = EnumSet.allOf(SystemPermission.class);
    more.removeAll(perms);
    more.remove(SystemPermission.GRANT);
    
    if (rand.nextBoolean() && more.size() > 0) {
      List<SystemPermission> moreList = new ArrayList<SystemPermission>(more);
      SystemPermission choice = moreList.get(rand.nextInt(moreList.size()));
      log.debug("adding permission " + choice);
      conn.securityOperations().grantSystemPermission(userName, choice);
    } else {
      if (perms.size() > 0) {
        List<SystemPermission> permList = new ArrayList<SystemPermission>(perms);
        SystemPermission choice = permList.get(rand.nextInt(permList.size()));
        log.debug("removing permission " + choice);
        conn.securityOperations().revokeSystemPermission(userName, choice);
      }
    }
  }
  
}
