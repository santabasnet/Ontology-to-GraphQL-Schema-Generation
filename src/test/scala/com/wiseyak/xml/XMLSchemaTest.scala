package com.wiseyak.xml

/**
  * This class is a part of the package com.wiseyak.xml and the package
  * is a part of the project 1.1.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-03-25.
  */
object XMLSchemaTest {

    val xmlSchema = XMLSchema()


    def main(args: Array[String]): Unit = {

        //val elements = xmlSchema.getChildrenComplexTypes
        val elements = xmlSchema.getChildrenSimpleTypes

        println(elements.size)


        elements.foreach(item => println(item.getName))


    }

}
