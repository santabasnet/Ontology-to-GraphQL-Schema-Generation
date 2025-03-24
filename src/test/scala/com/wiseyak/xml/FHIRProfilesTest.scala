package com.wiseyak.xml

import com.wiseyak.conf.Configuration

/**
  * This class is a part of the package com.wiseyak.xml and the package
  * is a part of the project 1.1.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-03-21.
  */
object FHIRProfilesTest extends App {
    val referenceInfo = FHIRProfiles.of(Configuration.ontologyProfileFile).referenceTargets
    println(referenceInfo)
}
