package com.wiseyak.conf

import org.apache.commons.io.FilenameUtils
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import scala.io.Source
import scala.util.Try
;

/**
  * This class is a part of the package com.wiseyak.app and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-23.
  */
object Configuration {

    /**
      * Implicit definition of Json Formats.
      */
    private implicit val formats: DefaultFormats.type = DefaultFormats

    /**
      * Initialize rule definition file.
      */
    private val configurationFile = "resources/configuration.json"

    /**
      * Schema content file, which is used during search part of GraphQL generation.
      */
    val schemaContentFile = "resources/schema/schema-info.json"

    /**
      * Search parameters information from FHIR web site.
      * https://www.hl7.org/fhir/searchparameter-registry.html
      * https://www.hl7.org/fhir/search.html
      */
    private val searchParameterFile = "resources/json/definitions.json/search-parameters.json"

    /**
      * Initialize the configuration from the file.
      */
    /**
      * Load rules from file.
      */
    private def loadConfigurationFile = Source
        .fromFile(configurationFile, AppConfiguration.UTF8).getLines()
        .filter(item => !item.trim.startsWith(AppConfiguration.COMMENT_PREFIX))
        .mkString(System.lineSeparator)


    /**
      * Load all rules definitions.
      */
    private val appConfiguration: AppConfiguration =
        Try(JsonMethods.parse(loadConfigurationFile).extract[AppConfiguration]).getOrElse(AppConfiguration.empty)


    /**
      * Returns the input folder to generate the schema.
      *
      * @return inputFolder of schema definitions.
      */
    def ontologyInputFile: String = appConfiguration.ontologyInputFile

    /**
      * Returns the output file to write the generated schema.
      *
      * @return schemaOutputFile
      */
    def schemaOutputFile: String = appConfiguration.schemaOutputFile

    /**
      * Output folder.
      */
    def outputFolder: String = appConfiguration.outputFolder

    /**
      * Output file.
      */
    def outputFile: String = appConfiguration.outputFile

    /**
      * Profile file.
      */
    def ontologyProfileFile: String = appConfiguration.ontologyProfileFile

    /**
      * Returns the list of supported schema definitions.
      *
      * @return supportedFormats in the list.
      */
    def supportedFormats: List[String] = appConfiguration.supportedFormats.map(_.toUpperCase)

    /**
      * Returns the definition file for basic types and their mapping to the
      * graphql primitives.
      *
      * @return mapOfBasicTypes
      */
    def basicTypeDefinitions: Map[String, String] = JsonMethods
        .parse(appConfiguration.basicTypesContent).extract[Map[String, String]]
        .map(typeName => (typeName._1.toLowerCase, typeName._2))

    /**
      * Checks if it needs to extract the inverse relation or not.
      */
    def needsInverseRelation: Boolean = appConfiguration.needsInverseRelation

    /**
      * Extract file format.
      */
    def ontologyFormat: String = FilenameUtils.getExtension(ontologyInputFile).toUpperCase

    /**
      * Default namespaces.
      */

    def defaultNameSpaces: List[String] = List("http://hl7.org/fhir")

    /**
      * Returns the definition of search parameters, used to build indexing in the backend database.
      */
    def searchParameters: String = Source.fromFile(searchParameterFile, "UTF8").mkString

}
