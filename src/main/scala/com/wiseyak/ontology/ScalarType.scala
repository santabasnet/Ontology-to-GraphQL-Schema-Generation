package com.wiseyak.ontology

import com.wiseyak.conf.Configuration
import org.json4s.DefaultFormats

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-10.
  */
object ScalarType {
    /**
      * Zero Depth.
      */
    val ZERO_DEPTH = 0

    /**
      * Empty String literals.
      */
    val EMPTY: String = ""

    /**
      * Space String literals.
      */
    val SPACE: String = " "

    /**
      * DOT string literal.
      */
    val DOT = "."

    /**
      * Json formats.
      */
    implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

    /**
      * Default type.
      */
    val DEFAULT_TYPE: String = "string"

    /**
      * id literal.
      */
    val ID: String = "id"

    /**
      * Boolean Literals.
      */
    val BOOLEAN: String = "boolean"

    /**
      * String literal for cardinality.
      */
    val CARDINALITY: String = "cardinality"

    /**
      * String literal for max_cardinality.
      */
    val MAX_CARDINALITY: String = "maxCardinality"

    /**
      * String literal for min_cardinality.
      */
    val MIN_CARDINALITY: String = "minCardinality"

    /**
      * String literal for all values restriction.
      */
    val ALL_VALUES_RESTRICTION: String = "allValuesRestriction"

    /**
      * String literal for some values restriction.
      */
    val SOME_VALUES_RESTRICTION: String = "someValuesRestriction"

    /**
      * String literal for has value.
      */
    val HAS_VALUE: String = "hasValue"

    /**
      * Filter literals.
      */
    val FILTER: String = "Filter"

    /**
      * String literal for id.
      */
    val PROPERTY_ID: String = "id"

    /**
      * String literal for render information.
      */
    val PROPERTY_RENDER: String = "render"

    /**
      * UID.
      */
    val UID = "uid"

    /**
      * String literal for ID class.
      */
    val CLASS_ID: String = "ID"

    /**
      * Reference literal.
      */
    val REFERENCE = "Reference"

    /**
      * None literals.
      */
    val NONE: String = "NONE"

    /**
      * Name property.
      */
    val NAME: String = "name"

    val VALUE: String = "value"

    /**
      * Min Occurs.
      */
    val MIN_OCCURS: String = "minOccurs"

    /**
      * Max Occurs.
      */
    val MAX_OCCURS: String = "maxOccurs"

    /**
      * Type literals.
      */
    val TYPE: String = "type"

    /**
      * Unbounded Literals.
      */
    val UNBOUNDED: String = "unbounded"

    /**
      * Primitive Literals.
      */
    val PRIMITIVE: String = "-primitive"

    val LIST: String = "-list"

    /**
      * GraphQL Comment Prefix.
      */
    val GRAPHQL_COMMENT_PREFIX = "#"

    /**
      * New line literal.
      */
    val NEW_LINE: String = System.lineSeparator()

    /**
      * Tab literal.
      */
    val TAB = "\t"

    /**
      * Underscore.
      */
    val UNDERSCORE: String = "_"

    val HYPHEN: String = "-"

    val SLASH: String = "/"

    /**
      * Model file format.
      */
    val TTL_FORMAT: String = "TTL"

    /**
      * Model file JSON format.
      */
    val JSON_FORMAT: String = "JSON"

    /**
      * Model file XML format.
      */
    val XML_FORMAT: String = "XML"

    /**
      * Model file XSD format.
      */
    val XSD_FORMAT: String = "XSD"

    val ENUM: String = "ENUM"

    val CLASS: String = "CLASS"

    /**
      * All scalar type definition equivalent to DGraph.
      */
    private val definitions: Map[String, String] = Configuration.basicTypeDefinitions

    /**
      * DGraph Primitive types.
      */
    private val dgraphPrimitiveTypes: List[String] = (definitions.values.toList :+ CLASS_ID).sorted.distinct

    /**
      * Get the type conversion.
      */
    def mappingOf(typeName: String): String = definitions.getOrElse(typeName.toLowerCase, DEFAULT_TYPE)

    /**
      * Checks whether the given name is of primitive type or not.
      */
    def isPrimitive(typeName: String): Boolean = definitions.contains(typeName.toLowerCase)

    /**
      * Check whether the given name is of non-primitive type.
      */
    def nonPrimitive(typeName: String): Boolean = !isPrimitive(typeName)

    /**
      * Checks if the given type name is of GraphQL primitive type.
      */
    def isDGraphPrimitive(typeName: String): Boolean = dgraphPrimitiveTypes.contains(typeName) //dgraphPrimitiveTypes.contains(Character.toLowerCase(typeName.head) + typeName.tail)

    /**
      * Check if the given type name is not in DGraph Primitive types.
      */
    def nonDGraphPrimitive(typeName: String): Boolean = !isDGraphPrimitive(typeName)

}
