package com.wiseyak.conf

import org.scalatest.funsuite.AnyFunSuite

/**
  * This class is a part of the package com.wiseyak.conf and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-23.
  */
class ConfigurationSpec extends AnyFunSuite {

    /**
      * An empty instance.
      */

    val emptyConfiguration: AppConfiguration = AppConfiguration.empty

    /**
      * Supported formats test.
      */
    test("TTL schema supported formats: "){
        assert(Configuration.supportedFormats.contains("TTL"))
    }

    /**
      * Empty Supported files.
      */
    test("All empty supported formats: ") {
        assert(emptyConfiguration.supportedFormats.isEmpty)
    }

}
