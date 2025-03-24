package com.wiseyak.ontology

import org.scalatest.funsuite.AnyFunSuite

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-10.
  */
class ScalarTypeSpec extends AnyFunSuite  {

    /**
      * Test Code.
      */
    test("String types of all: "){
        assert(ScalarType.mappingOf("base64Binary") == "string")
    }

}
