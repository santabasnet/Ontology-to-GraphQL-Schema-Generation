package com.wiseyak.xml

import java.util

import com.wiseyak.conf.Configuration
import com.wiseyak.graphql.ClassNameUtility
import com.wiseyak.ontology.{ClassProperties, EnumerationProperties, ScalarType, WiseUnionClass}
import com.wiseyak.search.FHIRSearch
import org.xmlet.xsdparser.core.XsdParser
import org.xmlet.xsdparser.xsdelements._

import scala.collection.mutable
import scala.util.Try


/**
  * This class is a part of the package com.wiseyak.xml and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-23.
  */

case class XMLSchema(fhirNameSpaces: List[String] = Configuration.defaultNameSpaces) {
    /**
      * Load XML schema from file here.
      */
    private val xmlSchema = new XsdParser(Configuration.ontologyInputFile)

    /**
      * Extract All Schemas.
      */
    private def allSchema: List[XsdSchema] = {
        val schemas = mutable.ListBuffer[XsdSchema]()
        xmlSchema.getResultXsdSchemas.filter(schema => fhirNameSpaces.contains(schema.getTargetNamespace)).forEach(schema => schemas += schema)
        schemas.toList
    }

    /**
      * Check if schema is present.
      */
    def isPresent: Boolean = allSchema.nonEmpty

    /**
      * Get all the children elements.
      */
    def getChildrenElements: List[XsdElement] = {
        val allElements = mutable.ListBuffer[XsdElement]()
        allSchema.foreach(schema => schema.getChildrenElements.forEach(element => allElements += element))
        allElements.toList
    }

    /**
      * Get all the children of complex types.
      *
      * @return listOfComplexType
      */
    def getChildrenComplexTypes: List[XsdComplexType] = {
        val allComplexTypes = mutable.ListBuffer[XsdComplexType]()
        allSchema.foreach(schema => schema.getChildrenComplexTypes.forEach(complexType => allComplexTypes += complexType))
        allComplexTypes.toList
    }

    /**
      * Get all the children of simple types.
      *
      * @return
      */
    def getChildrenSimpleTypes: List[XsdSimpleType] = {
        val allSimpleTypes = mutable.ListBuffer[XsdSimpleType]()
        allSchema.foreach(schema => schema.getChildrenSimpleTypes.forEach(simpleType => allSimpleTypes += simpleType))
        allSimpleTypes.toList
    }

    /**
      * Complex types with the given resource name.
      *
      * @param resourceName, given resource name to filter them out.
      * @return listOfComplexType
      */
    private def getChildrenComplexTypes(resourceName: String): List[XsdComplexType] = {
        val complexTypes = mutable.ListBuffer[XsdComplexType]()

        /*val result = getChildrenComplexTypes.filter(typeItem => {
            Try(typeItem.getComplexContent.getXsdExtension.getBase.getName).getOrElse("None") == resourceName
        })
        println(resourceName + ": " + result.size)
        result.foreach(item => {
            println(item.getComplexContent.getXsdExtension.getChildAsSequence.getChildrenElements.count())
        })*/

        getChildrenComplexTypes
            .filter(typeItem => Try(typeItem.getComplexContent.getXsdExtension.getBase.getName).getOrElse("None") == resourceName)
            .foreach(item => complexTypes += item)

        //getChildrenComplexTypes.filter(_.getName == resourceName).head.getXsdSchema.getChildrenComplexTypes.forEach(item => complexTypes += item)
        complexTypes.toList
    }


    /**
      * Returns the complex types with resource container name.
      *
      * @return listOfXsdElements
      */
    def resourceContainerElements: List[XsdElement] = {
        val elements = mutable.ListBuffer[XsdElement]()
        getChildrenComplexTypes.filter(_.getName == TypeDefinitions.RESOURCE_CONTAINER).head.getXsdChildElement.getXsdElements
            .forEach(element => elements += element.asInstanceOf[XsdElement])
        elements.toList
    }

    /**
      * Returns the complex types with domain resource.
      *
      * @return listOfComplexType
      */
    def domainResourceComplexTypes: List[XsdComplexType] = getChildrenComplexTypes(TypeDefinitions.DOMAIN_RESOURCE)

    /**
      * Returns the complex types with backbone element name.
      *
      * @return listOfComplexType
      */
    def backboneComplexTypes: List[XsdComplexType] = getChildrenComplexTypes(TypeDefinitions.BACKBONE_ELEMENT)

    /**
      * Returns the complex types with element name.
      *
      * @return listOfComplexType
      */
    def complexElementTypes: List[XsdComplexType] = getChildrenComplexTypes(TypeDefinitions.ELEMENT)

    /**
      * Returns the complex type that has name Resource, in case of FHIR schema.
      *
      * @return listOfComplexTypes
      */
    def resourceOnlyComplexType: List[XsdComplexType] = {
        val complexTypes = mutable.ListBuffer[XsdComplexType]()
        getChildrenComplexTypes
            .filter(typeItem => Try(typeItem.getChildAsSequence.getChildrenElements.count()).getOrElse(0L) > 1)
            .foreach(item => complexTypes += item)
        complexTypes.toList
    }

    /**
      * Returns all the complex types of Quantities.
      */
    def quantityComplexTypes: List[XsdComplexType] = getChildrenComplexTypes(TypeDefinitions.QUANTITY)

    /**
      * Returns all the complex types of Resources.
      *
      * @return
      */
    def resourceComplexTypes: List[XsdComplexType] = getChildrenComplexTypes(TypeDefinitions.RESOURCE)

    /**
      * Returns the simple types with backbone element name.
      *
      * @return listOfSimpleType
      */
    def backboneSimpleTypes: List[XsdSimpleType] = getChildrenSimpleTypes.filter(item => !isEnumeration(item))

    /**
      * Returns all the enumeration types.
      *
      * @return listOfEnumTypes
      */
    def backboneEnumTypes: List[XsdSimpleType] = {
        /*getChildrenSimpleTypes
            .filter(_.getName.endsWith("_list"))
            .foreach(item => println(item.getName + "\t" + item.getRestriction.getEnumeration.size()))*/
        getChildrenSimpleTypes.filter(_.getName.endsWith("_list"))
    }

    /**
      * All the enumerated class type names.
      */
    def enumeratedClasses: List[XsdSimpleType] = backboneEnumTypes

    /**
      * All enumeration mappings.
      */
    def enumerationMappings: Map[String, String] = backboneComplexTypes.map(complexType => {
        val attribute = complexType.getComplexContent.getXsdExtension.getXsdAttributes.findFirst()
        if (!attribute.isPresent) None
        else if (isEnumerationList(attribute.get())) Some(complexType.getName -> attribute.get.getType)
        else None
    }).filter(_.nonEmpty).map(_.get).toMap

    /**
      * Checks if the given type is of enumeration type or not.
      *
      * @param element, a simple type element.
      * @return true if the given element type is of enumeration.
      */
    private def isEnumeration(element: XsdSimpleType): Boolean

    = Try(element.getRestriction.getEnumeration.size() > 0).getOrElse(false)

    /**
      * Checks if the attribute is of enumeration list or not.
      *
      * @param attribute
      * @return true if the attribute is of enumeration type.
      */
    private def isEnumerationList(attribute: XsdAttribute): Boolean

    = attribute.getType.endsWith(ScalarType.LIST)

}

