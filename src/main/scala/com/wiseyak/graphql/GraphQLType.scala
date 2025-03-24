package com.wiseyak.graphql

import com.wiseyak.ontology.WiseUnionClass
import org.json4s.DefaultFormats
import org.json4s.jackson.Json

/**
  * This class is a part of the package com.wiseyak.graphql and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-10-05.
  */
case class GraphQLType(className: String, typeName: String, fields: List[GraphQLField] = List[GraphQLField]()) {
    /**
      * Json Serialization.
      *
      * @return jsonClass
      */
    override def toString: String = Json(DefaultFormats) writePretty this
}

/**
  * The companion object for graphql type.
  */
object GraphQLType {
    /**
      * A factory utility.
      *
      * @param className
      * @param typeName
      * @return graphQLType
      */
    def of(className: String, typeName: String) = GraphQLType(className = className, typeName = typeName)

    /**
      * A factory utility.
      *
      * @param className
      * @param typeName
      * @param fields
      * @return graphQLType
      */
    def of(className: String, typeName: String, fields: List[GraphQLField]) = GraphQLType(className = className, typeName = typeName, fields = fields)
}


case class GraphQLField(fieldName: String, fieldType: String = "None", notNull: Boolean = false, collection: Boolean = false) {

    /**
      * Json Serialization.
      *
      * @return jsonClass
      */
    override def toString: String = Json(DefaultFormats) writePretty this
}

/**
  * The factory utility for the GraphQL Field.
  */
object GraphQLField {
    /**
      * A factory utility.
      *
      * @param fieldName
      * @param fieldType
      * @return graphQLField
      */
    def of(fieldName: String) = GraphQLField(fieldName = fieldName)


    /**
      * An overloaded factory utility.
      *
      * @param fieldName
      * @param fieldType
      * @param notNull
      * @param collection
      * @return graphQLField
      */
    def of(fieldName: String, fieldType: String, notNull: Boolean, collection: Boolean) =
        GraphQLField(fieldName = fieldName, fieldType = fieldType, notNull = notNull, collection = collection)
}

case class GeneratedTypes(allTypes: Map[String, GraphQLType], basicTypes: List[String], allUnionDefinitions: Map[String, WiseUnionClass]) {
    /**
      * Add a given type to the instance.
      *
      * @param graphQLType
      * @return generatedTypes
      */
    def add(graphQLType: GraphQLType): GeneratedTypes = GeneratedTypes.of(allTypes + (graphQLType.className -> graphQLType), basicTypes, allUnionDefinitions)

    /**
      * Json Serialization.
      *
      * @return jsonClass
      */
    override def toString: String = Json(DefaultFormats) writePretty this
}

/**
  * The companion object.
  */
object GeneratedTypes {
    /**
      * An empty instance.
      *
      * @return empty instance.
      */
    def empty = GeneratedTypes(Map[String, GraphQLType](), List[String](), Map[String, WiseUnionClass]())

    /**
      * A factory utility.
      *
      * @param allTypes
      * @return generatedTypes
      */
    def of(allTypes: Map[String, GraphQLType], basicTypes: List[String], allUnionDefinitions: Map[String, WiseUnionClass]) = GeneratedTypes(allTypes, basicTypes, allUnionDefinitions)

}