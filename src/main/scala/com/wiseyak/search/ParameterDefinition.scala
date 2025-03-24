package com.wiseyak.search

import com.wiseyak.conf.Configuration
import org.json4s.DefaultFormats
import org.json4s.jackson.{Json, JsonMethods}

import scala.util.Try

/**
  * This class is a part of the package com.wiseyak.search and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-01-07.
  */
case class ParameterDefinition(definition: Map[String, Any]) {

    def isEmpty: Boolean = definition.isEmpty

    def entries: List[Map[String, Any]] =
        if (isEmpty) List[Map[String, Any]]()
        else definition.getOrElse(ParameterDefinition.ENTRY, List[Map[String, Any]]()).asInstanceOf[List[Map[String, Any]]]

}

/**
  * Factory definition for the ParameterDefinition class.
  */
object ParameterDefinition {

    /**
      * Attribute literals.
      */
    val EMPTY: String = ""
    val EMPTY_LIST: List[String] = List[String]()
    val HYPHEN: String = "-"
    val UNDERSCORE: String = "_"
    val ENTRY: String = "entry"
    val NAME: String = "name"
    val ID: String = "id"
    val DESCRIPTION: String = "description"
    val TYPE = "type"
    val CODE = "code"
    val FULL_URL: String = "fullUrl"
    val RESOURCE: String = "resource"
    val EXPRESSION: String = "expression"

    /**
      * Parameter type values.
      */
    val COMPOSITE = "composite"
    val TOKEN = "token"
    val URI = "uri"
    val REFERENCE = "reference"
    val STRING = "string"
    val DATE = "date"
    val QUANTITY = "quantity"
    val SPECIAL = "special"

    implicit val formats: DefaultFormats.type = DefaultFormats

    def buildWith(jsonData: String): ParameterDefinition = ParameterDefinition(
        Try(JsonMethods.parse(jsonData).extract[Map[String, Any]]).getOrElse(Map[String, Any]())
    )

    def buildFromConfiguration: ParameterDefinition = buildWith(Configuration.searchParameters)
}

/**
  * Represents resource entry for further processing.
  *
  * @param fullUrl
  * @param resource
  */
case class ResourceEntry(fullUrl: String, resource: Map[String, Any]) {

    def expression: String = Try(resource(ParameterDefinition.EXPRESSION).toString).getOrElse(ParameterDefinition.EMPTY)

}

/**
  * Factory implementation for resource entry.
  */
object ResourceEntry {

    def buildWith(fullUrl: String, resource: Map[String, Any]): ResourceEntry =
        ResourceEntry(fullUrl, resource)

    def buildWith(resourceEntry: Map[String, Any]): ResourceEntry =
        buildWith(resourceEntry(ParameterDefinition.FULL_URL).toString, resourceEntry(ParameterDefinition.RESOURCE).asInstanceOf[Map[String, Any]])

}

/**
  * Search Parameter Description.
  *
  * @param parameterName
  * @param parameterType
  * @param searchId
  * @param description
  * @param pathExpression
  */
case class SearchParameter(parameterName: String, parameterType: String, searchId: String, pathExpressions: List[String], resourceCode: String) {

    def isComposite: Boolean = this.parameterType == ParameterDefinition.COMPOSITE

    def isString: Boolean = this.parameterType == ParameterDefinition.STRING

    def isReference: Boolean = this.parameterType == ParameterDefinition.REFERENCE

    def isToken: Boolean = this.parameterType == ParameterDefinition.TOKEN

    def isURI: Boolean = this.parameterType == ParameterDefinition.URI

    def isDate: Boolean = this.parameterType == ParameterDefinition.DATE

    def isQuantity: Boolean = this.parameterType == ParameterDefinition.QUANTITY

    def isSpecial: Boolean = this.parameterType == ParameterDefinition.SPECIAL

    def toIdPathExpressions: List[IdPathExpression] = this.pathExpressions
        .map(expression => PathExpression.of(expression))
        .map(pathExpression => IdPathExpression.from(searchId, pathExpression))

    def reducedSearchParameter: SearchParameter = SearchParameter.buildWith(parameterName, parameterType, searchId, newPathExpressions, resourceCode)

    /*def resolutionSearchParameter: SearchParameter = {
        SearchParameter.buildWith(parameterName, parameterType, searchId, resolutionExpressions, resourceCode)
    }*/

    private def newPathExpressions: List[String] = this.pathExpressions
        .filter(_.trim.nonEmpty).map(SearchParameter.reducedExpression)
        .filterNot(_.startsWith("."))

    private def resolutionExpressions: List[String] = this.pathExpressions
        .map(_.trim).filter(_.nonEmpty).map(SearchParameter.extractResolutionType)
        .filter(_.nonEmpty).map(_.get).distinct

    override def toString: String = Json(DefaultFormats) writePretty this
}

