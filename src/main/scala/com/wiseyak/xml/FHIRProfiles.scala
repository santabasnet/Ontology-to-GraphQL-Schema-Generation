package com.wiseyak.xml

import com.wiseyak.conf.Configuration

import scala.util.Try
import scala.xml.{Elem, Node, NodeSeq, XML}

/**
  * This class is a part of the package com.wiseyak.xml and the package
  * is a part of the project 1.1.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2021-03-21.
  */
case class FHIRProfiles(fileName: String) {

    private def emptyTargets: Map[String, List[String]] = Map[String, List[String]]()

    private def attributeValue(node: Option[Node], attributeName: String = FHIRProfiles.VALUE_PROPERTY): Option[String] = node.map(entry => (entry \ s"@$attributeName").text)

    private def attributeValue(nodes: NodeSeq): List[String] = nodes.toList.map(node => attributeValue(node.headOption)).filter(_.nonEmpty).map(_.get)

    private def referenceFilter(node: Option[Node]): Boolean = attributeValue(node) match {
        case None => false
        case Some(value) => value == FHIRProfiles.REFERENCE_TYPE
    }

    private def typeNameFrom(text: String): String =
        if (text.startsWith(FHIRProfiles.HTTP)) text.substring(text.lastIndexOf(FHIRProfiles.PATH_SEPARATOR) + 1) else text

    private def profileData: Elem = XML.loadFile(fileName)

    private def cleanPathX(pathName: String): String = if (pathName.endsWith("[x]")) pathName.replace("[x]", "") else pathName

    private val collectUnionTypeTargets: Map[String, List[String]] = Try {
        (profileData \ "entry" \ "resource" \ "StructureDefinition" \ "snapshot" \ "element")
            .filter(entry => referenceFilter((entry \ "type" \ "code").headOption))
            .map(entry => (attributeValue((entry \ "path").headOption), attributeValue(entry \ "type" \ "targetProfile")))
            .filter(entry => entry._1.nonEmpty && entry._2.nonEmpty)
            .map(entry => entry._1.get -> entry._2.map(typeNameFrom))
            .map(entry => cleanPathX(entry._1) -> entry._2)
            .toMap
    }.getOrElse(emptyTargets)

    /**
      * Accumulates the path value of reference types and their targets attributes.
      *
      * @return mapOfPathWithTargets
      */
    def referenceTargets: Map[String, List[String]] = collectUnionTypeTargets

}

object FHIRProfiles {

    val VALUE_PROPERTY = "value"
    val REFERENCE_TYPE = "Reference"
    val HTTP = "http"
    val PATH_SEPARATOR = "/"

    def of(fileName: String): FHIRProfiles = FHIRProfiles(fileName)

    /**
      * Returns the property path and its associated references classes.
      * variablePath -> associatedReferenceClasses
      *
      * @return listOfPathReferences
      */
    def collectUnionTargets: Map[String, List[String]] = FHIRProfiles.of(Configuration.ontologyProfileFile).collectUnionTypeTargets

}
