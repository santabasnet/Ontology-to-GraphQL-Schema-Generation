package com.wiseyak.ontology

import com.wiseyak.graphql.Templates

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-21.
  */
case class InverseProperty(sourceClass: String, property: String, targetClass: String, fieldName: String) {

    /**
      * Extract inverse relation.
      */
    def getDefinition: String = Templates.ofInverseRelation(this)

    /**
      * Check if empty property.
      */
    def isEmpty: Boolean = sourceClass.isEmpty || targetClass.isEmpty || property.isEmpty || fieldName.isEmpty

    /**
      * Check if non-empty property.
      */
    def nonEmpty: Boolean = !isEmpty

}

/**
  * Factory definition.
  */
object InverseProperty {

    def of(givenProperty: (String, String), targetProperty: (String, String)): InverseProperty =
        InverseProperty.of(givenProperty._2, givenProperty._1, targetProperty._2, targetProperty._1)

    def of(sourceClass: String, property: String, targetClass: String, fieldName: String): InverseProperty =
        InverseProperty(sourceClass: String, property: String, targetClass: String, fieldName: String)

    def empty: InverseProperty = InverseProperty(ScalarType.EMPTY, ScalarType.EMPTY, ScalarType.EMPTY, ScalarType.EMPTY)
}
