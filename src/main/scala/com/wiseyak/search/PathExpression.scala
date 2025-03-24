package com.wiseyak.search

/**
  * This class is a part of the package com.wiseyak.search and the package
  * is a part of the project 1.1.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-07-14.
  */

/**
  * Represents token while parsing.
  *
  * @param typeName
  * @param typeValue
  */
case class TokenExpression(typeName: String, typeValue: Option[String] = None) {
    def isEmpty: Boolean = typeValue.isEmpty

    override def toString: String = typeName + " => " + typeValue
}

object TokenExpression {
    def emptyOf(typeName: String): TokenExpression = TokenExpression(typeName)

    def of(typeName: String, typeValue: Option[String]) = TokenExpression(typeName, typeValue)
}

/**
  * This class defines the parsing strategies and path resolution for the given path expression.
  * Available Tokens: MAIN_PATH, CONDITION, ARGUMENT_BEGIN, ARGUMENT_END, PREDICATE, OPERATOR, TARGET
  *
  * @param givenExpression, given path expression that needs to be parsed to generate the full path operation.
  */
case class PathExpression(givenExpression: String) {
    /**
      * Parse the given expression.
      */
    private val parsedExpression: Map[String, TokenExpression] = parseExpression()

    /**
      * Parse MAIN_PATH and forwards the operation to parse the rest one.
      *
      * @return mapOfParsedExpression.
      */
    private def parseExpression(): Map[String, TokenExpression] = {
        this.givenExpression.split(PathExpression.WHERE).map(_.trim).filter(_.nonEmpty).toList match {
            case head :: Nil => buildMainPath(head)
            case head :: tail => (buildMainPath(head) + PathExpression.conditionPath()) ++ parseCondition(tail)
            case Nil => PathExpression.emptyTokens()
        }
    }

    /**
      * Supports for the following structures:
      * 1. val example1 = "(Questionnaire.useContext.value as Quantity)"
      * 2. val example3 = "(Observation.value as CodeableConcept).text"
      * 3. val example6 = "OrganizationAffiliation.identifier"
      *
      * @param pathValue
      * @return mapOfParsedExpression
      */
    private def buildMainPath(pathValue: String): Map[String, TokenExpression] = {
        if (pathValue.startsWith("(")) {
            val lastIndex = pathValue.lastIndexOf(")") + 1
            val otherType = pathValue.substring(0, lastIndex).replace("(", "").replace(")", "").split("as").map(_.trim).toList match {
                case path :: target :: Nil => buildAsPath(path, target)
                case _ => PathExpression.emptyTokens() + PathExpression.mainPath(pathValue)
            }
            val trailPath = pathValue.substring(lastIndex).split("\\.").filter(_.nonEmpty).mkString(".")
            otherType + PathExpression.trailPath(trailPath)
        } else PathExpression.emptyTokens() + PathExpression.mainPath(pathValue)
    }

    private def buildAsPath(path: String, target: String): Map[String, TokenExpression] = {
        PathExpression.emptyTokens() + PathExpression.mainPath(path) + PathExpression.operatorPath("as") + PathExpression.targetPath(target, capitalize = true)
    }

    private def parseCondition(tailItems: List[String]): Map[String, TokenExpression] = tailItems match {
        case head :: Nil => parseArguments(head)
        case _ => Map[String, TokenExpression]()
    }

    private def parseArguments(arguments: String): Map[String, TokenExpression] = arguments
        .split("\\.").map(_.trim).filter(_.nonEmpty).toList match {
        case head :: Nil => argumentTokens(head)
        case head :: tail => argumentTokens(head) + PathExpression.trailPath(tail.mkString("."))
        case _ => Map[String, TokenExpression]()
    }


    private def argumentTokens(arguments: String): Map[String, TokenExpression] = arguments
        .replaceAll("\\(", "").replaceAll("\\)", "").split(Array(' ', '=')).map(_.trim).filter(_.nonEmpty).toList match {
        case predicate :: operator :: target :: Nil => buildArguments(predicate, operator, target)
        case predicate :: target :: Nil => buildArguments(predicate, "=", target)
        case _ => Map[String, TokenExpression]()
    }

    private def buildArguments(predicate: String, operator: String, target: String) = Map[String, TokenExpression](
        PathExpression.predicatePath(predicate),
        PathExpression.operatorPath(operator),
        PathExpression.targetPath(target.replaceAll("'", ""))
    )

    private def isAsExpression: Boolean = operatorPath.getOrElse(PathExpression.OPERATOR, "") == "as"

    private def isWhereExpression: Boolean = conditionPath.getOrElse(PathExpression.OPERATOR, "") == "where"

    def hasResolvePredicate: Boolean = predicatePath.getOrElse(PathExpression.PREDICATE, "") == "resolve"

    private def isEnum: Boolean = conditionPath.isDefined && operatorPath.getOrElse("") == "=" && Character.isLowerCase(targetPath.getOrElse("0")(0))

    def mainPath: Option[String] = parsedExpression(PathExpression.MAIN_PATH).typeValue

    def trailPath: Option[String] = parsedExpression(PathExpression.TRAIL_PATH).typeValue

    def targetPath: Option[String] = parsedExpression(PathExpression.TARGET).typeValue

    def operatorPath: Option[String] = parsedInformation(PathExpression.OPERATOR).typeValue

    def predicatePath: Option[String] = parsedExpression(PathExpression.PREDICATE).typeValue

    def conditionPath: Option[String] = parsedExpression(PathExpression.CONDITION).typeValue

    /**
      * Returns the resolution type of the given reference attribute.
      */
    def referenceResolution: Option[String] = {
        val reference = parsedExpression.get(PathExpression.TARGET).map(_.typeValue).get
        if (isEnum) reference.map(_.capitalize) else reference
    }

    /**
      * Returns all the parsed information.
      */
    def parsedInformation: Map[String, TokenExpression] = parsedExpression

    /**
      * Returns the path expression.
      */
    def rebuildPathExpression: String = {
        if (isAsExpression && trailPath.isDefined) List(mainPath.get, targetPath.get, ".", trailPath.getOrElse("")).mkString
        else if (isAsExpression) List(mainPath.get, targetPath.get).mkString
        else if (isWhereExpression && isEnum) List(mainPath.get, predicatePath.get).filter(_.nonEmpty).mkString(".")
        else if (isWhereExpression && hasResolvePredicate) List(mainPath.get, trailPath.getOrElse("")).filter(_.nonEmpty).mkString(".")
        else if (isWhereExpression) List(mainPath.get, predicatePath.get, trailPath.getOrElse("")).filter(_.nonEmpty).mkString(".")
        else mainPath.get
    }

}

