package com.wiseyak.search

/**
  * This class is a part of the package com.wiseyak.search and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-01-08.
  */
object IndexTemplates {

    /**
      * Index type literals.
      */
    private val EMPTY = ""
    private val ID = "id"
    private val BASIC = "basic"
    private val EXACT = "exact"
    private val FULL_TEXT = "fulltext"

    private val STRING = "STRING"
    private val TOKEN = "TOKEN"
    private val DATETIME = "DATETIME"
    private val DATE = "DATE"
    private val BOOLEAN = "BOOLEAN"
    private val URI = "URI"
    private val FLOAT = "FLOAT"
    private val NUMBER = "NUMBER"

    /**
      * Supported index types.
      */
    private val supportedIndexTypes: Set[String] = Set(EXACT, FULL_TEXT)

    /**
      * Checks whether the given index type is supported by the system to make search or not.
      *
      * @param indexType, type name of the indexing.
      * @return true if the given index type is supported in the schema.
      */
    private def isSupportedIndexType(indexType: String): Boolean = supportedIndexTypes.contains(indexType)

    /**
      * Condition for String-Token.
      *
      * @param innerType
      * @param searchType
      * @return
      */
    private def isStringToken(innerType: String, searchType: String): Boolean =
        innerType.toUpperCase == STRING && searchType.toUpperCase == TOKEN

    /**
      * Condition for String-String.
      *
      * @param innerType
      * @param searchType
      * @return
      */
    private def isStringString(innerType: String, searchType: String): Boolean =
        innerType.toUpperCase == STRING && searchType.toUpperCase == STRING

    /**
      * Condition for Datetime data.
      *
      * @param innerType
      * @param searchType
      * @return
      */
    private def isDateTime(innerType: String, searchType: String): Boolean =
        innerType.toUpperCase == DATETIME && searchType.toUpperCase == DATE

    /**
      * Condition for Boolean token.
      *
      * @param innerType
      * @param searchType
      * @return
      */
    private def isBooleanToken(innerType: String, searchType: String): Boolean =
        innerType.toUpperCase == BOOLEAN && searchType.toUpperCase == TOKEN

    /**
      * Condition for String-URI.
      *
      * @param innerType
      * @param searchType
      * @return
      */
    private def isStringURI(innerType: String, searchType: String): Boolean =
        innerType.toUpperCase == STRING && searchType.toUpperCase == URI

    /**
      * Condition for the Float-Number.
      * @param innerType
      * @param searchType
      * @return
      */
    private def isFloatNumber(innerType: String, searchType: String): Boolean =
        innerType.toUpperCase == FLOAT && searchType.toUpperCase == NUMBER

    /**
      * Search template for text data type.
      * @param indexType, type name of the indexing.
      * @return textIndex
      */
    private def textSearchTemplate(indexType: String): String =
        s"""
           |@search(by: [$indexType])
         """.stripMargin

    /**
      * Search template definition with given index type.
      * In this case, index type is from {exact, fulltext}.
      *
      * @param indexType, type name of the indexing.
      * @return searchTemplate.
      */
    private def searchFor(indexType: String): String =
        if(indexType == ID) "@id"
        else if (isSupportedIndexType(indexType)) textSearchTemplate(indexType).trim
        else if (indexType == BASIC) "@search"
        else EMPTY

    /**
      * Returns the token type by using given type names with rules.
      *
      * @param innerType , datatype in the ontology.
      * @param searchType, datatype in the search definition.
      * @return indexType from supported index types.
      */
    private def getIndexType(innerType: String, searchType: String): Option[String] =
        if(searchType == ID) Some(ID)
        else if (isStringToken(innerType, searchType)) Some(EXACT)
        else if (isStringString(innerType, searchType)) Some(FULL_TEXT)
        else if (isStringURI(innerType, searchType)) Some(EXACT)
        else if (isBooleanToken(innerType, searchType)) Some(BASIC)
        else if (isDateTime(innerType, searchType)) Some(BASIC)
        else if (isFloatNumber(innerType, searchType)) Some(BASIC)
        else None

    /**
      * Build index part of the given type name.
      *
      * @param innerType , datatype from the ontology.
      * @param searchType, datatype in the search definition.
      * @return searchPart
      */
    private def buildIndexPart(innerType: String, searchType: String): String =
        getIndexType(innerType, searchType).map(searchFor).getOrElse(EMPTY)

    /**
      * Builds index information to make property search.
      *
      * @param propertyName  , property name given in the ontology.
      * @param innerTypeName , data type of the given property in the ontology.
      * @param searchTypeName, data type in the search definition for the same property.
      * @return indexInformation.
      */
    def buildIndexPart(propertyName: String, innerTypeName: String, searchTypeName: String): String = {
        //println(innerTypeName + ", " + searchTypeName + " >> " + buildIndexPart(innerTypeName, searchTypeName))
        buildIndexPart(innerTypeName, searchTypeName)
    }


}
