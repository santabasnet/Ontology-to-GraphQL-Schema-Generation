package com.wiseyak.generator

import com.wiseyak.graphql.Templates
import com.wiseyak.ontology.{ClassProperties, InverseRelation}
import org.scalatest.funsuite.AnyFunSuite

/**
  * This class is a part of the package com.wiseyak.generator and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-23.
  */
class GeneratorSpec extends AnyFunSuite {

    /**
      * Expected output schema defined for the given demo properties.
      *
      * @return expectedSchemaDefinition.
      */
    private def expectedSchema: String =
        """
          |type Question {
          |    datePublished: dateTime
          |	text: string!
          |	instanceID: string!
          |	answers: [Answer] @hasInverse(field: inAnswerTo)
          |	id: string!
          |}
          |type Answer {
          |    datePublished: dateTime
          |	text: string!
          |	instanceID: string!
          |	id: string!
          |	inAnswerTo: Question!
          |}
        """.stripMargin.trim

    /**
      * Test Code.
      */
    test("Generated schema validation of demo classes : ") {
        val uniqueClassProperties: List[ClassProperties] = DemoProperties.uniqueClassProperties
        val inverseRelation = InverseRelation.of(uniqueClassProperties)
        val uniqueGeneratedClasses = uniqueClassProperties.map(classDefinition => Templates.ofPropertyClass(classDefinition, inverseRelation))
        val generatedSchema = uniqueGeneratedClasses.mkString(System.lineSeparator()).trim
        assert(expectedSchema == generatedSchema)
    }


}
