package com.wiseyak.ontology

import com.wiseyak.graphql.ClassNameUtility

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project 1.1.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-03-15.
  */
case class WiseUnionClass(propertyPath: String, associatedClasses: List[String], resolution: String = "") {

    /**
      * Verifies if the union class has empty association.
      *
      * @return true for empty associations.
      */
    def isEmpty: Boolean = associatedClasses.isEmpty

    /**
      * Check if valid union class.
      */
    def isValidUnionClass: Boolean = associatedClasses.distinct.size > 1

    /**
      * Check if the current instance has only single reference name.
      */
    def hasSingleReference: Boolean = associatedClasses.distinct.size == 1

    private def concatenatedNames: String = associatedClasses.distinct.map(ClassNameUtility.formatClassName).sorted.mkString("_")

    private def buildHashCode: String = List(WiseUnionClass.NAME_PREFIX, concatenatedNames.hashCode.toString.replace("-", "_")).mkString

    private def classFromPropertyPath: String = propertyPath.split("\\.").map(_.capitalize).mkString

    /**
      * Generates the union class name based on the given policy.
      *
      * @return
      */
    private def buildUnionClassName: String = WiseUnionClass.GIVEN_POLICY match {
        case WiseUnionClass.HASHING => buildHashCode
        case WiseUnionClass.INDIVIDUAL_PATH => classFromPropertyPath
        case _ => concatenatedNames
    }

    /**
      * Build union class name.
      * for eg.: A_B_C
      */
    def unionClassName: String =
        if (isValidUnionClass) ClassNameUtility.formatClassName(buildUnionClassName)
        else if (hasSingleReference) ClassNameUtility.formatClassName(associatedClasses.head)
        else ScalarType.EMPTY

    /**
      * Build union class arguments.
      * for eg.: A | B | C
      */
    def unionArguments: String =
        if (isValidUnionClass) associatedClasses.distinct.map(ClassNameUtility.formatClassName).sorted.mkString(" | ")
        else ClassNameUtility.formatClassName(associatedClasses.head)

    /**
      * Build union class definition.
      */
    def buildDefinition: String = if (isValidUnionClass) WiseUnionClass.buildDefinition(unionClassName, unionArguments) else ScalarType.EMPTY

}

/**
  * The companion objects for factory utilities.
  */
object WiseUnionClass {

    // -----------------------------------------------------
    /**
      * Naming Policy.
      */
    val NAME_PREFIX = "ReferenceWith"
    val HASHING: String = "HASHING"
    val CONCATENATION: String = "CONCATENATION"
    val PARTIAL_MATCHING: String = "PARTIAL_MATCHING"
    val INDIVIDUAL_PATH: String = "INDIVIDUAL_PATH"

    val GIVEN_POLICY: String = INDIVIDUAL_PATH
    // -----------------------------------------------------

    /**
      * Union class definition template.
      */
    def buildDefinition(unionName: String, unionClasses: String): String =
        s"""
           | union $unionName = $unionClasses
        """.stripMargin

    /**
      * Factory for union class definition.
      *
      * @param propertyPath
      * @param associatedClasses
      * @return unionClass
      */
    def of(propertyPath: String, associatedClasses: List[String]): WiseUnionClass =
        WiseUnionClass(propertyPath, associatedClasses.map(ClassNameUtility.formatClassName))

    /**
      * Factory for union class definition
      * @param propertyPath
      * @param associatedClasses
      * @param resolution
      * @return instanceOfWiseUnionClass
      */
    def withFormattedName(propertyPath: String, associatedClasses: List[String], resolution: String): WiseUnionClass =
        WiseUnionClass(propertyPath, associatedClasses.map(ClassNameUtility.formatClassName), ClassNameUtility.formatClassName(resolution))

    /**
      * Factory for union class definition with union resolution.
      * @param propertyPath
      * @param associatedClasses
      * @param resolution
      * @return instanceOfWiseUnionClass
      */
    def of(propertyPath: String, associatedClasses: List[String], resolution: Option[String]): WiseUnionClass =
        WiseUnionClass.withFormattedName(propertyPath, associatedClasses, resolution.getOrElse(""))

    /**
      * Generates all the union class for the given the map of association.
      *
      * @param allAssociations, map of all associations extracted.
      * @return listOfUnionClass
      */
    def batchesOf(allAssociations: Map[String, (List[String], Option[String])]): List[WiseUnionClass] =
        allAssociations.map(entry => WiseUnionClass.of(entry._1, entry._2._1, entry._2._2)).toList
}
