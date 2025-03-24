package com.wiseyak.graphql

import java.io.File

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-04.
  */
object FileResources {

    /**
      * Basic types file.
      */
    private val BASIC_TYPES = "types.graphql"

    /**
      * Root Folder.
      */
    private val ROOT = "C:/WiseYak/data/ontology/graphql/defn/"

    /**
      * Get all schema files.
      */
    private def listFiles() = new File(ROOT)
        .listFiles.filter(_.isFile)
        .filterNot(_.getName.contains(BASIC_TYPES)).toList

    /**
      * Returns all the graphql schema files.
      */
    def graphQLSchema(): List[File] = new File(ROOT + BASIC_TYPES) +: listFiles

}