object XMLSchema {

    /**
      * Load all the fhir namespaced schema only.
      */
    val fhirXMLSchema: XMLSchema = XMLSchema()

    /**
      * Class type elements.
      */
    private def classTypeElements: List[XsdElement] = fhirXMLSchema.getChildrenElements
        .filter(element => namesSet.contains(element.getName))

    private def formatResourceName(givenName: String): String =
        if (ClassNameUtility.hasReservedName(givenName)) ClassNameUtility.getKeywordClass(givenName)
        else givenName

    /**
      * Returns the list of complex types that are used to create the GraphQL type system.
      *
      * @return listOfClassTypeNames
      */
    private def typeNames: List[String] = {
        val givenNames = mutable.ListBuffer[String]()
        fhirXMLSchema.resourceContainerElements.foreach(item => givenNames += formatResourceName(item.getName))
        givenNames.toList
    }

    /**
      * Prepare all the types names in a set for validation.
      */
    private val namesSet = (typeNames :+ ScalarType.CLASS_ID).toSet

    /**
      * Enumeration types.
      */
    private val allEnumerationTypes: Map[String, XsdSimpleType] = fhirXMLSchema
        .backboneSimpleTypes.map(simpleType => simpleType.getName -> simpleType).toMap

    /**
      * All the generated enumerated classes.
      */
    private val enumeratedClasses: List[XsdSimpleType] = fhirXMLSchema.enumeratedClasses

