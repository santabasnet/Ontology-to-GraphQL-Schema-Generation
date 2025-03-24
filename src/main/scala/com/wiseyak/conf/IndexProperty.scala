package com.wiseyak.conf

/**
  * This class is a part of the package com.wiseyak.conf and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-31.
  */
case class IndexProperty(propertyName: String, indexType: List[String]) {
    def isEmpty: Boolean = propertyName.isEmpty || indexType.isEmpty
}

/**
  * The companion object.
  */
object IndexProperty {
    def empty = IndexProperty("", List[String]())
}
