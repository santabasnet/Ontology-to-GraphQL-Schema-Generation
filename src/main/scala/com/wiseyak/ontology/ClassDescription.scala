package com.wiseyak.ontology

import com.wiseyak.graphql.ClassNameUtility
import org.apache.jena.ontology.{OntClass, Restriction, UnionClass}

import scala.collection.JavaConverters._

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-11.
  */
case class ClassDescription() {

    /**
      * Returns the class description with the attributes.
      *
      * @param ontologyClass, given ontology class.
      * @return mapOfProperty definitions.
      */
    def describe(ontologyClass: OntClass): Map[String, Map[String, Any]] = {
        val result = ontologyClass.listSuperClasses(true).toList.asScala
        if (result.isEmpty) Map[String, Map[String, Any]]()
        else result.map(superClass => makePropertyEntryOf(superClass)).reduce(_ ++ _)
    }

    /**
      * Make property entry of the given super class.
      */
    private def makePropertyEntryOf(superClass: OntClass): Map[String, Map[String, Any]] = {
        if (superClass.isUnionClass) unionClassAttributesOf(superClass.asUnionClass())
        else if (superClass.isRestriction) {
            val restriction = superClass.asRestriction()
            Map[String, Map[String, Any]](restrictionNameOf(restriction) -> buildFromRestriction(restriction))
        }
        else if (!superClass.isAnon) Map[String, Map[String, Any]](ClassNameUtility.extractVariableNameOf(superClass.getLocalName) -> objectResource(superClass))
        else Map[String, Map[String, Any]]()
    }

    /**
      * Generate the attributes for the Union class.
      *
      * @param unionClass, given union class for the property.
      * @return mapOfAttributes of Union type.
      */
    private def unionClassAttributesOf(unionClass: UnionClass): Map[String, Map[String, Any]] =
        unionClass.listOperands().toList.asScala.map(ontologyClass => makePropertyEntryOf(ontologyClass)).reduce(_ ++ _)

    /**
      * Generates the
      *
      * @param superClass, given super class.1
      * @return generalObject
      */
    private def objectResource(superClass: OntClass): Map[String, Any] = Map[String, Any](
        ScalarType.MAX_CARDINALITY -> 1,
        ScalarType.ALL_VALUES_RESTRICTION -> ClassNameUtility.formatClassName(superClass.getLocalName)
    )

    /**
      * Build attribute definition from restriction.
      *
      * @param restriction, given restriction.
      * @return mapOfProperty definition.
      */
    private def buildFromRestriction(restriction: Restriction): Map[String, Any] = {
        var result = Map[String, Any]()
        if (restriction.isCardinalityRestriction)
            result += ScalarType.CARDINALITY -> restriction.asCardinalityRestriction().getCardinality
        if (restriction.isMinCardinalityRestriction)
            result += ScalarType.MIN_CARDINALITY -> restriction.asMinCardinalityRestriction().getMinCardinality
        if (restriction.isMaxCardinalityRestriction)
            result += ScalarType.MAX_CARDINALITY -> restriction.asMaxCardinalityRestriction().getMaxCardinality
        if (restriction.isAllValuesFromRestriction)
            result += ScalarType.ALL_VALUES_RESTRICTION -> ClassNameUtility.formatClassName(restriction.asAllValuesFromRestriction().getAllValuesFrom.getLocalName)
        if (restriction.isSomeValuesFromRestriction)
            result += ScalarType.SOME_VALUES_RESTRICTION -> ClassNameUtility.formatClassName(restriction.asSomeValuesFromRestriction().getSomeValuesFrom.getLocalName)
        if (restriction.isHasValueRestriction)
            result += ScalarType.HAS_VALUE -> ClassNameUtility.formatClassName(restriction.asHasValueRestriction().getHasValue.toString)
        result
    }

    /**
      * Extract property name of the restriction.
      *
      * @param restriction, given restriction.
      * @return restrictionName
      */
    def restrictionNameOf(restriction: Restriction): String = ClassNameUtility.extractVariableNameOf(restriction.getOnProperty.getLocalName)

}

/**
  * The companion object.
  */
object ClassDescription {
    /**
      *
      */
    def describe(ontologyClass: OntClass): Map[String, Map[String, Any]] = ClassDescription().describe(ontologyClass)
    /**
      * Build Property Class.
      */
    def buildPropertyClassOf(ontologyClass: OntClass): ClassProperties = ClassProperties(ontologyClass.getLocalName, ClassDescription.describe(ontologyClass))

}