    /**
      * Non Primitive type.
      *
      * @param attribute
      * @return true if the name is of non primitive type.
      */
    private def nonPrimitive(attribute: XsdAttribute): Boolean = !attribute.getType.endsWith(ScalarType.PRIMITIVE)

    /**
      * Checks if the attribute is of enumeration list or not.
      *
      * @param attribute
      * @return true if the attribute is of enumeration type.
      */
    private def isEnumerationList(attribute: XsdAttribute): Boolean = attribute.getType.endsWith(ScalarType.LIST)

    /**
      * Check if the complex type is derived from enumeration.
      */
    private def isDerivedEnumeration(typeItem: XsdComplexType): Boolean = Try {
        val attribute = typeItem.getComplexContent.getXsdExtension.getXsdAttributes.findFirst()
        if (!attribute.isPresent) false
        else isEnumerationList(attribute.get)
    }.getOrElse(false)

    /**
      * Base Type Mapping, Record the base type mapping to generate extra parameters.
      */
    private val baseTypeMappings: Map[String, String] = fhirXMLSchema.backboneComplexTypes.map(item => {
        val baseName = Try(item.getComplexContent.getXsdExtension.getBase.getName).getOrElse(ScalarType.EMPTY)
        ClassNameUtility.formatClassName(item.getName) -> baseName
    }).filter(_._2.nonEmpty).toMap


    private def isEligibleMap(attribute: util.Map[String, String]): Boolean = {
        attribute.containsKey(ScalarType.TYPE) && attribute.containsKey(ScalarType.NAME)
    }

    private def propertyMapOf(item: XsdElement): util.Map[String, String] = {
        val attributes: util.Map[String, String] = new util.HashMap[String, String]()
        attributes.put(ScalarType.MAX_OCCURS, Try(item.getMaxOccurs).getOrElse(ScalarType.EMPTY))
        attributes.put(ScalarType.MIN_OCCURS, Try(item.getMinOccurs.toString).getOrElse(ScalarType.EMPTY))
        attributes.put(ScalarType.TYPE, Try(item.getType).getOrElse(ScalarType.EMPTY))
        attributes.put(ScalarType.NAME, Try(item.getName).getOrElse(ScalarType.EMPTY))
        attributes
    }

