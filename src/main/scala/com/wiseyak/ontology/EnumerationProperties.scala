package com.wiseyak.ontology

import com.wiseyak.graphql.{ClassNameUtility, GraphQLField, GraphQLType}
import org.json4s.DefaultFormats
import org.json4s.jackson.Json
import org.xmlet.xsdparser.xsdelements.XsdSimpleType

import scala.collection.mutable


/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-28.
  */
case class EnumerationProperties(localName: String, options: List[String]) {
    def isEmpty: Boolean = options.isEmpty

    def nonEmpty: Boolean = !nonEmpty

    def className(): String = ClassNameUtility.formatClassName(localName)

    def enumType: GraphQLType = GraphQLType.of(ScalarType.ENUM, localName, graphQLFields)

    private def graphQLFields: List[GraphQLField] = options
        .map(option => GraphQLField.of(option))

    override def toString: String = Json(DefaultFormats) writePretty this
}

/**
  * The companion object used for factories.
  */
object EnumerationProperties {

    def of(localName: String, simpleType: XsdSimpleType): EnumerationProperties = {
        val options = mutable.ListBuffer[String]()
        simpleType.getRestriction.getEnumeration.forEach(enumItem => options += ClassNameUtility.formatEnumValue(enumItem.getValue, simpleType))
        EnumerationProperties.of(localName, options.toList)
    }

    def of(localName: String, options: List[String]) = EnumerationProperties(localName, options)
}
