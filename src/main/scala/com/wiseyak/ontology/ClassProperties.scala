package com.wiseyak.ontology

import com.wiseyak.graphql.{ClassNameUtility, GraphQLField, GraphQLType, Templates}
import com.wiseyak.xml.TypeDefinitions
import org.apache.jena.ontology.OntClass
import org.json4s.DefaultFormats
import org.json4s.jackson.Json

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-12.
  */
case class ClassProperties(localName: String, classInformation: Map[String, Map[String, Any]]) {

    /**
      * Extract cardinality information.
      *
      * @param propertyDefinition, map of property definition.
      * @return cardinalityType
      */
    private def cardinalityOf(propertyDefinition: Map[String, Any]): String =
        if (Templates.hasCardinality(propertyDefinition)) ClassProperties.EXACTLY_ONE
        else if (Templates.hasMinMaxCardinality(propertyDefinition)) ClassProperties.ZERO_OR_ONE
        else ClassProperties.ZERO_OR_MORE

    /**
      * Cardinality information.
      */
    val propertiesCardinality: Map[String, String] = classInformation.map(property => property._1 -> cardinalityOf(property._2))

    /**
      * Default ID definition, it also attaches the render information.
      */
    private def defaultID = Map[String, Map[String, Any]](
        ScalarType.PROPERTY_ID -> Map[String, Any](
            ScalarType.CARDINALITY -> 1,
            ScalarType.ALL_VALUES_RESTRICTION -> ScalarType.DEFAULT_TYPE //ScalarType.CLASS_ID
        ) /*,
        ScalarType.PROPERTY_RENDER -> Map[String, Any](
            ScalarType.MAX_CARDINALITY -> 1,
            ScalarType.ALL_VALUES_RESTRICTION -> ScalarType.DEFAULT_TYPE
        )*/
    )


    /**
      * Returns the class information with ID defined.
      * 1. It verifies if there exists any id attributes.
      * 2. It takes care of uid attribute, because it is reserved.
      */
    def classInformationWithID: Map[String, Map[String, Any]] = {
        defaultID ++ classInformation.map(entry => {
            val idType = entry._2.getOrElse(ScalarType.ALL_VALUES_RESTRICTION, ScalarType.EMPTY).toString
            if (idType == ScalarType.CLASS_ID && entry._1 == ScalarType.PROPERTY_ID) TypeDefinitions.escapeReservedWords(entry._1) -> defaultID(ScalarType.PROPERTY_ID)
            else if (idType == ScalarType.CLASS_ID) TypeDefinitions.escapeReservedWords(entry._1) -> (entry._2 + (ScalarType.ALL_VALUES_RESTRICTION -> ScalarType.DEFAULT_TYPE))
            else TypeDefinitions.escapeReservedWords(entry._1) -> entry._2
        })
    }

    /**
      * Class properties triple representation for inverse relation.
      * It avoids the primitive types during listing.
      */
    def listTriples(): List[(String, String, String)] = {
        val sourceName = ClassNameUtility.formatClassName(localName)
        classInformation.map(propertyEntry => propertyEntry._1 -> Templates.subClassOf(propertyEntry._2))
            .filter(propertyEntry => ScalarType.nonDGraphPrimitive(propertyEntry._2))
            .map(propertyEntry => (sourceName, propertyEntry._1, propertyEntry._2))
            .toList
    }

    def allPropertiesTriples(): List[(String, String, String)] = {
        val sourceName = ClassNameUtility.formatClassName(localName)
        classInformation.map(propertyEntry => propertyEntry._1 -> Templates.subClassOf(propertyEntry._2))
            .map(propertyEntry => (sourceName, propertyEntry._1, propertyEntry._2))
            .toList
    }

    /**
      * Category name.
      */
    def getCategoryName: String = {
        val hasEdges = classInformation.find(entry => {
            val typeName = entry._2.getOrElse(ScalarType.ALL_VALUES_RESTRICTION, ScalarType.EMPTY).toString
            ScalarType.nonDGraphPrimitive(typeName)
        })
        if (hasEdges.nonEmpty) SchemaClasses.SCHEMA_WITH_EDGES else SchemaClasses.BASIC_SCHEMA
    }

    /**
      * Returns the name of the given class.
      */
    def ontologyClassName(): String = ClassNameUtility.formatClassName(localName)


    /**
      * Check if it has empty information or not.
      */
    def hasEmptyProperties: Boolean = classInformation.isEmpty

    /**
      * Checks if it has non-empty information or not.
      */
    def hasNonEmptyProperties: Boolean = !hasEmptyProperties

    /**
      * Returns the list of class properties.
      *
      * @return listOfProperties, i.e. List(propertyName, className)
      */
    def listOfProperties: List[(String, String)] = classInformation.toList
        .map(entry => (entry._1, Templates.subClassOf(entry._2)))

    /**
      * Separates basic properties, object properties.
      *
      * @return tupleOfSeparatedProperties
      */
    def separateAttributes: (Map[String, String], Map[String, String]) = {
        val separatedItems = listOfProperties.partition(propertyItem => ScalarType.isDGraphPrimitive(propertyItem._2))
        (separatedItems._1.map(entry => entry._1 -> entry._2).toMap, separatedItems._2.map(entry => entry._1 -> entry._2).toMap)
    }

    /**
      * Returns all the basic type attributes only.
      */
    def basicAttributes: Map[String, String] = separateAttributes._1

    /**
      * Returns the map of class properties with super classes.
      */
    def mapOfPropertyWithClass: Map[String, List[(String, String)]] = Map[String, List[(String, String)]](
        ontologyClassName() -> classInformation.toList.map(propertyEntry => (propertyEntry._1, Templates.subClassOf(propertyEntry._2)))
    )

    /**
      * Builds the GraphQL class type.
      *
      * @return graphQLType
      */
    def classType: GraphQLType = GraphQLType.of(ScalarType.CLASS, ontologyClassName(), graphQLFields)

    private def graphQLFields: List[GraphQLField] = {
        (defaultID ++ classInformation).map(item => {
            val cardinality = cardinalityOf(item._2)
            GraphQLField.of(
                item._1,
                Templates.subClassOf(item._2),
                cardinality == ClassProperties.EXACTLY_ONE,
                cardinality == ClassProperties.ZERO_OR_MORE
            )
        }).toList
    }

    /**
      * Returns the property name and their associated class names.
      *
      * @return listOfTuples containing the property name and their associated class names.
      */
    def referenceElements: List[String] = classInformation
        .map(propertyEntry => propertyEntry._1 -> (Templates.subClassOf(propertyEntry._2), propertyEntry._2))
        .filter(_._2._1 == "Reference").keySet
        .map(List(ontologyClassName(), _).mkString(".")).toList


    /**
      * Json String representation.
      */
    override def toString: String = Json(DefaultFormats) writePretty this
}

/**
  * The companion object that defines helper for the property definitions.
  */
object ClassProperties {

    /**
      * Cardinality literals: exactly one.
      */
    val EXACTLY_ONE = "EXACTLY_ONE"

    /**
      * Cardinality literals: zero or one.
      */
    val ZERO_OR_ONE = "ZERO_OR_ONE"

    /**
      * Cardinality literals: zero or more.
      */
    val ZERO_OR_MORE = "ZERO_OR_MORE"

    /**
      * Factory method.
      */
    def of(localName: String, classInformation: Map[String, Map[String, Any]]) = ClassProperties(localName, classInformation)

    /**
      * Factory method for ontoClass.
      */
    def of(ontClass: OntClass) = ClassProperties(ontClass.getLocalName, ClassDescription.describe(ontClass))

}