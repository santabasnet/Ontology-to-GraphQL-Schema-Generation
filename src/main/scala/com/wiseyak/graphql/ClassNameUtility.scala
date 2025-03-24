package com.wiseyak.graphql

import java.util.Date

import com.wiseyak.ontology.ScalarType
import org.apache.commons.lang3.StringUtils
import org.apache.jena.ext.com.google.common.base.CaseFormat
import org.xmlet.xsdparser.xsdelements.XsdSimpleType

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-10.
  */
object ClassNameUtility {

    /**
      * List of arithmetic operators.
      */
    private val arithmeticOperators: Set[Char] = Set('+', '-', '>', '<', '=', '/', '^', '*', '!', '%')

    /**
      * Mapping or arithmetic operators.
      */
    private val operatorsMappings: Map[String, String] = Map(
        "=" -> "Equal",
        ">" -> "Greater_Than",
        "<" -> "Less_Than",
        "==" -> "Exactly_Equal",
        ">=" -> "Greater_Than_Or_Equal",
        "<=" -> "Less_Than_Or_Equal",
        "!=" -> "Not_Equal"
    )

    /**
      * DGraph Reserved Words: Type names.
      */
    private val dgraphReservedNames: List[List[String]] = List(
        "$Filter", "$Orderable", "$Order", "$Ref", "Add$Input", "Update$Input", "$Patch", "Add$Payload", "Delete$Payload"
    ).map(_.split("\\$").toList)

    /**
      * DGraph
      */
    private val keyWords: List[String] = List[String]("Subscription")

    /**
      * Check if the given type name is of reserved name.
      */
    def hasReservedName(typeName: String): Boolean =
        if (keyWords.contains(typeName)) true
        else dgraphReservedNames.exists(validation => typeName.startsWith(validation.head) && typeName.endsWith(validation.last))


    def getKeywordClass(givenName: String): String = givenName + ScalarType.UNDERSCORE

    /**
      * Class Name Utility.
      *
      * @param givenName, given name.
      * @return upperCamel class name.
      */
    def formatClassName(givenName: String): String = {
        val result = List(ScalarType.DOT, ScalarType.UNDERSCORE)
            .foldLeft(givenName)((replaced, item) => StringUtils.replace(replaced, item, ScalarType.EMPTY))

        val mappedResult: String = if (ScalarType.isDGraphPrimitive(result)) result
        else if (ScalarType.isPrimitive(result)) ScalarType.mappingOf(result)
        else result.capitalize

        if (hasReservedName(mappedResult)) getKeywordClass(mappedResult) else mappedResult
    }

    /**
      * Variable Name Utility.
      *
      * @param propertyName, given property name.
      * @return lowerCamel variable name.
      */
    def extractVariableNameOf(propertyName: String): String = {
        val position: Int = propertyName.length - propertyName.lastIndexOf(".")
        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, propertyName.takeRight(position - 1))
    }

    /**
      * Formats Enumeration Class name.
      */
    def formatEnumClassName(name: String): String = {
        var newName = (if(name.endsWith("_list")) name.replace("_list", "") else name).capitalize
        val result = StringUtils.replace(newName, ScalarType.HYPHEN, ScalarType.UNDERSCORE)
        if (hasReservedName(result)) getKeywordClass(result) else result
    }

    private def getNameOnly(givenName: String): String = givenName.split(ScalarType.UNDERSCORE).head.capitalize

    private def ignorePrefix(value: String): Boolean = {
        !(value.head.isDigit /*|| arithmeticOperators.contains(value.head)*/)
    }

    private def mapOperators(value: String): String = operatorsMappings.getOrElse(value, value)

    /**
      * Formats Enumeration values.
      */
    def formatEnumValue(value: String, simpleType: XsdSimpleType): String = {
        val formattedValue = List(ScalarType.DOT, ScalarType.HYPHEN, ScalarType.UNDERSCORE, ScalarType.SLASH)
            .foldLeft(value)((replaced, item) => StringUtils.replace(replaced, item, ScalarType.SPACE))
            .split(ScalarType.SPACE).map(_.capitalize).mkString(ScalarType.SPACE)

        val result = if (ignorePrefix(formattedValue)) formattedValue.split(ScalarType.SPACE).map(mapOperators).mkString(ScalarType.UNDERSCORE)
        else (getNameOnly(simpleType.getName) +: formattedValue.split(ScalarType.SPACE).toList).map(mapOperators).mkString(ScalarType.UNDERSCORE)

        if (hasReservedName(result)) getKeywordClass(result) else result
    }

    /**
      * Closing Message.
      */
    def openingMessage(): String =
        """
          |#----------------------------------*** Schema Definition Started ***----------------------------------
        """.stripMargin.trim

    /**
      * Returns preamble message.
      */
    def authorInfo(): String =
        s"""
           |# ----------------------------------------
           |# Author: Santa Basnet
           |# Company: WiseYak Inc.
           |# Date: ${new Date().toString}
           |# ----------------------------------------
        """.stripMargin.trim

    /**
      * Returns Enum Header.
      */
    def enumTypeHeader(): String =
        """
          |#----------------------------------*** Enumeration Types Section ***----------------------------------
        """.stripMargin.trim

    /**
      * Returns Class Header.
      */
    def classTypeHeader(): String =
        """
          |#--------------------------------------*** Class Types Section ***-------------------------------------
        """.stripMargin.trim

    /**
      * Returns Union Header.
      */
    def unionTypeHeader(): String =
        """
          |#--------------------------------------*** Union Types Section ***-------------------------------------
        """.stripMargin

    /**
      * Closing Message.
      */
    def closingMessage(): String =
        """
          |#----------------------------------*** Schema Definition Completed ***----------------------------------
        """.stripMargin.trim

}
