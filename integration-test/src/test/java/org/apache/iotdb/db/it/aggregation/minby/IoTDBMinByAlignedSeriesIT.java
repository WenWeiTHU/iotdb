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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.db.it.aggregation.minby;

import org.apache.iotdb.it.env.EnvFactory;

import org.junit.BeforeClass;

import static org.apache.iotdb.db.it.utils.TestUtils.prepareData;

public class IoTDBMinByAlignedSeriesIT extends IoTDBMinByIT {
  protected static final String[] ALIGNED_DATASET =
      new String[] {
        // x input
        "CREATE ALIGNED TIMESERIES root.db.d1(x1 INT32, x2 INT64, x3 FLOAT, x4 DOUBLE, x5 BOOLEAN, x6 TEXT)",
        // y input
        "CREATE ALIGNED TIMESERIES root.db.d1(y1 INT32, y2 INT64, y3 FLOAT, y4 DOUBLE, y5 BOOLEAN, y6 TEXT)",
        "INSERT INTO root.db.d1(timestamp,x1,x2,x3,x4,x5,x6) values(1, 1, 1, 1, 1, true, \"1\")",
        "INSERT INTO root.db.d1(timestamp,x1,x2,x3,x4,x5,x6) values(2, 2, 2, 2, 2, false, \"2\")",
        "INSERT INTO root.db.d1(timestamp,x1,x2,x3,x4,x5,x6) values(3, 3, 3, 3, 3, false, \"3\")",
        "INSERT INTO root.db.d1(timestamp,y1,y2,y3,y4,y5,y6) values(2, 3, 3, 3, 3, true, \"4\")",
        "INSERT INTO root.db.d1(timestamp,y1,y2,y3,y4,y5,y6) values(3, 2, 2, 2, 2, false, \"3\")",
        "INSERT INTO root.db.d1(timestamp,y1,y2,y3,y4,y5,y6) values(4, 1, 1, 1, 1, false, \"4\")",
        "INSERT INTO root.db.d1(timestamp,x1,x2,x3,x4,x5,x6) values(8, 3, 3, 3, 3, false, \"3\")",
        "INSERT INTO root.db.d1(timestamp,y1,y2,y3,y4,y5,y6) values(8, 8, 8, 8, 8, false, \"4\")",
        "INSERT INTO root.db.d1(timestamp,x1,x2,x3,x4,x5,x6) values(12, 3, 3, 3, 3, false, \"3\")",
        "INSERT INTO root.db.d1(timestamp,y1,y2,y3,y4,y5,y6) values(12, 0, 0, 0, 0, false, \"4\")",
        "INSERT INTO root.db.d1(timestamp,x1,x2,x3,x4,x5,x6) values(13, 4, 4, 4, 4, false, \"4\")",
        "INSERT INTO root.db.d1(timestamp,y1,y2,y3,y4,y5,y6) values(13, 0, 0, 0, 0, false, \"4\")",
        "flush",
        // For Align By Device
        "CREATE ALIGNED TIMESERIES root.db.d2(x1 INT32, x2 INT64, x3 FLOAT, x4 DOUBLE, x5 BOOLEAN, x6 TEXT)",
        "CREATE ALIGNED TIMESERIES root.db.d2(y1 INT32, y2 INT64, y3 FLOAT, y4 DOUBLE, y5 BOOLEAN, y6 TEXT)",
        "INSERT INTO root.db.d2(timestamp,x1,x2,x3,x4,x5,x6) values(1, 1, 1, 1, 1, true, \"1\")",
        "INSERT INTO root.db.d2(timestamp,x1,x2,x3,x4,x5,x6) values(2, 2, 2, 2, 2, false, \"2\")",
        "INSERT INTO root.db.d2(timestamp,x1,x2,x3,x4,x5,x6) values(3, 3, 3, 3, 3, false, \"3\")",
        "INSERT INTO root.db.d2(timestamp,y1,y2,y3,y4,y5,y6) values(2, 3, 3, 3, 3, true, \"4\")",
        "INSERT INTO root.db.d2(timestamp,y1,y2,y3,y4,y5,y6) values(3, 2, 2, 2, 2, false, \"3\")",
        "INSERT INTO root.db.d2(timestamp,y1,y2,y3,y4,y5,y6) values(4, 1, 1, 1, 1, false, \"4\")",
        "INSERT INTO root.db.d2(timestamp,x1,x2,x3,x4,x5,x6) values(8, 3, 3, 3, 3, false, \"3\")",
        "INSERT INTO root.db.d2(timestamp,y1,y2,y3,y4,y5,y6) values(8, 8, 8, 8, 8, false, \"4\")",
        "INSERT INTO root.db.d2(timestamp,x1,x2,x3,x4,x5,x6) values(12, 3, 3, 3, 3, false, \"3\")",
        "INSERT INTO root.db.d2(timestamp,y1,y2,y3,y4,y5,y6) values(12, 0, 0, 0, 0, false, \"4\")",
        "INSERT INTO root.db.d2(timestamp,x1,x2,x3,x4,x5,x6) values(13, 4, 4, 4, 4, false, \"4\")",
        "INSERT INTO root.db.d2(timestamp,y1,y2,y3,y4,y5,y6) values(13, 0, 0, 0, 0, false, \"4\")",
      };

  @BeforeClass
  public static void setUp() throws Exception {
    EnvFactory.getEnv().getConfig().getCommonConfig().setPartitionInterval(1000);
    EnvFactory.getEnv().initClusterEnvironment();
    prepareData(ALIGNED_DATASET);
  }
}
