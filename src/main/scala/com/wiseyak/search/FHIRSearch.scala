package com.wiseyak.search

import com.wiseyak.graphql.Generator
import com.wiseyak.ontology.ScalarType

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-01-06.
  */
object FHIRSearch {

    /**
      * Key generation for accessing the indexing information.
      */
    private def indexKey(typeName: String, attributeName: String) =
        List(typeName, ParameterDefinition.HYPHEN, attributeName).mkString

    /**
      * Index key for search template.
      */
    private def indexOfSearchTemplate(attributeName: String, innerType: String, searchType: String): String =
        List(attributeName.toUpperCase, innerType.toUpperCase, searchType.toUpperCase).mkString(ParameterDefinition.UNDERSCORE)

    /**
      * Represents the inner type name from the class attributes(ontology definition)
      * Example:
      * type A {
      * a of Type B
      * }
      * Map of A-a -> B.
      **/
    private val innerTypeAttributeIndex = Generator
        .uniqueClassProperties
        .map(classItem => classItem.localName -> classItem.listOfProperties)
        .flatMap(entry => entry._2.map(property => (entry._1, property._1, property._2)))
        .map(entry => indexKey(entry._1, entry._2) -> entry._3)
        .toMap

    /**
      * All the search parameters.
      */
    private val allParameterEntries: List[SearchParameter] = ParameterDefinition
        .buildFromConfiguration.entries
        .map(ResourceEntry.buildWith)
        .map(SearchParameter.buildWith)

    /**
      * All the path expressions, translate it to only attribute name and the type name.
      * 1. Slot.status no need to translate.
      * 2. Specimen.collection.bodySite, need to translate to the collection type name of collection with bodySite.
      * >> SpecimenCollection.bodySite
      * 3. (ResearchDefinition.useContext.value as Quantity), need to translate to the usage context value.
      * >> UsageContext.valueQuantity
      * 4. Currently reference types are ignored to index.
      */
    private val allReducedParameterEntries: List[SearchParameter] = {
        allParameterEntries.filterNot(_.isReference).map(_.reducedSearchParameter)
    }

    private val allResolutionReferences: List[IdPathExpression] = allParameterEntries
        .filter(_.isReference).flatMap(_.toIdPathExpressions)

    /**
      * Path with search parameter type.
      * MolecularSequenceReferenceSeq.referenceSeqId -> token.
      */
    private val pathWithParameterType: Map[String, String] = {
        allReducedParameterEntries.flatMap(searchParameter => {
            searchParameter.pathExpressions.map(path => (path, searchParameter.parameterType))
        }).map(entry => entry._1 -> entry._2).toMap
    }

    /**
      * Type name and attributes indexing information.
      * Build from class attributes, ontology.
      */
    private val typeAttributeIndex: Map[String, List[String]] = Generator
        .uniqueClassProperties.map(_.basicAttributes)
        .foldLeft(Map[String, List[String]]())((result, item) => {
            val itemWithList: Map[String, List[String]] = item.map(entry => entry._1 -> List(entry._2))
            (result.keySet ++ item.keySet).map(key => {
                val lValues = result.getOrElse(key, ParameterDefinition.EMPTY_LIST)
                val rValue = itemWithList.getOrElse(key, ParameterDefinition.EMPTY_LIST)
                key -> (lValues ++ rValue).distinct
            }).toMap
        })

    /**
      * Search templates for all the search parameters.
      */
    private val searchTemplates: Map[String, String] = {
        //val allPaths = allReducedParameterEntries.flatMap(_.pathExpressions)

        val pathWithSearchParameter = allReducedParameterEntries.flatMap(searchParameter => {
            val basicPaths = searchParameter.pathExpressions.filter(hasBasicInnerType)
            val pathsWithInnerType = basicPaths.map(path => {
                val typeAttribute = path.split("\\.").toList
                val innerType = innerTypeOf(typeAttribute.head, typeAttribute.last)
                (path, typeAttribute.last, innerType)
            })
            pathsWithInnerType.map(entry => (entry._1, entry._2, entry._3, searchParameter))
        })

        val result = pathWithSearchParameter.map(entry => {
            val key = indexOfSearchTemplate(entry._2, entry._3, entry._4.parameterType)
            val value = IndexTemplates.buildIndexPart(entry._2, entry._3, entry._4.parameterType)
            (key, value)
        }).groupBy(_._1).map(_._2.head)

        result
    }

    /**
      * Returns the search index part during schema building.
      *
      * @param attributeName, given attribute name.
      * @param innerType    , inner type i.e. the type of the attribute.
      * @param searchType   , type name defined in the search definition of the given property name.
      * @return searchIndexPart
      */
    def searchTemplateOf(attributeName: String, innerType: String, searchType: String): String =
        searchTemplates.getOrElse(indexOfSearchTemplate(attributeName, innerType, searchType), ParameterDefinition.EMPTY)

    /**
      * Extracts the search type definition of the given class and its associated attribute name.
      *
      * @param outerType    , the class name in which the attribute is associated.
      * @param attributeName, given attribute name.
      * @return searchTypeDefinition.
      */
    def searchTypeOf(outerType: String, attributeName: String): String = {
        if (attributeName == ScalarType.ID) attributeName
        else {
            val path = List(outerType, attributeName).mkString(".")
            pathWithParameterType.getOrElse(path, ParameterDefinition.EMPTY)
        }
    }

    /**
      * Returns
      *
      * @param attributeName
      * @return generatedIndexParameter
      */
    def indexParameter(attributeName: String): List[String] =
        typeAttributeIndex.getOrElse(attributeName, ParameterDefinition.EMPTY_LIST)

    /**
      * Returns the inner type name from the class attributes(ontology definition)
      * Example:
      * type A {
      * a of Type B
      * }
      * if A and a is provided, it returns B.
      *
      * @param typeName
      * @param attributeName
      * @return innerTypeName
      */
    def innerTypeOf(typeName: String, attributeName: String): String =
        innerTypeAttributeIndex.getOrElse(indexKey(typeName, attributeName), ParameterDefinition.EMPTY)

    private def hasInnerType(path: String): Boolean = {
        val typeAttribute = path.split("\\.").toList
        val innerType = innerTypeOf(typeAttribute.head, typeAttribute.last)
        innerType.trim.nonEmpty
    }

    private def hasBasicInnerType(path: String): Boolean = {
        val typeAttribute = path.split("\\.").toList
        val innerType = innerTypeOf(typeAttribute.head, typeAttribute.last)
        ScalarType.isDGraphPrimitive(innerType)
    }

    /**
      * Serves all the search parameters.
      *
      * @return listOfSearchParameters
      */
    def allParameters(): List[SearchParameter] = allParameterEntries

    /**
      * Serves all the reference search parameters.
      *
      * @return listOfSearchParameters
      */
    def allReferenceParameters(): List[SearchParameter] = allReducedParameterEntries

    /**
      * Returns all the collected resolution class for the reference properties.
      *
      * @return mapOfIdWithResolutionClass
      */
    def collectResolutionClasses: Map[String, PathExpression] = {
        /**
          * Find the path expression that has only resolution argument.
          */
        allResolutionReferences
            .filter(_.pathExpression.hasResolvePredicate)
            .map(idExpr => idExpr.id -> idExpr.pathExpression).toMap
    }
}
