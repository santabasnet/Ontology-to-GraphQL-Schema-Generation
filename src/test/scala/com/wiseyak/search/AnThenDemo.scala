package com.wiseyak.search

/**
  * This class is a part of the package com.wiseyak.search and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-01-27.
  */
object AnThenDemo extends App {

    private val text = "Hello world npeal /akldjfa+ $$#*test 88"
    private val delimiters = "[ -$^/+#&*//@।,;!]"

    private def updateStatistics(text: String): String = {
        println(text.split(delimiters).toList)
        val noOfWords = text.split(delimiters).count(_.nonEmpty)
        println("No. of words : " + noOfWords)
        // update the records.
        text
    }
    private def convertText(text: String): String = text.toUpperCase
    val result = Some(text).map(updateStatistics).map(convertText).get
    println("Result : " + result)

    //////////////////////////////////////////////////////////////////////
    //Another way:
    val updateCount = (text: String) => {
        val noOfWords = text.split(delimiters).count(_.nonEmpty)
        println("No. of words : " + noOfWords)
        // update the records.
        text
    }
    val convert = (text: String) => text.toUpperCase
    val r1 = (updateCount andThen convertText)(text)
    println("Another result: " + r1)

}
