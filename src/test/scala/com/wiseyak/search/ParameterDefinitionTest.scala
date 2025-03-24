package com.wiseyak.search

/**
  * This class is a part of the package com.wiseyak.search and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-01-07.
  */
object ParameterDefinitionTest extends App {

    println(FHIRSearch.allParameters().size)
    println(FHIRSearch.allReferenceParameters().size)

    //FHIRSearch.allReferenceParameters().map(_.pathExpressions).foreach(println)
    FHIRSearch.allReferenceParameters().map(_.pathExpressions).filter(_.nonEmpty).foreach(println)

//    ParameterDefinition.buildFromConfiguration.entries
//        .map(ResourceEntry.buildWith)
//        .map(_.expression)
//        .filter(_.contains("where"))
//        .foreach(println)


}
