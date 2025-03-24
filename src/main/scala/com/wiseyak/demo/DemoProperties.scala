package com.wiseyak.demo

import com.wiseyak.ontology.ClassProperties

/**
  * This class is a part of the package com.wiseyak.demo and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-23.
  */
object DemoProperties {

    /**
      * Demo properties definition for first class of Question.
      */
    private val questionInformation = Map[String, Map[String, Any]](
        "id" -> Map[String, Any](
            "cardinality" -> 1,
            "allValuesRestriction" -> "ID",
        ),
        "text" -> Map[String, Any](
            "cardinality" -> 1,
            "allValuesRestriction" -> "string"
        ),
        "datePublished" -> Map[String, Any](
            "maxCardinality" -> 1,
            "someValuesRestriction" -> "dateTime"
        ),
        "answers" -> Map[String, Any](
            "allValuesRestriction" -> "Answer"
        )
    )

    private val questionProperties = ClassProperties.of("Question", questionInformation)

    /**
      * Demo properties definition for the second class of Answer.
      */
    private val answerInformation = Map[String, Map[String, Any]](
        "id" -> Map[String, Any](
            "cardinality" -> 1,
            "allValuesRestriction" -> "ID",
        ),
        "text" -> Map[String, Any](
            "cardinality" -> 1,
            "allValuesRestriction" -> "string"
        ),
        "datePublished" -> Map[String, Any](
            "maxCardinality" -> 1,
            "someValuesRestriction" -> "dateTime"
        ),
        "inAnswerTo" -> Map[String, Any](
            "cardinality" -> 1,
            "allValuesRestriction" -> "Question"
        )
    )

    private val answerProperties = ClassProperties.of("Answer", answerInformation)

    /**
      * Demo class properties.
      */
    val uniqueClassProperties: List[ClassProperties] = List(questionProperties, answerProperties)

}
