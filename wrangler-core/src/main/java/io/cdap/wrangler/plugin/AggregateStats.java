/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

 package io.cdap.wrangler.plugin;

 import io.cdap.wrangler.api.*;
 import io.cdap.wrangler.api.parser.*;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * A directive that aggregates byte sizes and time durations across all rows.
  */
 public class AggregateStats implements Directive {
 
   private String sizeColumn;
   private String timeColumn;
   private String outputSizeColumn;
   private String outputTimeColumn;
   private String unitSize = "MB";
   private String unitTime = "seconds";
   private String aggregation = "total";
 
   private long totalBytes = 0;
   private long totalMillis = 0;
 
   @Override
public UsageDefinition define() {
  UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
  builder.define("sizeColumn", TokenType.COLUMN_NAME);
  builder.define("timeColumn", TokenType.COLUMN_NAME);
  builder.define("outputSizeColumn", TokenType.COLUMN_NAME);
  builder.define("outputTimeColumn", TokenType.COLUMN_NAME);
  builder.define("unitSize", TokenType.TEXT, true);
  builder.define("unitTime", TokenType.TEXT, true);
  builder.define("aggregation", TokenType.TEXT, true);
  return builder.build();
}

 
@Override
public void initialize(Arguments arguments) throws DirectiveParseException {
  sizeColumn = ((ColumnName) arguments.value("sizeColumn")).value();
  timeColumn = ((ColumnName) arguments.value("timeColumn")).value();
  outputSizeColumn = ((ColumnName) arguments.value("outputSizeColumn")).value();
  outputTimeColumn = ((ColumnName) arguments.value("outputTimeColumn")).value();

  if (arguments.contains("unitSize")) {
    unitSize = ((Text) arguments.value("unitSize")).value().toUpperCase();
  }

  if (arguments.contains("unitTime")) {
    unitTime = ((Text) arguments.value("unitTime")).value().toLowerCase();
  }

  if (arguments.contains("aggregation")) {
    aggregation = ((Text) arguments.value("aggregation")).value().toLowerCase();
  }
}

 
   @Override
   public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
     for (Row row : rows) {
       Object sizeObj = row.getValue(sizeColumn);
       Object timeObj = row.getValue(timeColumn);
 
       if (sizeObj != null) {
         totalBytes += parseBytes(sizeObj.toString());
       }
 
       if (timeObj != null) {
         totalMillis += parseMillis(timeObj.toString());
       }
     }
 
     long rowCount = rows.size();
     double finalSize = aggregation.equals("average") ? (totalBytes / (double) rowCount) : totalBytes;
     double finalTime = aggregation.equals("average") ? (totalMillis / (double) rowCount) : totalMillis;
 
     double convertedSize = convertBytes(finalSize, unitSize);
     double convertedTime = convertTime(finalTime, unitTime);
 
     Row result = new Row(outputSizeColumn, convertedSize);
     result.add(outputTimeColumn, convertedTime);
 
     return Collections.singletonList(result);
   }
 
   @Override
   public void destroy() {
     // Optional cleanup logic
   }
 
   // ---------- Helper Methods ----------
 
   private long parseBytes(String value) {
     value = value.trim().toUpperCase();
     try {
       if (value.endsWith("KB")) {
         return (long) (Double.parseDouble(value.replace("KB", "")) * 1024);
       } else if (value.endsWith("MB")) {
         return (long) (Double.parseDouble(value.replace("MB", "")) * 1024 * 1024);
       } else if (value.endsWith("GB")) {
         return (long) (Double.parseDouble(value.replace("GB", "")) * 1024 * 1024 * 1024);
       } else if (value.endsWith("B")) {
         return Long.parseLong(value.replace("B", ""));
       }
     } catch (NumberFormatException e) {
       throw new RuntimeException("Invalid byte size format: " + value, e);
     }
     return 0;
   }
 
   private long parseMillis(String value) {
     value = value.trim().toLowerCase();
     try {
       if (value.endsWith("ms")) {
         return (long) Double.parseDouble(value.replace("ms", ""));
       } else if (value.endsWith("s")) {
         return (long) (Double.parseDouble(value.replace("s", "")) * 1000);
       } else if (value.endsWith("m")) {
         return (long) (Double.parseDouble(value.replace("m", "")) * 60 * 1000);
       } else if (value.endsWith("h")) {
         return (long) (Double.parseDouble(value.replace("h", "")) * 60 * 60 * 1000);
       }
     } catch (NumberFormatException e) {
       throw new RuntimeException("Invalid time duration format: " + value, e);
     }
     return 0;
   }
 
   private double convertBytes(double value, String unit) {
     switch (unit) {
       case "GB": return value / (1024 * 1024 * 1024.0);
       case "MB": return value / (1024 * 1024.0);
       case "KB": return value / 1024.0;
       case "B":
       default: return value;
     }
   }
 
   private double convertTime(double value, String unit) {
     switch (unit) {
       case "seconds": return value / 1000.0;
       case "minutes": return value / (60 * 1000.0);
       case "hours": return value / (60 * 60 * 1000.0);
       case "milliseconds":
       default: return value;
     }
   }
 }
 