    /**
      * Returns the list of of maps associated with given complex type.
      *
      * @param typeItem
      * @return listOfMaps extracted from the given complex type item.
      */
    private def listOfMaps(typeItem: XsdComplexType): List[util.Map[String, String]] = Try {
        val attributesMap = mutable.ListBuffer[util.Map[String, String]]()

        /**
          * Collect all the elements entries.
          */
        typeItem.getComplexContent.getXsdExtension.getChildAsSequence.getChildrenElements.forEach(item => {
            val attributes = propertyMapOf(item)
            if (isEligibleMap(attributes)) attributesMap += attributes
        })

        /*typeItem.getComplexContent.getXsdExtension.getXsdChildElement.getXsdElements.forEach(item => {
            val attributes = item.getAttributesMap
            if (isEligibleMap(attributes)) attributesMap += attributes
        })*/

        /**
          * Collect all the elements of choices type.
          */
        typeItem.getComplexContent.getXsdExtension.getChildAsSequence.getChildrenChoices.forEach(item => {
            item.getChildrenElements.forEach(child => {
                val attributes = child.getAttributesMap
                if (!attributes.containsKey(ScalarType.MIN_OCCURS)) attributes.put(ScalarType.MIN_OCCURS, item.getMinOccurs.toString)
                if (!attributes.containsKey(ScalarType.MAX_OCCURS)) attributes.put(ScalarType.MAX_OCCURS, item.getMaxOccurs)
                attributesMap += attributes
            })
        })

        /**
          * 1. Extract additional attributes for certain types like resources.
          */
        val baseTypeName = Try(typeItem.getComplexContent.getXsdExtension.getBase.getName).getOrElse(ScalarType.EMPTY)
        attributesMap ++= additionalAttributes(baseTypeName)

        /**
          * 2. Perform some inheritance mappings here.
          * 3. Needs to look into reserved key words in attributes like uid.
          */
        val newAttributes = attributesMap.toList.filter(mapItem => {
            mapItem.get(ScalarType.TYPE) != null
        }) /*.map(mapItem => {
            if (!TypeDefinitions.inheritanceTypes.contains(baseTypeName)) mapItem
            else {
                val typeName = mapItem.get(ScalarType.TYPE)
                mapItem.put(ScalarType.TYPE, getBaseTypeOf(typeName))
                mapItem
            }
        })*/ .map(mapItem => {
            val givenType = mapItem.get(ScalarType.TYPE).toLowerCase
            if (givenType == ScalarType.ID) mapItem.put(ScalarType.TYPE, ScalarType.CLASS_ID)
            mapItem
        })

        newAttributes
    }.getOrElse(List[util.Map[String, String]]())

    /**
      * Build map from class name.
      */
    private def buildMapFrom(typeName: String): List[util.Map[String, String]] = Try {
        listOfMaps(fhirXMLSchema.getChildrenComplexTypes.filter(_.getName == typeName).head)
    }.getOrElse(List[util.Map[String, String]]())

    /**
      * Resource Map for additional attributes.
      */
    private val resourceMap: List[util.Map[String, String]] = buildMapFrom(TypeDefinitions.RESOURCE)

    /**
      * Backbone type map for additional attributes.
      */
    private val backboneMap: List[util.Map[String, String]] = buildMapFrom(TypeDefinitions.BACKBONE_TYPE)

    /**
      * Returns the Base Type mapping of the given backbone/element.
      */
    private def getBaseTypeOf(complexTypeName: String): String =
        baseTypeMappings.getOrElse(ClassNameUtility.formatClassName(complexTypeName), TypeDefinitions.DATA_TYPE)

    /**
      * Type Level Documentation.
      *
      * @param annotation
      * @return documentation of the given type system.
      */
    private def typeDocumentations(annotation: XsdAnnotation): String = {
        if (annotation == null) ScalarType.EMPTY
        else {
            val annotations = mutable.ListBuffer[String]()
            annotation.getDocumentations.forEach(document => annotations += (ScalarType.GRAPHQL_COMMENT_PREFIX + document.getContent))
            annotations.toList.mkString(ScalarType.NEW_LINE)
        }
    }

    /**
      * Returns the list of given name of the data type elements.
      *
      * @return listOfDataTypeNames
      */
    private def dataTypeNames: List[String] = {
        val typeNames = mutable.ListBuffer[String]()
        fhirXMLSchema.getChildrenComplexTypes
            .filter(_.getName == TypeDefinitions.EXTENSION).head
            .getComplexContent.getXsdExtension.getChildAsSequence.getChildrenElements
            .forEach(item => typeNames += item.getName)
        typeNames.toList
    }