/**
  * Companion object for search parameter to define factory methods.
  */
object SearchParameter {

    def buildWith(resource: Map[String, Any]): SearchParameter = {
        val parameterName: String = Try(resource(ParameterDefinition.NAME).toString).getOrElse(ParameterDefinition.EMPTY)
        val parameterType: String = Try(resource(ParameterDefinition.TYPE).toString).getOrElse(ParameterDefinition.EMPTY)
        val searchId: String = Try(resource(ParameterDefinition.ID).toString).getOrElse(ParameterDefinition.EMPTY)
        val pathExpressions: List[String] = Try(resource(ParameterDefinition.EXPRESSION).toString)
            .getOrElse(ParameterDefinition.EMPTY).split("\\|").map(_.trim).toList
        val resourceCode: String = Try(resource(ParameterDefinition.CODE).toString).getOrElse(ParameterDefinition.EMPTY)
        SearchParameter(parameterName, parameterType, searchId, pathExpressions, resourceCode)
    }

    def buildWith(resourceEntry: ResourceEntry): SearchParameter = buildWith(resourceEntry.resource)

    def buildWith(parameterName: String, parameterType: String, searchId: String, pathExpressions: List[String], resourceCode: String): SearchParameter =
        SearchParameter(parameterName, parameterType, searchId, pathExpressions, resourceCode)

    def reducedExpression(expression: String): String = {
        if (nonEligible(expression)) expression
        else if (hasWhereCondition(expression)) {
            val reduced = expression.slice(0, expression.indexOf(".where"))
            reducedExpression(reduced)
        } else if (isRepresentedWithAnotherType(expression)) {
            val newExpression = expression.replace("(", "").replace(")", "")
            val separatedWithAs: List[String] = newExpression.split("as").toList.map(_.trim)
            val firstParts: List[String] = separatedWithAs.head.split("\\.").toList
            val lastPart: String = List(firstParts.last, separatedWithAs.last).mkString
            reducedExpression((firstParts.dropRight(1) :+ lastPart).mkString("."))
        } else if (hasMultipleInnerTypes(expression)) {
            val typeTokens = expression.split("\\.").toList
            val reduced = List(convertInnerType(typeTokens.take(2)), typeTokens.drop(2).mkString(".")).mkString(".")
            reducedExpression(reduced)
        } else expression
    }

    def extractResolutionType(expression: String): Option[String] = PathExpression.of(expression).targetPath

    private def convertInnerType(typeAttribute: List[String]): String = FHIRSearch.innerTypeOf(typeAttribute.head, typeAttribute.last)

    private def isRepresentedWithAnotherType(expression: String): Boolean = {
        val trimmedExpression = expression.trim
        trimmedExpression.startsWith("(") && trimmedExpression.endsWith(")") && expression.contains("as")
    }

    private def hasWhereCondition(expression: String): Boolean = expression.contains("where")

    private def nonEligible(expression: String): Boolean = expression.isEmpty || expression.split("\\.").length <= 1

    private def hasMultipleInnerTypes(expression: String): Boolean = expression.split("\\.").length > 2

}