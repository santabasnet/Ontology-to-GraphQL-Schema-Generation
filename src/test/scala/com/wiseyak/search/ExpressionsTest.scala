package com.wiseyak.search

import org.scalatest.funsuite.AnyFunSuite

/**
  * This class is a part of the package com.wiseyak.search and the package
  * is a part of the project 1.1.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-07-14.
  */
class ExpressionsTest extends AnyFunSuite {

    /**
      * Reference resolution parsing test.
      */
    test("Validates resolve with targets arguments path: "){
        val example = "Provenance.target.where(resolve() is Patient)"
        val pathResult = "Provenance.target"
        val predicateResult = "resolve"
        val targetResult = "Patient"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
        assert(parsedExample.predicatePath.getOrElse("").equals(predicateResult))
        assert(parsedExample.targetPath.getOrElse("").equals(targetResult))
    }

    test("Validates value as target path: "){
        val example = "(Questionnaire.useContext.value as Quantity)"
        val pathResult = "Questionnaire.useContext.valueQuantity"
        val targetResult = "Quantity"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
        assert(parsedExample.targetPath.getOrElse("").equals(targetResult))
    }

    test("Validates value as target and simple data type path: "){
        val example = "(RiskAssessment.occurrence as dateTime)"
        val pathResult = "RiskAssessment.occurrenceDateTime"
        val targetResult = "DateTime"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
        assert(parsedExample.targetPath.getOrElse("").equals(targetResult))
    }

    test("Validates typed arguments with trailing path: "){
        val example = "ResearchDefinition.relatedArtifact.where(type='composed-of').resource"
        val pathResult = "ResearchDefinition.relatedArtifact.type"
        val predicateResult = "type"
        val targetResult = "composed-of"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
        assert(parsedExample.predicatePath.getOrElse("").equals(predicateResult))
        assert(parsedExample.targetPath.getOrElse("").equals(targetResult))
    }

    test("Validates value as target with trailing path: "){
        val example = "(Observation.value as CodeableConcept).text"
        val pathResult = "Observation.valueCodeableConcept.text"
        val targetResult = "CodeableConcept"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
        assert(parsedExample.targetPath.getOrElse("").equals(targetResult))
    }

    test("Validates value as target with basic type path: "){
        val example = "(RiskAssessment.occurrence as dateTime)"
        val pathResult = "RiskAssessment.occurrenceDateTime"
        val targetResult = "DateTime"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
        assert(parsedExample.targetPath.getOrElse("").equals(targetResult))
    }

    test("Validates typed arguments with system path: "){
        val example = "OrganizationAffiliation.telecom.where(system='email')"
        val pathResult = "OrganizationAffiliation.telecom.system"
        val predicateResult = "system"
        val targetResult = "email"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
        assert(parsedExample.predicatePath.getOrElse("").equals(predicateResult))
        assert(parsedExample.targetPath.getOrElse("").equals(targetResult))
    }

    test("Validates main path only: "){
        val example = "OrganizationAffiliation.identifier"
        val pathResult = "OrganizationAffiliation.identifier"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
    }

    test("Validates typed arguments without trailing path: "){
        val example = "ResearchDefinition.relatedArtifact.where(type='composed-of')"
        val pathResult = "ResearchDefinition.relatedArtifact.type"
        val predicateResult = "type"
        val targetResult = "composed-of"

        val parsedExample = PathExpression.of(example)
        assert(parsedExample.rebuildPathExpression.equals(pathResult))
        assert(parsedExample.predicatePath.getOrElse("").equals(predicateResult))
        assert(parsedExample.targetPath.getOrElse("").equals(targetResult))
    }

}