    /**
      * All the class definition extracted from the backbone element.
      *
      * @return mapOfClassDefinitions
      */
    private def backBoneElements: Map[String, List[util.Map[String, String]]] = {
        val allElements = mutable.Map[String, List[util.Map[String, String]]]()
        fhirXMLSchema.backboneComplexTypes.foreach(typeItem => allElements += (typeItem.getName -> listOfMaps(typeItem)))
        allElements.filter(_._2.nonEmpty).toMap
    }

    /**
      * All the class definition extracted from the backbone element.
      *
      * @return mapOfClassDefinitions.
      */
    private def domainResourceElements: Map[String, List[util.Map[String, String]]] = {
        val allElements = mutable.Map[String, List[util.Map[String, String]]]()
        fhirXMLSchema.domainResourceComplexTypes.foreach(typeItem => allElements += (typeItem.getName -> listOfMaps(typeItem)))
        //allElements.toList.sortBy(_._1).map(_._1).foreach(println)
        allElements.filter(_._2.nonEmpty).toMap
    }

    /**
      * All the class definition extracted from the backbone element.
      *
      * @return mapOfClassDefinitions.
      */
    private def resourceElements: Map[String, List[util.Map[String, String]]] = {
        val allElements = mutable.Map[String, List[util.Map[String, String]]]()
        fhirXMLSchema.resourceComplexTypes.foreach(typeItem => allElements += (typeItem.getName -> listOfMaps(typeItem)))
        allElements.filter(_._2.nonEmpty).toMap
    }

    /**
      * All the class definition of elements that should defined as a complex type.
      *
      * @return mapOfClassDefinitions.
      */
    private def complexTypeElements: Map[String, List[util.Map[String, String]]] = {
        val allElements = mutable.Map[String, List[util.Map[String, String]]]()
        fhirXMLSchema.complexElementTypes.foreach(typeItem => allElements += (typeItem.getName -> listOfMaps(typeItem)))
        allElements.filter(_._2.nonEmpty).toMap
    }

    /**
      * These are records to be replaced as their extension.
      * Duration has base Quantity, So Duration is derived from Quantity.
      */
    private def complexTypeExtensions: Map[String, String] =
        fhirXMLSchema.quantityComplexTypes.map(entry => entry.getName -> entry.getComplexContent.getXsdExtension.getBase.getName).toMap


    /**
      * Handles special case for like Resource (complex type) in case FHIR Schema.
      *
      * @return mapOfClassDefinitions.
      */
    private def complexResourceOnly: Map[String, List[util.Map[String, String]]] = {
        val allElements = mutable.Map[String, List[util.Map[String, String]]]()

        fhirXMLSchema.resourceOnlyComplexType.foreach(typeItem => {
            val attributesMap = mutable.ListBuffer[util.Map[String, String]]()

            typeItem.getChildAsSequence.getChildrenElements.forEach(item => {
                val attributes = propertyMapOf(item)
                if (isEligibleMap(attributes)) attributesMap += attributes
            })

            typeItem.getChildAsSequence.getChildrenChoices.forEach(item => {
                item.getChildrenElements.forEach(child => {
                    val attributes = child.getAttributesMap
                    if (!attributes.containsKey(ScalarType.MIN_OCCURS)) attributes.put(ScalarType.MIN_OCCURS, item.getMinOccurs.toString)
                    if (!attributes.containsKey(ScalarType.MAX_OCCURS)) attributes.put(ScalarType.MAX_OCCURS, item.getMaxOccurs)
                    attributesMap += attributes
                })
            })

            val baseTypeName = Try(typeItem.getComplexContent.getXsdExtension.getBase.getName).getOrElse(ScalarType.EMPTY)
            attributesMap ++= additionalAttributes(baseTypeName)

            val newAttributes = attributesMap.toList.filter(mapItem => {
                mapItem.get(ScalarType.TYPE) != null
            }).map(mapItem => {
                val givenType = mapItem.get(ScalarType.TYPE).toLowerCase
                if (givenType == ScalarType.ID) mapItem.put(ScalarType.TYPE, ScalarType.CLASS_ID)
                mapItem
            })

            allElements += (typeItem.getName -> newAttributes)
        })

        allElements.filter(_._2.nonEmpty).toMap
    }

