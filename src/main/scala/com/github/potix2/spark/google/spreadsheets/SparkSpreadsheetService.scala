/*
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
package com.github.potix2.spark.google.spreadsheets

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.sheets.v4.model._
import org.apache.spark.sql.types.StructType
import com.google.api.services.sheets.v4.Sheets

import java.util.{List => JavaList}
import scala.collection.JavaConverters._
import com.google.api.client.json.gson.GsonFactory
import com.google.auth.http.HttpCredentialsAdapter

import scala.Option.option2Iterable
import scala.util.Try

object SparkSpreadsheetService {
  private val APP_NAME = "spark-google-spreadsheets-1.0.0"
  private val HTTP_TRANSPORT: NetHttpTransport =
    GoogleNetHttpTransport.newTrustedTransport()
  private val JSON_FACTORY: GsonFactory = GsonFactory.getDefaultInstance

  private implicit class SpreadsheetOps(val spreadsheet: Spreadsheet)
      extends AnyVal {

    def executeBatchUpdate(requests: Seq[Request])(
      implicit context: SparkSpreadsheetContext
    ): BatchUpdateSpreadsheetResponse = {
      context.service
        .spreadsheets()
        .batchUpdate(
          spreadsheet.getSpreadsheetId,
          new BatchUpdateSpreadsheetRequest()
            .setRequests(requests.asJava)
        )
        .execute()
    }

    def updateSheet[T](schema: StructType,
                       nextId: Integer,
                       data: List[T],
                       extractor: T => RowData)(firstRequests: Request)(
      implicit context: SparkSpreadsheetContext
    ): BatchUpdateSpreadsheetResponse = {

      val headerValues: List[CellData] = schema.fields.map { field =>
        new CellData()
          .setUserEnteredValue(
            new ExtendedValue()
              .setStringValue(field.name)
          )
      }(collection.breakOut)

      val updateHeaderRequest = new UpdateCellsRequest()
        .setStart(
          new GridCoordinate()
            .setSheetId(nextId)
            .setRowIndex(0)
            .setColumnIndex(0)
        )
        .setRows(List(new RowData().setValues(headerValues.asJava)).asJava)
        .setFields("userEnteredValue")

      val updateRowsRequest = new UpdateCellsRequest()
        .setStart(
          new GridCoordinate()
            .setSheetId(nextId)
            .setRowIndex(1)
            .setColumnIndex(0)
        )
        .setRows(data.map(extractor).asJava)
        .setFields("userEnteredValue")

      val requests = List(
        firstRequests,
        new Request().setUpdateCells(updateHeaderRequest),
        new Request().setUpdateCells(updateRowsRequest)
      )

      spreadsheet.executeBatchUpdate(requests)
    }
  }

  case class SparkSpreadsheetContext(credentials: HttpCredentialsAdapter) {

    lazy val service: Sheets =
      new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
        .setApplicationName(APP_NAME)
        .build()

    def findSpreadsheet(spreadSheetId: String): SparkSpreadsheet =
      SparkSpreadsheet(service.spreadsheets().get(spreadSheetId).execute())(
        this
      )

    def query(spreadsheetId: String, range: String): ValueRange =
      service.spreadsheets().values().get(spreadsheetId, range).execute()
  }

  case class SparkSpreadsheet(private var spreadsheet: Spreadsheet)(
    implicit context: SparkSpreadsheetContext
  ) {
    def name: String = spreadsheet.getProperties.getTitle
    def getWorksheets: Seq[SparkWorksheet] =
      spreadsheet.getSheets.asScala.map(SparkWorksheet(spreadsheet, _)(context))

    def nextSheetId: Integer =
      getWorksheets.map(_.sheet.getProperties.getSheetId).max + 1

    private def sheetRequest(sheetName: String,
                             colNum: Integer,
                             rowNum: Integer): AddSheetRequest = {
      val addSheetRequest = new AddSheetRequest()
      addSheetRequest.setProperties(
        new SheetProperties()
          .setSheetId(nextSheetId)
          .setTitle(sheetName)
          .setGridProperties(
            new GridProperties()
              .setColumnCount(colNum)
              .setRowCount(rowNum)
          )
      )
    }

    def addWorksheet(sheetName: String,
                     colNum: Integer,
                     rowNum: Integer): Unit = {
      val sheetReq = sheetRequest(sheetName, colNum, rowNum)

      val requests: Seq[Request] = List(new Request().setAddSheet(sheetReq))

      spreadsheet.executeBatchUpdate(requests)

      spreadsheet = context.service
        .spreadsheets()
        .get(spreadsheet.getSpreadsheetId)
        .execute()
    }

    def addWorksheet[T](sheetName: String,
                        schema: StructType,
                        data: List[T],
                        extractor: T => RowData): Unit = {
      val colNum = schema.fields.length
      val rowNum = data.size + 1
      val nextId = nextSheetId
      val sheetReq = sheetRequest(sheetName, colNum, rowNum)

      spreadsheet.updateSheet(schema, nextId, data, extractor)(
        new Request().setAddSheet(sheetReq)
      )

      spreadsheet = context.service
        .spreadsheets()
        .get(spreadsheet.getSpreadsheetId)
        .execute()
    }

    def findWorksheet(worksheetName: String): Option[SparkWorksheet] = {
      val worksheets: Seq[SparkWorksheet] = getWorksheets
      worksheets.find(_.sheet.getProperties.getTitle == worksheetName)
    }

    def deleteWorksheet(worksheetName: String): Unit = {
      val worksheet: Option[SparkWorksheet] = findWorksheet(worksheetName)
      if (worksheet.isDefined) {
        val request = new Request()
        val sheetId = worksheet.get.sheet.getProperties.getSheetId
        request.setDeleteSheet(
          new DeleteSheetRequest()
            .setSheetId(sheetId)
        )

        spreadsheet.executeBatchUpdate(List(request))

        spreadsheet = context.service
          .spreadsheets()
          .get(spreadsheet.getSpreadsheetId)
          .execute()
      }
    }
  }

  case class SparkWorksheet(spreadsheet: Spreadsheet, sheet: Sheet)(
    implicit context: SparkSpreadsheetContext
  ) {

    require(
      sheet.getProperties.getTitle != null,
      "Title of spreadsheet should be defined."
    )

    val name: String = sheet.getProperties.getTitle

    lazy val values: Option[List[JavaList[Object]]] =
      Option(context.query(spreadsheet.getSpreadsheetId, name).getValues)
        .map(_.asScala.toList)

    lazy val headers: Seq[String] =
      values.toSeq.flatMap(
        _.headOption
          .fold[Seq[String]](Seq.empty)(
            _.asScala.map(_.toString)(collection.breakOut)
          )
      )

    lazy val rows: Seq[Map[String, String]] = {
      values.fold(Seq.empty[Map[String, String]])(
        v =>
          if (v.isEmpty) {
            Seq.empty
          } else {
            v.tail.map { row =>
              headers
                .zip(row.asScala.map(_.toString))(collection.breakOut): Map[
                String,
                String
              ]
            }
        }
      )
    }

    def updateCells[T](schema: StructType,
                       data: List[T],
                       extractor: T => RowData): Unit = {
      val colNum = schema.fields.length
      val rowNum = data.size + 2
      val sheetId = sheet.getProperties.getSheetId

      val updatePropertiesRequest = new UpdateSheetPropertiesRequest()
      updatePropertiesRequest
        .setProperties(
          new SheetProperties()
            .setSheetId(sheetId)
            .setGridProperties(
              new GridProperties()
                .setColumnCount(colNum)
                .setRowCount(rowNum)
            )
        )
        .setFields("gridProperties(rowCount,columnCount)")

      spreadsheet.updateSheet(schema, sheetId, data, extractor)(
        new Request().setUpdateSheetProperties(updatePropertiesRequest)
      )
    }
  }

  /**
    * create new context of spreadsheets for spark
    *
    * @param credentials
    * @return
    */
  def apply(credentials: HttpCredentialsAdapter): SparkSpreadsheetContext =
    SparkSpreadsheetContext(credentials)

  /**
    * find a spreadsheet by name
    *
    * @param spreadsheetName
    * @param context
    * @return
    */
  def findSpreadsheet(
    spreadsheetName: String
  )(context: SparkSpreadsheetContext): Option[SparkSpreadsheet] =
    Try(context.findSpreadsheet(spreadsheetName)).toOption
}
