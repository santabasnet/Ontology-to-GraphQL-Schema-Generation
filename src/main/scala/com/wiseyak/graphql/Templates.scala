package com.wiseyak.graphql

import com.wiseyak.ontology._
import com.wiseyak.search.{FHIRSearch, IndexTemplates}

/**
  * This class is a part of the package com.wiseyak.graphql and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-11.
  */
object Templates {

    /**
      * Formats the max cardinality property syntax of GraphQL.
      *
      * @param propertyName  , given property name.
      * @param superClassName, given super class name.
      * @return maxCardinality definition.
      */
    private def ofMaxCardinality(propertyName: String, superClassName: String): String =
        s"""
           |    $propertyName: $superClassName
        """.stripMargin.trim

    /**
      * Formats the single cardinality property syntax of GraphQL.
      *
      * @param propertyName  , given property name.
      * @param subClassName  , given super class name.
      * @return singleCardinality definition.
      */
    private def ofSingleCardinality(propertyName: String, subClassName: String): String =
        s"""
           |    $propertyName: $subClassName!
        """.stripMargin.trim

    /**
      * Boolean value should not be in the list store.
      *
      * @param className
      * @return formattedClassName
      */
    private def checkBoolean(className: String): String = if (ScalarType.BOOLEAN == className.toLowerCase) className else s"""[$className]"""

    /**
      * Formats the all values property syntax of GraphQL.
      *
      * @param propertyName  , given property name.
      * @param superClassName, given super class name.
      * @return allValuesRestriction definition.
      */
    private def ofAllValuesRestriction(propertyName: String, superClassName: String): String =
        s"""
           |    $propertyName: ${checkBoolean(superClassName)}
        """.stripMargin.trim

    /**
      * Formats the type definition of the GraphQL Type.
      *
      * @param className, given class name.
      * @return typeDefinition
      */
    private def ofTypeName(className: String): String =
        s"""
           |type $className
        """.stripMargin.trim

    /**
      * Formats the enum definition of the GraphQL Type.
      *
      * @param className
      * @return enumDefinition
      */
    def ofEnumName(className: String): String =
        s"""
           |enum $className
        """.stripMargin.trim

    /**
      * Formats the property definition of the class.
      *
      * @param propertyClass, given property class.
      * @return propertyClassDefinition
      */
    def ofPropertyClass(propertyClass: ClassProperties, inverseRelation: InverseRelation): String = {
        s"""
           |${ofTypeName(propertyClass.ontologyClassName())} {
           |    ${propertyList(propertyClass.ontologyClassName(), propertyClass.classInformationWithID, inverseRelation)}
           |}
        """.stripMargin.trim
    }

    /**
      * Formats the options definition of Enumeration class.
      *
      * @param enumDefinition
      * @return enumerationClassGenerated
      */
    def ofEnumerationClass(enumerationProperties: EnumerationProperties): String =
        s"""
           |${ofEnumName(enumerationProperties.className())} {
           |    ${enumOptions(enumerationProperties.options)}
           |}
        """.stripMargin.trim

    def ofInverseRelation(inverseProperty: InverseProperty): String =
        s"""
           |@hasInverse(field: ${inverseProperty.fieldName})
        """.stripMargin.trim

    /**
      * Format the property definition of the given class.
      *
      * @param properties, properties definition.
      * @return formattedProperties
      */
    private def propertyList(className: String, properties: Map[String, Map[String, Any]], inverseRelation: InverseRelation): String = {
        properties.map(entry => {
            /**
              * Identify the sub-class for the given property.
              * (It should handle the reference type too.)
              */
            var subClass = subClassOf(entry._2)
            if (subClass == ScalarType.REFERENCE) {
                val path = List(ClassNameUtility.formatClassName(className), entry._1).mkString(ScalarType.DOT)
                val unionClass = Generator.allUnionDefinitions.get(path)
                if (unionClass.isDefined) subClass = unionClass.get.unionClassName
            }
            /**
              * Build template here.
              */
            val template: String =
                if (hasCardinality(entry._2)) ofSingleCardinality(entry._1, subClass)
                else if (hasMinMaxCardinality(entry._2)) ofMaxCardinality(entry._1, subClass)
                else ofAllValuesRestriction(entry._1, subClass)

            /**
              * No need to compute this relation. We do not find any information to this in the entire
              * FHIR ontology definition.
              */
            //val inverse = inverseRelation.eligible(entry._1, subClassOf(entry._2))
            //if (inverse._1) List(template, inverse._2).mkString(ScalarType.SPACE) else template

            /**
              * Needs to build the indexing information here.
              */
            val searchType = FHIRSearch.searchTypeOf(className, entry._1)
            if (searchType.isEmpty) template
            else {
                val indexPart = IndexTemplates.buildIndexPart(entry._1, subClass, searchType)
                List(template, indexPart).mkString(ScalarType.SPACE).trim
            }

        }).map(ScalarType.TAB + _).mkString(ScalarType.NEW_LINE).trim
    }

    /**
      * Formats the enum options definitions.
      *
      * @param options
      * @return formattedEnumOptions
      */
    private def enumOptions(options: List[String]): String = {
        options.map(ScalarType.TAB + _).mkString(ScalarType.NEW_LINE).trim
    }

    /**
      * Verifies whether the definition contains cardinality information or not.
      *
      * @param attributesDefinition
      * @return true if the definition contains cardinality information.
      */
    def hasCardinality(attributesDefinition: Map[String, Any]): Boolean =
        attributesDefinition.contains(ScalarType.CARDINALITY)

    /**
      * Verifies whether the definition contains min/max cardinality.
      *
      * @param attributesDefinition
      * @return true if the definition contains min/max cardinality.
      */
    def hasMinMaxCardinality(attributesDefinition: Map[String, Any]): Boolean =
        attributesDefinition.contains(ScalarType.MAX_CARDINALITY) || attributesDefinition.contains(ScalarType.MIN_CARDINALITY)

    /**
      * Returns the class name of that property belongs to.
      *
      * @param attributeDefinition
      * @return className of the property.
      */
    def subClassOf(attributeDefinition: Map[String, Any]): String = {
        val className = attributeDefinition.getOrElse(ScalarType.ALL_VALUES_RESTRICTION, attributeDefinition(ScalarType.SOME_VALUES_RESTRICTION)).toString
        //if (className == ScalarType.CLASS_ID) className
        if (ScalarType.isPrimitive(className)) ScalarType.mappingOf(typeName = className)
        else className
    }
}
