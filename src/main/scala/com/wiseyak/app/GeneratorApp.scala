package com.wiseyak.app

import com.wiseyak.graphql.Generator

/**
  * This class is a part of the package com.wiseyak.app and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-11.
  */
object GeneratorApp {

    def main(args: Array[String]): Unit = {
        Generator.buildGraphQLSchema()
    }

}
