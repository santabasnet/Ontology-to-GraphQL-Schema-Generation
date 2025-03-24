package com.wiseyak.ontology

import com.wiseyak.graphql.{Generator, Templates}

import scala.collection.mutable

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-09-01.
  */
case class SchemaClasses(
                            enumClasses: List[EnumerationProperties],
                            typeClasses: List[ClassProperties],
                            inverseRelation: InverseRelation) {

    /**
      * Enum template classes.
      *
      * @return listOfEnumDefinitions
      */
    private def enumTemplates: List[String] = enumClasses
        .map(enumDefinition => Templates.ofEnumerationClass(enumDefinition))

    /**
      * Union template classes.
      *
      * @return listOfUnionDefinitions
      */
    private def unionTemplates: List[String] = WiseUnionClass.GIVEN_POLICY match {
        case WiseUnionClass.INDIVIDUAL_PATH => Generator.allUnionDefinitions.values.filter(_.associatedClasses.size > 1).map(_.buildDefinition.trim).filter(_.nonEmpty).toList
        case _ => Generator.allUnionDefinitions.values.groupBy(_.unionClassName).map(_._2.head).map(_.buildDefinition.trim).filter(_.nonEmpty).toList
    }

    /**
      * Separates the basic and edges schema.
      */
    private def schemaCategories: Map[String, List[String]] = {
        typeClasses
            .map(classProperties => classProperties.getCategoryName -> classProperties)
            .groupBy(_._1).map(entry => entry._1 -> entry._2.map(_._2))
            .map(entry => entry._1 -> entry._2.map(classDefinition => Templates.ofPropertyClass(classDefinition, inverseRelation)))
    }

    /**
      * Returns the categorized schema.
      *
      * @return mapOfCategorizedSchema
      */
    def splitSchema: Map[String, List[String]] = {
        val result = mutable.Map[String, List[String]]()

        /**
          * Enum classes accumulation.
          */
        result += SchemaClasses.ENUM_SCHEMA -> enumTemplates

        /**
          * Union class definition.
          */
        result += SchemaClasses.UNION_DEFINITION -> unionTemplates

        /**
          * Basic and edges categories.
          */
        result ++= schemaCategories


        result.toMap
    }
}

/**
  * The companion object.
  */
object SchemaClasses {

    /**
      * Full Schema.
      */
    val FULL_SCHEMA = "FULL"

    /**
      * Basic Schema.
      */
    val BASIC_SCHEMA = "BASIC_SCHEMA"

    /**
      * Enum Schema.
      */
    val ENUM_SCHEMA = "ENUM_SCHEMA"

    /**
      * Union Schema.
      */
    val UNION_DEFINITION = "UNION_SCHEMA"

    /**
      * Schema with edges.
      */
    val SCHEMA_WITH_EDGES = "SCHEMA_WITH_EDGES"

    /**
      * Schema classes.
      */
    val schemaTypes: List[String] = List(ENUM_SCHEMA, UNION_DEFINITION, BASIC_SCHEMA, SCHEMA_WITH_EDGES)

    /**
      * Factory.
      */
    def of(enumClasses: List[EnumerationProperties], typeClasses: List[ClassProperties], inverseRelation: InverseRelation) =
        SchemaClasses(enumClasses, typeClasses, inverseRelation)
}
