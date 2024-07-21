<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BAstepSIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# ILTS
- The code for ILTS and other baselines compared in experiments are available in this repository.
    - ILTS is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_ILTS.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_ILTS.java) with convex hull precomputation in [tsfile/src/main/java/org/apache/iotdb/tsfile/file/metadata/statistics/Statistics.java](tsfile/src/main/java/org/apache/iotdb/tsfile/file/metadata/statistics/Statistics.java).
    - LTTB is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_LTTB.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_LTTB.java).
    - MinMaxLTTB is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_MinMaxPreselection.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_MinMaxPreselection.java) and [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/GroupByWithoutValueFilterDataSet.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/GroupByWithoutValueFilterDataSet.java).
    - Visval is implemented in [server/src/main/java/org/apache/iotdb/db/query/simpiece/Visval.java](server/src/main/java/org/apache/iotdb/db/query/simpiece/Visval.java).
    - MinMax is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_MinMax.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_MinMax.java).
    - M4 is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_M4.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_M4.java).
    - Uniform is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_Uniform.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_Uniform.java).
    - FSW is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_FSW.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_FSW.java) and [server/src/main/java/org/apache/iotdb/db/query/simpiece/FSW.java](server/src/main/java/org/apache/iotdb/db/query/simpiece/FSW.java).
    - SC is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_SC.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_SC.java) and [server/src/main/java/org/apache/iotdb/db/query/simpiece/ShrinkingCone.java](server/src/main/java/org/apache/iotdb/db/query/simpiece/ShrinkingCone.java).
    - Sim-Piece is implemented in [server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_SimPiece.java](server/src/main/java/org/apache/iotdb/db/query/dataset/groupby/LocalGroupByExecutorTri_SimPiece.java) and [server/src/main/java/org/apache/iotdb/db/query/simpiece/SimPiece.java](server/src/main/java/org/apache/iotdb/db/query/simpiece/SimPiece.java).
    - Some integration tests for correctness are in [server/src/test/java/org/apache/iotdb/db/integration/tri/MyTest_ILTS.java](server/src/test/java/org/apache/iotdb/db/integration/tri/MyTest_ILTS.java).
- For the README of [Apache IoTDB](https://iotdb.apache.org/) itself, please see [README_IOTDB.md](README_IOTDB.md). To build this repository, run `mvn clean package -DskipTests -pl -distribution`.