    /**
      * Collect all the types available in the ontology system.
      * 1. Collect from domain resource elements.
      * 2. Collect from backBoneElements.
      * 3. Collect from complex elements.
      * 4. Collect extending from quantity complex types.
      * 5. Remove duplicate based on the given names.
      */
    private def allAvailableTypes: Map[String, List[util.Map[String, String]]] = {
        val elementDefinitions = complexTypeElements ++ domainResourceElements ++ backBoneElements ++ complexResourceOnly ++ resourceElements
        val complexTypes = complexTypeExtensions.map(entry => entry._1 -> elementDefinitions(entry._2))
        elementDefinitions ++ complexTypes
    }

    /**
      * Build Class properties.
      */
    private def buildClassProperties(attributes: util.Map[String, String]): Map[String, Map[String, Any]] = {
        val propertyName = attributes.get(ScalarType.NAME)
        val classInformation = mutable.Map[String, Any]()

        val minCardinality = attributes.getOrDefault(ScalarType.MIN_OCCURS, "0")
        val maxCardinality = attributes.getOrDefault(ScalarType.MAX_OCCURS, "0")
        val allValuesRestriction = attributes.getOrDefault(ScalarType.TYPE, ScalarType.EMPTY)
        val formattedAllValuesRestriction = ClassNameUtility.formatClassName(allValuesRestriction)

        if (minCardinality == maxCardinality && minCardinality == "1") classInformation += (ScalarType.CARDINALITY -> 1)
        else if (maxCardinality != minCardinality && maxCardinality != ScalarType.UNBOUNDED) {
            classInformation += (ScalarType.MIN_CARDINALITY -> minCardinality.toInt)
            classInformation += (ScalarType.MAX_CARDINALITY -> maxCardinality.toInt)
        }
        classInformation += (ScalarType.ALL_VALUES_RESTRICTION -> formattedAllValuesRestriction)

        Map[String, Map[String, Any]](propertyName -> classInformation.toMap)
    }

    /**
      * Extract additional parameter based on base type.
      */
    private def additionalAttributes(elementName: String): List[util.Map[String, String]] = elementName match {
        case TypeDefinitions.METADATA_RESOURCE | TypeDefinitions.DOMAIN_RESOURCE | TypeDefinitions.CANONICAL_RESOURCE | TypeDefinitions.RESOURCE => resourceMap
        case TypeDefinitions.BACKBONE_TYPE => backboneMap
        case _ => List[util.Map[String, String]]()
    }

    /**
      * Extracts class information from the given XsdElement.
      *
      * @param element
      * @return listOfMap
      */
    private def classInformationOf(element: XsdElement): List[util.Map[String, String]] = {
        val attributesMap = mutable.ListBuffer[util.Map[String, String]]()
        element.getXsdComplexType.getComplexContent.getXsdExtension.getXsdElements
            .forEach(item => attributesMap += item.getAttributesMap)
        /**
          * Extract additional attributes for certain types like resources.
          */
        val baseTypeName = Try(element.getXsdComplexType.getComplexContent.getXsdExtension.getBase.getName).getOrElse(ScalarType.EMPTY)
        attributesMap ++= additionalAttributes(baseTypeName)

        attributesMap.map(attributeMap => {
            val typeName = attributeMap.get(ScalarType.TYPE)
            val baseTypeName = getBaseTypeOf(typeName)
            if (!TypeDefinitions.inheritanceTypes.contains(baseTypeName)) attributeMap
            else {
                attributeMap.put(ScalarType.TYPE, baseTypeName)
                attributeMap
            }
        })

        attributesMap.toList
    }

    /**
      * Build list of class properties.
      */
    private def buildFromResourceClassProperties: List[ClassProperties] = classTypeElements.map(element => {
        val classInformation = classInformationOf(element).map(entry => buildClassProperties(entry)).reduce(_ ++ _)
        ClassProperties.of(element.getName, classInformation)
    })

