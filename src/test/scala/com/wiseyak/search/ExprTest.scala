package com.wiseyak.search

/**
  * This class is a part of the package com.wiseyak.search and the package
  * is a part of the project 1.1.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-07-16.
  */
object ExprTest extends App{

    val example = "(RiskAssessment.occurrence as dateTime)"
    val example1 = "ResearchDefinition.relatedArtifact.where(type='composed-of').resource"
    val example3 = "OrganizationAffiliation.telecom.where(system='email')"


    val parsedExample = PathExpression.of(example3)

    parsedExample.parsedInformation.foreach(println)

    println(parsedExample.rebuildPathExpression)
    println(parsedExample.referenceResolution)
    //println(parsedExample.targetPath)

}
