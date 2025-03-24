package com.wiseyak.conf


import scala.io.Source
import scala.util.Try

/**
  * This class is a part of the package com.wiseyak.conf and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-23.
  */
case class AppConfiguration(
                               ontologyInputFile: String = AppConfiguration.EMPTY_STRING,
                               ontologyProfileFile: String = AppConfiguration.EMPTY_STRING,
                               supportedFormats: List[String] = AppConfiguration.EMPTY_LIST,
                               basicTypeDefinitions: String = AppConfiguration.EMPTY_STRING,
                               outputFolder: String = AppConfiguration.EMPTY_STRING,
                               inferInverseRelation: String = AppConfiguration.OFF,
                               outputFile: String = AppConfiguration.EMPTY_STRING,
                               indexProperties: String = AppConfiguration.OFF,
                               indexInformation: List[IndexProperty] = List()
                           ) {

    /**
      * Checks if it is to infer the inverse relation or not.
      */
    def needsInverseRelation: Boolean = inferInverseRelation.toUpperCase == AppConfiguration.ON

    /**
      * Returns output path.
      */
    def schemaOutputFile: String = outputFolder + outputFile

    /**
      * Is an empty instance.
      */
    def isEmpty: Boolean = ontologyInputFile.isEmpty || supportedFormats.isEmpty || outputFile.isEmpty

    /**
      * Returns the content of basic type definitions.
      */
    def basicTypesContent: String = Try(Source.fromFile(basicTypeDefinitions).getLines).getOrElse(AppConfiguration.EMPTY_LIST).mkString
}

/**
  * The companion object.
  */
object AppConfiguration {
    /**
      * String literal empty.
      */
    val EMPTY_STRING: String = ""

    /**
      * Empty String List.
      */
    val EMPTY_LIST: List[String] = List[String]()

    /**
      * String literal OFF.
      */
    val OFF: String = "OFF"

    /**
      * String literal ON.
      */
    val ON: String = "ON"

    /**
      * String literal UTF-8
      */
    val UTF8: String = "utf-8"

    /**
      * Comments prefix.
      */
    val COMMENT_PREFIX: String = "//"

    /**
      * An empty instance.
      */
    def empty: AppConfiguration = AppConfiguration()
}
