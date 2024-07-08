# Repository Archived

This repository has been archived and is now read-only.

## Reason for Archiving

This project will no longer be maintained and new versions will no longer be published.

## Important Notes

- The code and all issues, pull requests, and discussions are still accessible in a read-only state.
- No further updates, bug fixes, or support will be provided.

# Spark Google Spreadsheets

Google Spreadsheets datasource for [SparkSQL and DataFrames](http://spark.apache.org/docs/latest/sql-programming-guide.html)

[![Actions Build](https://github.com/riskidentdms/spark-google-spreadsheets/actions/workflows/scala.yml/badge.svg)](https://github.com/riskidentdms/spark-google-spreadsheets/actions)

## Notice

The version 0.4.0 breaks compatibility with previous versions. You must
use a ** spreadsheetId ** to identify which spreadsheet is to be accessed or altered.
In older versions, spreadsheet name was used.

If you don't know spreadsheetId, please read the [Introduction to the Google Sheets API v4](https://developers.google.com/sheets/guides/concepts).

## Requirements

This library supports different versions of Spark:

### Latest compatible versions

| This library | Spark Version |
| ------------ | ------------- |
| 0.1.x        | 3.1.1  |

## Linking

Using SBT:

```
libraryDependencies += "com.github.riskidentdms" %% "spark-google-spreadsheets" % "0.1.0"
```

Using Maven:

```xml
<dependency>
  <groupId>com.github.riskidentdms</groupId>
  <artifactId>spark-google-spreadsheets_2.12</artifactId>
  <version>0.1.0</version>
</dependency>
```

## SQL API

TBD: Should be updated

```sql
CREATE TABLE cars
USING com.github.riskidentdms.spark.google.spreadsheets
OPTIONS (
    path "<spreadsheetId>/worksheet1",
    serviceAccountId "xxxxxx@developer.gserviceaccount.com",
    credentialPath "/path/to/credential.p12"
)
```

## Scala API

TBD: Should be updated

```scala
import org.apache.spark.sql.SparkSession

val sqlContext = SparkSession.builder()
  .master("local[2]")
  .appName("SpreadsheetSuite")
  .getOrCreate().sqlContext

// Creates a DataFrame from a specified worksheet
val df = sqlContext.read.
    format("com.github.riskidentdms.spark.google.spreadsheets").
    option("serviceAccountId", "xxxxxx@developer.gserviceaccount.com").
    option("credentialPath", "/path/to/credential.p12").
    load("<spreadsheetId>/worksheet1")

// Saves a DataFrame to a new worksheet
df.write.
    format("com.github.riskidentdms.spark.google.spreadsheets").
    option("serviceAccountId", "xxxxxx@developer.gserviceaccount.com").
    option("credentialPath", "/path/to/credential.p12").
    save("<spreadsheetId>/newWorksheet")

```

### Using Google default application credentials
TBD: Should be updated

Provide authentication credentials to your application code by setting the environment variable 
`GOOGLE_APPLICATION_CREDENTIALS`. The variable should be set to the path of the service account json file.


```scala
import org.apache.spark.sql.SparkSession

val sqlContext = SparkSession.builder()
  .master("local[2]")
  .appName("SpreadsheetSuite")
  .getOrCreate().sqlContext

// Creates a DataFrame from a specified worksheet
val df = sqlContext.read.
    format("com.github.riskidentdms.spark.google.spreadsheets").
    load("<spreadsheetId>/worksheet1")
```

More details: https://cloud.google.com/docs/authentication/production

## License

Copyright 2016-2018, Katsunori Kanda

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
