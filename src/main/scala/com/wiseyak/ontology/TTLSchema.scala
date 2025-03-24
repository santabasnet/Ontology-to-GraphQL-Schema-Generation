package com.wiseyak.ontology

import java.io.FileInputStream

import com.wiseyak.conf.Configuration
import com.wiseyak.ontology.FHIRHierarchy.Hierarchy
import org.apache.jena.ontology.{OntModel, OntModelSpec}
import org.apache.jena.rdf.model.ModelFactory

import scala.collection.mutable
import scala.util.Try

/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-25.
  */
object TTLSchema {

    /**
      * Initialize the model here.
      *
      * @return ontologyModel
      */
    private def initializeModel: OntModel = Try {
        val model = ModelFactory.createDefaultModel()
        model.read(new FileInputStream(Configuration.ontologyInputFile), null, Configuration.ontologyFormat)
        val ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model)
        ontologyModel.setStrictMode(false)
        ontologyModel
    }.getOrElse(ModelFactory.createOntologyModel())

    /**
      * Model Class Hierarchy.
      */
    private val ontologyModelHierarchy = FHIRHierarchy().build(initializeModel)

    /**
      * Build class descriptions.
      */
    private def buildClassDescription(hierarchy: Hierarchy): List[ClassProperties] =
        ontologyModelHierarchy(hierarchy).map(ClassDescription.buildPropertyClassOf).filter(_.hasNonEmptyProperties)

    /**
      * Build property classes.
      */
    private def buildPropertyClasses: List[ClassProperties] = {
        val propertyClasses = mutable.ListBuffer[ClassProperties]()
        ontologyModelHierarchy.keys.toList.sortWith(_ > _)
            .map(hierarchy => hierarchy -> buildClassDescription(hierarchy)).filter(_._2.nonEmpty)
            .foreach(entry => propertyClasses ++= entry._2)
        propertyClasses.groupBy(_.ontologyClassName()).map(_._2.head).toList
    }

    /**
      * Returns the list class properties from the TTL file.
      */
    def listOfClassProperties: List[ClassProperties] = buildPropertyClasses

    /**
      * Returns the list of enumeration properties from the TTL file.
      */
    def listOfEnumerationProperties: List[EnumerationProperties] = {
        List()
    }

}
