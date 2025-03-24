package com.wiseyak.graphql

import java.io.{File, PrintWriter}

import com.wiseyak.conf.Configuration
import com.wiseyak.json.JSONSchema
import com.wiseyak.ontology._
import com.wiseyak.xml.XMLSchema

import scala.collection.mutable

/**
  * This class is a part of the package com.wiseyak.graphql and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-10.
  */
object Generator {

    /**
      * Write content to given content writer.
      *
      * @param writer , given writer.
      * @param content, generated schema content.
      */
    private def writeContent(writer: PrintWriter, content: String): Unit = {
        writer.write(content)
        writer.write(ScalarType.NEW_LINE)
        writer.write(ScalarType.NEW_LINE)
    }

    /**
      * Format the given schema file name.
      *
      * @param schemaName
      * @return schemaFileName
      */
    private def getFileName(schemaName: String): String =
        Configuration.outputFolder + List(schemaName.toLowerCase, ScalarType.UNDERSCORE, Configuration.outputFile).mkString

    /**
      * Write the generated output here.
      */
    private def writeSchema(propertyClasses: List[String], schemaName: String = "full"): Unit = {
        val schemaWriter = new PrintWriter(new File(getFileName(schemaName)))
        propertyClasses.foreach(content => writeContent(schemaWriter, content))
        schemaWriter.close()
    }

    /**
      * Extracts the class properties based on the configuration of schema format.
      *
      * @return listOfClassProperties
      */
    private def buildPropertyClasses: List[ClassProperties] = Configuration.ontologyFormat match {
        case ScalarType.TTL_FORMAT => TTLSchema.listOfClassProperties
        case ScalarType.XSD_FORMAT => XMLSchema.listOfClassProperties
        case ScalarType.JSON_FORMAT => JSONSchema.listOfClassProperties
        case _ => List[ClassProperties]()
    }

    private def buildEnumerationClasses: List[EnumerationProperties] = Configuration.ontologyFormat match {
        case ScalarType.TTL_FORMAT => TTLSchema.listOfEnumerationProperties
        case ScalarType.XSD_FORMAT => XMLSchema.listOfEnumerationProperties
        case ScalarType.JSON_FORMAT => JSONSchema.listOfEnumerationProperties
        case _ => List[EnumerationProperties]()
    }

    private def buildUnionClasses: List[WiseUnionClass] = Configuration.ontologyFormat match {
        //case ScalarType.TTL_FORMAT => TTLSchema.listOfUnionClasses
        case ScalarType.XSD_FORMAT => XMLSchema.listOfUnionClasses
        //case ScalarType.JSON_FORMAT => JSONSchema.listOfUnionClasses
        case _ => List[WiseUnionClass]()
    }

    private def allBasicTypes(): List[String] = Configuration.basicTypeDefinitions.values.toList.distinct.sorted

    private def writeSchemaInformation(enumProperties: List[EnumerationProperties], classProperties: List[ClassProperties]): Unit = {
        val typeContent = enumProperties.map(_.enumType).map(entry => ClassNameUtility.formatClassName(entry.typeName) -> entry).toMap ++
            classProperties.map(_.classType).map(entry => ClassNameUtility.formatClassName(entry.typeName) -> entry).toMap
        val jsonWriter = new PrintWriter(new File(Configuration.schemaContentFile))
        writeContent(jsonWriter, GeneratedTypes.of(typeContent, allBasicTypes(), Generator.allUnionDefinitions).toString)
        jsonWriter.close()
    }

    /**
      * 1. Unique class and their properties.
      */
    val uniqueClassProperties: List[ClassProperties] = buildPropertyClasses

    /**
      * Enumeration class and their properties.
      */
    val enumerationProperties: List[EnumerationProperties] = buildEnumerationClasses

    /**
      * Path mapping of Template classes.
      */
    val allUnionDefinitions: Map[String, WiseUnionClass] = buildUnionClasses.map(entry => entry.propertyPath -> entry).toMap

    /**
      * Collect Inverse Relations.
      */
    val inverseRelation: InverseRelation = InverseRelation.of(uniqueClassProperties)

    /**
      * Perform schema generation here.
      */
    def buildGraphQLSchema(): Unit = {
        /**
          * Schema are divided according to their sections.
          */
        val splitSchemas: Map[String, List[String]] = SchemaClasses
            .of(enumerationProperties, uniqueClassProperties, inverseRelation)
            .splitSchema

        val singleSchema: List[String] = SchemaClasses.schemaTypes.flatMap(schemaType => {
            val schema = splitSchemas(schemaType)
            val generatedSchema = mutable.ListBuffer[String]()
            generatedSchema += ClassNameUtility.openingMessage()
            generatedSchema += ClassNameUtility.authorInfo()
            schemaType match {
                case SchemaClasses.ENUM_SCHEMA => generatedSchema += ClassNameUtility.enumTypeHeader()
                case SchemaClasses.UNION_DEFINITION => generatedSchema += ClassNameUtility.unionTypeHeader()
                case _ => generatedSchema += ClassNameUtility.classTypeHeader()
            }
            generatedSchema ++= schema
            generatedSchema += ClassNameUtility.closingMessage()
            generatedSchema.toList
        })

        writeSchema(singleSchema)
        writeSchemaInformation(enumerationProperties, uniqueClassProperties)

    }

}
