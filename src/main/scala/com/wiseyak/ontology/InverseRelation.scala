package com.wiseyak.ontology

import com.wiseyak.conf.Configuration

import scala.util.Try

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-18.
  *
  * * Build the property class triple.
  * * Example:
  * *
  * * Step 1.
  * * C1  --  P1  --  C13
  * * C2  --  P2  --  C10
  * * ...
  * * C10 --  P11 --  C2
  * *
  * * Step 2.
  * * P2  =>  [C2, C10]
  * * P11 =>  [C2, C10]
  * *
  * * Step 3.
  * * P2 and P11 has inverse relation with some criteria.
  *     a. P1   --  C13 should have single cardinality.
  *     b. P11  --  C2 should have exactly one cardinality.
  *     c. P11 has inverse relation with P1 with field P11.
  *
  */
case class InverseRelation(propertyClasses: List[ClassProperties]) {

    /**
      * Defines the association of class name and the property class.
      */
    private val classDefinitionWithProperties: Map[String, ClassProperties] =
        propertyClasses.map(propertyClass => propertyClass.ontologyClassName() -> propertyClass).toMap

    /**
      * Generates the pair of class names.
      *
      * @param outerClass
      * @param innerClass
      * @return classesPair
      */
    private def namePair(outerClass: String, innerClass: String): String =
        List(outerClass, innerClass).sorted.mkString(ScalarType.UNDERSCORE)

    /**
      * Pair Classes.
      */
    private val allClassPairs: Map[String, List[String]] = {
        propertyClasses.flatMap(_.listTriples())
            .filterNot(triple => triple._1 == ScalarType.REFERENCE || triple._3 == ScalarType.REFERENCE)
            .map(entry => (entry._2, namePair(entry._1, entry._3)))
            .groupBy(_._2).map(entry => entry._1 -> entry._2.map(_._1).distinct)
            .filter(_._2.size > 1) // Should have at least two properties in common.
    }

    /**
      * Cyclic relations of the class properties to the parent class.
      *
      * @param sourceClass
      * @param givenClassName
      * @return listOfProperties with classes.
      */
    private def extractCyclicProperties(sourceClass: String, givenClassName: String): List[(String, String)] = Try {
        classDefinitionWithProperties(sourceClass).listOfProperties.filter(_._2 == givenClassName)
    }.getOrElse(List[(String, String)]())

    /**
      * Returns the property cardinality in the given class context.
      *
      * @param className
      * @param propertyName
      * @return definedCardinality
      */
    private def cardinalityOf(className: String, propertyName: String): String =
        classDefinitionWithProperties(className).propertiesCardinality.getOrElse(propertyName, ScalarType.NONE)

    /**
      * Validity criteria for inverse relation extraction.
      *
      * @param source
      * @param target
      * @return true if the inverse relation extraction criteria is valid.
      */
    private def validCardinality(source: String, target: String): Boolean =
        source.equals(ClassProperties.ZERO_OR_MORE) && (target.equals(ClassProperties.EXACTLY_ONE) || target.equals(ClassProperties.ZERO_OR_ONE))

    /**
      * Build Inverse Property relation from the given property definitions in tuples.
      *
      * @param givenProperty
      * @param targetProperty
      * @return inverseProperty relation.
      */
    private def buildInverseProperty(givenProperty: (String, String), targetProperty: (String, String)): InverseProperty = {
        val sourceCardinality = cardinalityOf(targetProperty._2, givenProperty._1)
        val targetCardinality = cardinalityOf(givenProperty._2, targetProperty._1)
        if (!validCardinality(sourceCardinality, targetCardinality)) InverseProperty.empty
        else InverseProperty.of(givenProperty, targetProperty)
    }

    /**
      * Verify the eligibility of inverse relation, and returns the
      * formatted inverse directive.
      *
      * @return (true, valueString) after validation of eligibility.
      */
    def eligible(propertyName: String, instanceOfClass: String): (Boolean, String) = {
        if (!Configuration.needsInverseRelation) (false, ScalarType.EMPTY)
        else if (!classDefinitionWithProperties.contains(instanceOfClass)) (false, ScalarType.EMPTY)
        else {
            val eligibleProperties = classDefinitionWithProperties(instanceOfClass).listOfProperties
                .filter(propertyWithClass => allClassPairs.contains(namePair(instanceOfClass, propertyWithClass._2)))
            val cyclicProperties = eligibleProperties.map(item => {
                val cyclicProperties = extractCyclicProperties(item._2, instanceOfClass).headOption
                if (cyclicProperties.isEmpty) InverseProperty.empty else buildInverseProperty((propertyName, instanceOfClass), item)
            }).filter(_.nonEmpty)
            if (cyclicProperties.isEmpty) (false, ScalarType.EMPTY) else (true, cyclicProperties.head.getDefinition)
        }
    }

    /**
      * Get all the unique classes.
      */
    def uniquePropertyClasses: List[ClassProperties] = propertyClasses
}

/**
  * The companion object for Inverse Relation information.
  */
object InverseRelation {

    /**
      * Property Name type.
      */
    type PropertyName = String

    /**
      * Type definition for field name.
      */
    type FieldName = String

    /**
      * Factory definition.
      */
    def of(propertyClasses: List[ClassProperties]): InverseRelation = InverseRelation(propertyClasses)

    def empty() = InverseRelation(List())
}
