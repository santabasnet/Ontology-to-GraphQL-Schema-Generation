package com.wiseyak.json

import com.wiseyak.ontology.{ClassProperties, EnumerationProperties}

/**
  * This class is a part of the package com.wiseyak.json and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-23.
  */
object JSONSchema {

    /**
      * Returns the list of class properties from the XSD schema file.
      */
    def listOfClassProperties: List[ClassProperties] = ???

    /**
      * Returns the list of enumeration properties from the XSD schema file.
      */
    def listOfEnumerationProperties: List[EnumerationProperties] = ???



    def main(args: Array[String]): Unit = {
        //val jsonNode = objectMapper.readTree(new File(Configuration.ontologyInputFile))
        //jsonNode.fields().forEachRemaining(item => println(item.getKey))

    }


}
