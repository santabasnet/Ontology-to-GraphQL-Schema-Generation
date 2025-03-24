package com.wiseyak.ontology

import com.wiseyak.ontology.FHIRHierarchy.Hierarchy
import org.apache.jena.ontology.{OntClass, OntModel, OntProperty}

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-06.
  */
case class FHIRHierarchy() {

    /**
      * Ordering definition for the sorted map.
      */
    implicit val Ordering: Ordering[Hierarchy] = implicitly[Ordering[Hierarchy]]

    /**
      * Writes the class hierarchy.
      *
      * @param ontologyModel, a model source.
      */
    def build(ontologyModel: OntModel): Map[Hierarchy, List[OntClass]] = {
        val ontologyClasses = ontologyModel.listHierarchyRootClasses().filterDrop(_.isAnon)
        while (ontologyClasses.hasNext) buildClass(ontologyClasses.next, mutable.ListBuffer[OntClass](), ScalarType.ZERO_DEPTH)
        FHIRHierarchy.generatedHierarchy.toMap
    }

    /**
      * Build ontology class hierarchy.
      *
      * @param ontologyClass
      * @param appears
      * @param depth
      */
    private def buildClass(ontologyClass: OntClass, appears: mutable.ListBuffer[OntClass], depth: Hierarchy): Unit = {
        makeEntry(ontologyClass, depth)

        /**
          * SuperClasses Entries.
          */
        val sClasses = ontologyClass.listSuperClasses(true).toList
        val addedClasses = FHIRHierarchy.generatedSuperClasses.getOrElse(ontologyClass, new java.util.ArrayList[OntClass]())
        FHIRHierarchy.generatedSuperClasses.put(ontologyClass, addedClasses)
        addedClasses.addAll(sClasses)
        if (eligibleEntry(ontologyClass, appears)) {
            ontologyClass.listSubClasses(true).toList.forEach(subClass => {
                appears += subClass
                buildClass(subClass, appears, depth + 1)
                appears -= subClass
            })
        }
    }

    /**
      * Check if the current ontology class is eligible to make entry or not.
      */
    private def eligibleEntry(ontologyClass: OntClass, appears: mutable.ListBuffer[OntClass]): Boolean =
        ontologyClass.canAs(classOf[OntClass])

    /**
      * Perform entry of the class with its hierarchy.
      */
    private def makeEntry(ontologyClass: OntClass, depth: Hierarchy): Unit = FHIRHierarchy
        .generatedHierarchy.put(depth, FHIRHierarchy.generatedHierarchy.getOrElse(depth, List[OntClass]()) :+ ontologyClass)

    /**
      * Schema generation for the given class.
      *
      * @param ontologyClass, given class.
      * @param depth        , current depth.
      */
    private def generateSchema(ontologyClass: OntClass, depth: Int): Unit = {
        ontologyClass.listSubClasses().toList.stream().forEach(propertyItem => {
            println("Name : " + propertyItem.getLocalName)
        })
    }

    private def propertyType(property: OntProperty): String = {
        if (property.isFunctionalProperty) "FunctionalProperty"
        else if (property.isObjectProperty) "ObjectType"
        else if (property.isDatatypeProperty) "DataType"
        else "Other Type"
    }

    def printSuperClasses(): Unit = {
        FHIRHierarchy.generatedSuperClasses
            .map(entry => entry._1.getLocalName -> entry._2.asScala.map(_.getURI).distinct)
            .foreach(println)
    }


}

/**
  * The companion object.
  */
object FHIRHierarchy {

    /**
      * Type definition of class hierarchy.
      */
    type Hierarchy = Int

    /**
      * Hierarchy of classes extracted from the ontology.
      */
    val generatedHierarchy: mutable.Map[Hierarchy, List[OntClass]] = mutable.Map[Hierarchy, List[OntClass]]()

    /**
      * SubClass relation.
      */
    val generatedSuperClasses: mutable.Map[OntClass, java.util.List[OntClass]] = mutable.Map[OntClass, java.util.List[OntClass]]()

    /**
      * Class accumulator.
      */
    val graphqlClasses: mutable.Map[Hierarchy, String] = mutable.Map[Hierarchy, String]()
}
