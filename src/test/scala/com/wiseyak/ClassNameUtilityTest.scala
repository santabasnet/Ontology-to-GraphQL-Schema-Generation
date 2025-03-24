package com.wiseyak

import com.wiseyak.graphql.ClassNameUtility

/**
  * This class is a part of the package com.wiseyak and the package
  * is a part of the project 1.1.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-07-30.
  */
object ClassNameUtilityTest extends App{

    val name = "NutritionOrder"

    println(ClassNameUtility.hasReservedName(name))

    println(ClassNameUtility.getKeywordClass(name))

}
