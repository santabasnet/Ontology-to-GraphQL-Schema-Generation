package com.wiseyak.graphql

import java.io.File

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}
import scala.io.Source

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-04.
  */
object GraphQLClient {

    /**
      * URL.
      */
    val SERVER_URL = "http://localhost:8080/admin/schema"

    /**
      * Program starts here.
      */
    def main(args: Array[String]): Unit = {
        val allFiles = FileResources.graphQLSchema()
        val result = allFiles.map(schemaContent).mkString("\n")
        val firstContent = schemaContent(allFiles.head)


        makeRequest(demoSchema1)



        //println("Total Size : " + allFiles.size)
    }

    private def makeRequest(content: String): Unit = {

        val httpClient = HttpClients.createDefault()
        val execution = Future {
            httpClient.execute(postEntity(content))
        }
        val closableResponse = Await.result(execution, 60 seconds)


        //println(postEntity(file).getEntity.getContentLength)

        val serverResponse = EntityUtils.toString(closableResponse.getEntity)
        println("Response : \n" + serverResponse)
    }

    private def postEntity(content: String): HttpPost = {
        val httpPost = new HttpPost(SERVER_URL)
        val schemaEntity = new StringEntity(content)
        httpPost.setEntity(schemaEntity)
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-type", "application/json")
        httpPost
    }

    private def schemaContent(file: File): String = Source.fromFile(file, "UTF-8").mkString


    private def demoSchema: String =
        """
          |type Author {
          |  username: String! @id
          |  questions: [Question] @hasInverse(field: author)
          |  answers: [Answer] @hasInverse(field: author)
          |}
          |interface Post {
          |  id: ID!
          |  text: String! @search(by: [fulltext])
          |  datePublished: DateTime @search
          |  author: Author!
          |  comments: [Comment] @hasInverse(field: commentsOn)
          |}
          |type Question implements Post {
          |  answers: [Answer] @hasInverse(field: inAnswerTo)
          |}
          |type Answer implements Post {
          |  inAnswerTo: Question!
          |}
          |type Comment implements Post {
          |  commentsOn: Post!
          |}
        """.stripMargin.trim

    private def demoSchema1 =
        """
          |type Code {
          |	id:ID
          |	text: String
          |}
          |
          |type ElementBase {
          |  id: ID
          |  extension: [Extension]
          |}
          |
          |type Coding {
          |  id: String
          |  extension: [Extension]
          |  system: String
          |  version: String
          |  code: Code
          |  display: String
          |  userSelected: Boolean
          |}
          |
          |type Extension {
          |  id: String
          |  extension: [Extension]
          |  url: String
          |  valueCode: Code
          |  valueDate: DateTime
          |}
          |
        """.stripMargin.trim


}
