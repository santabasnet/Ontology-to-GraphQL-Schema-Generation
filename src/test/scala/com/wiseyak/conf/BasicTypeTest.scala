package com.wiseyak.conf

/**
  * This class is a part of the package com.wiseyak.conf and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-11-17.
  */
object BasicTypeTest extends App {

    val basicTypes = Configuration.basicTypeDefinitions.values.toList.distinct.sorted

    println(basicTypes)

}