/**
  * The companion object for helper constants and functions.
  */
object PathExpression {

    /**
      * Token Types.
      */
    val MAIN_PATH = "MAIN_PATH"
    val CONDITION = "CONDITION"
    val PREDICATE = "PREDICATE"
    val OPERATOR = "OPERATOR"
    val TARGET = "TARGET"
    val TRAIL_PATH = "TRAIL_PATH"
    val WHERE = ".where"

    /**
      * Empty Tokens.
      *
      * @return mapOfEmptyTokens.
      */
    def emptyTokens(): Map[String, TokenExpression] = Map[String, TokenExpression](
        MAIN_PATH -> TokenExpression.emptyOf(MAIN_PATH),
        CONDITION -> TokenExpression.emptyOf(CONDITION),
        PREDICATE -> TokenExpression.emptyOf(PREDICATE),
        OPERATOR -> TokenExpression.emptyOf(OPERATOR),
        TARGET -> TokenExpression.emptyOf(TARGET),
        TRAIL_PATH -> TokenExpression.emptyOf(TRAIL_PATH)
    )

    def mainPath(pathValue: String): (String, TokenExpression) = MAIN_PATH -> TokenExpression.of(MAIN_PATH, Some(pathValue))

    def conditionPath(): (String, TokenExpression) = CONDITION -> TokenExpression.of(CONDITION, Some("where"))

    def trailPath(pathValue: String): (String, TokenExpression) =
        if (pathValue.nonEmpty) TRAIL_PATH -> TokenExpression.of(TRAIL_PATH, Some(pathValue))
        else TRAIL_PATH -> TokenExpression.of(TRAIL_PATH, None)

    def predicatePath(pathValue: String): (String, TokenExpression) = PREDICATE -> TokenExpression.of(PREDICATE, Some(pathValue))

    def operatorPath(pathValue: String): (String, TokenExpression) = OPERATOR -> TokenExpression.of(OPERATOR, Some(pathValue))

    def targetPath(pathValue: String, capitalize: Boolean = false): (String, TokenExpression) =
        if (capitalize) TARGET -> TokenExpression.of(TARGET, Some(pathValue.capitalize))
        else TARGET -> TokenExpression.of(TARGET, Some(pathValue))

    /**
      * Factory definition of object creation.
      *
      * @param givenExpression, factory parameter for the given expression.
      * @return objectOfPathExpression
      */
    def of(givenExpression: String): PathExpression = PathExpression(givenExpression)
}

/**
  * Id with path expression.
  */
case class IdPathExpression(id: String, pathExpression: PathExpression)
object IdPathExpression{
    def from(id: String, pathExpression: PathExpression) = IdPathExpression(id, pathExpression)
}