    /**
      * Build the list of class properties to generate the GraphQL schema.
      *
      * @return listOfClassProperties
      */
    private def buildFromBackboneElements: List[ClassProperties] = {
        val listOfClassProperties = mutable.ListBuffer[ClassProperties]()

        val allElements = backBoneElements
            .map(entry => entry._1 -> entry._2.filter(entry => entry.containsKey(ScalarType.TYPE)))
            .filter(_._2.nonEmpty)

        allElements.keys.toList.sorted.map(elementKey => {
            val classInformation = allElements(elementKey).map(entry => buildClassProperties(entry)).reduce(_ ++ _)
            listOfClassProperties += ClassProperties.of(elementKey, classInformation)
        })
        listOfClassProperties.toList
    }

    private def buildFromResourceElements: List[ClassProperties] = {
        val listOfClassProperties = mutable.ListBuffer[ClassProperties]()
        val allElements = domainResourceElements
            .map(entry => entry._1 -> entry._2.filter(entry => entry.containsKey(ScalarType.TYPE)))
            .filter(_._2.nonEmpty)
        allElements.keys.toList.sorted.map(elementKey => {
            val classInformation = allElements(elementKey).map(entry => buildClassProperties(entry)).reduce(_ ++ _)
            listOfClassProperties += ClassProperties.of(elementKey, classInformation)
        })
        listOfClassProperties.toList
    }

    private def buildFromAllElements: List[ClassProperties] = {
        val listOfClassProperties = mutable.ListBuffer[ClassProperties]()
        val allElements = allAvailableTypes
            .map(entry => entry._1 -> entry._2.filter(entry => entry.containsKey(ScalarType.TYPE)))
            .filter(_._2.nonEmpty)

        allElements.keys.toList.sorted.map(elementKey => {
            val classInformation = allElements(elementKey).map(entry => buildClassProperties(entry)).reduce(_ ++ _)
            listOfClassProperties += ClassProperties.of(elementKey, classInformation)
        })
        listOfClassProperties.toList
    }

    /**
      * All property triples.
      * Class Name, property name, inner class name.
      * (className_propertyName -> innerClassName)
      */
    private val classAttributePair: Map[String, String] = {
        buildFromAllElements
            .map(propertyClass => propertyClass.ontologyClassName() -> propertyClass).toMap
            .flatMap(entry => entry._2.allPropertiesTriples())
            .map(entry => List(entry._1, entry._2).mkString("_") -> entry._3)
            .toMap
    }


    private def innerClassOf(className: String, attributeName: String): Option[String] =
        classAttributePair.get(className + "_" + attributeName)

    /**
      * Builds enumeration properties.
      *
      * @return listOfEnumerationProperties.
      */
    private def buildEnumerationProperties: List[EnumerationProperties] = {
        val enumElementsType: List[EnumerationProperties] = enumeratedClasses.map(enumItem => {
            val enumType = Try(ClassNameUtility.formatEnumClassName(enumItem.getName)).getOrElse(ScalarType.EMPTY)
            if (enumType.nonEmpty) (enumItem.getName, enumType, enumItem)
            else (ScalarType.EMPTY, ScalarType.EMPTY, null)
        }).filter(_._1.nonEmpty).map(entry => EnumerationProperties.of(entry._2, entry._3))

        /**
          * Resource Container for special purpose.
          */
        enumElementsType :+ EnumerationProperties.of(TypeDefinitions.RESOURCE_CONTAINER, typeNames)
    }

    private def singleDepthOf(givenPath: String): String = {
        val parts = givenPath.split("\\.")
        if (parts.length > 2) {
            val innerClass = innerClassOf(parts(0), parts(1))
            if (innerClass.isDefined) singleDepthOf((List(innerClass.get) ++ parts.drop(2)).mkString("."))
            else givenPath
        } else givenPath
    }

