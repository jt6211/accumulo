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
package org.apache.accumulo.wikisearch.query;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.accumulo.wikisearch.sample.Results;


@Path("/Query")
public interface IQuery {
  
  @GET
  @POST
  @Path("/html")
  @Consumes("*/*")
  public String html(@QueryParam("query") String query, @QueryParam("auths") String auths);
  
  @GET
  @POST
  @Path("/xml")
  @Consumes("*/*")
  @Produces("application/xml")
  public Results xml(@QueryParam("query") String query, @QueryParam("auths") String auths);
  
  @GET
  @POST
  @Path("/json")
  @Consumes("*/*")
  @Produces("application/json")
  public Results json(@QueryParam("query") String query, @QueryParam("auths") String auths);
  
  @GET
  @POST
  @Path("/yaml")
  @Consumes("*/*")
  @Produces("text/x-yaml")
  public Results yaml(@QueryParam("query") String query, @QueryParam("auths") String auths);
  
  @GET
  @POST
  @Path("/content")
  @Consumes("*/*")
  @Produces("application/xml")
  public Results content(@QueryParam("query") String query, @QueryParam("auths") String auths);
  
}