    /**
      * Perform mapping of union/reference types here.
      *
      * @param referenceClasses
      * @param referencePaths
      * @return mapOfExtractedUnionTypes
      */
    private def reconcileReferences(referenceClasses: Map[String, String], referencePaths: Map[String, List[String]]): Map[String, List[String]] = {
        val reconcileResult = mutable.Map[String, List[String]]()

        /**
          * 1. Find common keys and add to the reconcile result.
          */
        val commonKeys = referenceClasses.keySet & referencePaths.keySet
        commonKeys.foreach(pathKey => reconcileResult += (pathKey -> referencePaths(pathKey)))

        /**
          * 2. Find keys that are present in either reference class or reference paths.
          */
        var onlyInReferenceClasses = referenceClasses.keySet diff commonKeys
        var onlyInReferencePaths = referencePaths.keySet diff commonKeys

        /**
          * 3. Add those reference types altering either the class name with "_", it is for special
          * names that does supported by GraphQL or contains the suffix of "Reference".
          */
        onlyInReferencePaths.foreach(path => {
            val underscoreName = path.replaceAll("\\.", "_.")
            val referenceName = List(path, ScalarType.REFERENCE).mkString
            if (onlyInReferenceClasses.contains(underscoreName)) reconcileResult += (underscoreName -> referencePaths(path))
            else if (onlyInReferenceClasses.contains(referenceName)) reconcileResult += (referenceName -> referencePaths(path))
        })

        reconcileResult.toMap
    }

    /**
      * Builds union classes from all the elements in the ontology schema definition.
      *
      * @return listOfClasses
      */
    private def buildUnionClasses: List[WiseUnionClass] = {
        /**
          * 1. Class Property Triples.
          * className.propertyName -> innerClass.
          */
        val classPropertyInnerClass: Map[String, String] = buildFromAllElements
            .map(propertyClass => propertyClass.ontologyClassName() -> propertyClass).toMap
            .flatMap(entry => entry._2.allPropertiesTriples())
            .map(entry => List(entry._1, entry._2).mkString(".") -> entry._3)
            .filter(_._2 == ScalarType.REFERENCE).toMap

        /**
          * Work for path reducing to only single depth.
          * Class1.attr1.attr2 => InnerClass1Attr1.attr2.
          * Ignored for the class backbone element.
          */
        val reducedPath: Map[String, List[String]] = FHIRProfiles.collectUnionTargets.map(entry => singleDepthOf(entry._1) -> entry._2)
        /**
          * Reconcile reference class path with the references types.
          */
        val reconcileResults: Map[String, List[String]] = reconcileReferences(classPropertyInnerClass, reducedPath)

        /**
          * Collect all the union resolved paths, It does contains path with their resolution option for Union types.
          */
        val unionResolvedPaths: Map[String, String] = FHIRSearch.collectResolutionClasses
            .map(entry => (singleDepthOf(entry._2.rebuildPathExpression), entry._2.targetPath))
            .filter(entry => reconcileResults.getOrElse(entry._1, List[String]()).size > 1)
            .filter(entry => entry._2.isDefined)
            .map(entry => entry._1 -> entry._2.get)

        /**
          * Merge union resolved path and union associated reconciled paths.
          */
        val reconcileWithResolutionPath: Map[String, (List[String], Option[String])] = reconcileResults
                .map(entry => entry._1 -> (entry._2, unionResolvedPaths.get(entry._1)))

        WiseUnionClass.batchesOf(reconcileWithResolutionPath)
    }

    /**
      * Returns the list of class properties from the XSD schema file.
      */
    //def listOfClassProperties: List[ClassProperties] = buildFromBackboneElements
    def listOfClassProperties: List[ClassProperties] = buildFromAllElements

    /**
      * Returns the list of enumeration properties from the XSD schema file.
      */
    def listOfEnumerationProperties: List[EnumerationProperties] = buildEnumerationProperties

    /**
      * Returns the map of union classes.
      */
    def listOfUnionClasses: List[WiseUnionClass] = buildUnionClasses

}
