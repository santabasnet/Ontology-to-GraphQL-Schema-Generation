package com.wiseyak.xml

import java.util
import java.util.Optional
import java.util.stream.Collectors

import com.wiseyak.conf.Configuration
import com.wiseyak.graphql.ClassNameUtility
import com.wiseyak.ontology.{ClassProperties, EnumerationProperties, ScalarType}
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


object XMLSchema_OLD {

    /**
      * FHIR namespace.
      */
    private val fhirNameSpace = "http://hl7.org/fhir"

    /**
      * Load XML schema from file here.
      */
    private val xmlSchema = new XsdParser(Configuration.ontologyInputFile)


    /**
      * Load all the fhir namespaced schema only.
      */
    private def fhirXMLSchema: Optional[XsdSchema] = xmlSchema.getResultXsdSchemas
        .filter(_.getTargetNamespace == fhirNameSpace).findFirst()

    /**
      * Class type elements.
      */
    private def classTypeElements: util.List[XsdElement] = fhirXMLSchema.get()
        .getChildrenElements
        .filter(element => namesSet.contains(element.getName))
        .collect(Collectors.toList[XsdElement])

    /**
      * Returns the list of complex types that are used to create the GraphQL type system.
      *
      * @return listOfClassTypeNames
      */
    private def typeNames: List[String] = {
        val typeNames = mutable.ListBuffer[String]()
        if (fhirXMLSchema.isPresent) {
            val result = fhirXMLSchema.get().getChildrenComplexTypes
                .filter(_.getName == TypeDefinitions.RESOURCE_CONTAINER).collect(Collectors.toList[XsdComplexType])
            result.get(0).getXsdChildElement.getXsdElements.forEach(item => {
                typeNames += item.asInstanceOf[XsdElement].getName
            })
        }
        typeNames.toList
    }

    /**
      * Checks if the given type is of enumeration type or not.
      *
      * @param element
      * @return true if the given element type is of enumeration.
      */
    private def isEnumeration(element: XsdSimpleType): Boolean =
        Try(element.getRestriction.getEnumeration.size() > 0).getOrElse(false)

    /**
      * Backbone Complex Types.
      */
    private def backboneComplexTypes: util.List[XsdComplexType] = Try {
        fhirXMLSchema.get().getChildrenComplexTypes
            .filter(_.getName == TypeDefinitions.BACKBONE_ELEMENT).collect(Collectors.toList[XsdComplexType])
            .get(0).getXsdSchema.getChildrenComplexTypes.collect(Collectors.toList[XsdComplexType])
    }.getOrElse(new util.ArrayList[XsdComplexType]())

    /**
      * Backbone Simple Types.
      */
    private def backboneSimpleTypes: util.List[XsdSimpleType] = Try {
        fhirXMLSchema.get().getChildrenSimpleTypes
            .filter(item => isEnumeration(item))
            .collect(Collectors.toList[XsdSimpleType])
    }.getOrElse(new util.ArrayList[XsdSimpleType]())

    /**
      * Enumeration types.
      */
    private val allEnumerationTypes: Map[String, XsdSimpleType] = {
        val items = mutable.Map[String, XsdSimpleType]()
        backboneSimpleTypes.forEach(simpleType => items += simpleType.getName -> simpleType)
        items.toMap
    }

    /**
      * All the generated enumerated classes.
      */
    private val enumeratedClasses = mutable.ListBuffer[XsdComplexType]()

    /**
      * Enumeration mappings.
      */
    private val enumerationMappings = mutable.Map[String, String]()

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
        else {
            val result = isEnumerationList(attribute.get)
            if (result) enumerationMappings += typeItem.getName -> attribute.get.getType
            result
        }
    }.getOrElse(false)

    /**
      * Base Type Mapping, Record the base type mapping to generate extra parameters.
      */
    private val baseTypeMappings: Map[String, String] = {
        val result = mutable.Map[String, String]()
        backboneComplexTypes.forEach(item => {
            val baseName = Try(item.getComplexContent.getXsdExtension.getBase.getName).getOrElse(ScalarType.EMPTY)
            if (baseName.nonEmpty) result += ClassNameUtility.formatClassName(item.getName) -> baseName
        })
        result.toMap
    }

    /**
      * Returns the list of of maps associated with given complex type.
      *
      * @param typeItem
      * @return listOfMaps extracted from the given complex type item.
      */
    private def listOfMaps(typeItem: XsdComplexType): List[util.Map[String, String]] = Try {
        val attributesMap = mutable.ListBuffer[util.Map[String, String]]()
        typeItem.getComplexContent.getXsdExtension.getXsdChildElement.getXsdElements.forEach(item => attributesMap += {
            val attributes = item.getAttributesMap
            if (!attributes.containsKey(ScalarType.TYPE)) attributes.put(ScalarType.TYPE, ScalarType.DEFAULT_TYPE)
            attributes
        })

        /**
          * 1. Extract additional attributes for certain types like resources.
          */
        val baseTypeName = Try(typeItem.getComplexContent.getXsdExtension.getBase.getName).getOrElse(ScalarType.EMPTY)
        attributesMap ++= additionalAttributes(baseTypeName)

        /**
          * 2. Perform some inheritance mappings here.
          */
        attributesMap.map(mapItem => {
            val typeName = mapItem.get(ScalarType.TYPE)
            val baseTypeName = getBaseTypeOf(typeName)
            if (!TypeDefinitions.inheritanceTypes.contains(baseTypeName)) mapItem
            else {
                mapItem.put(ScalarType.TYPE, baseTypeName)
                mapItem
            }
        })

        /**
          * 3. Needs to look into reserved key words in attributes like uid.
          */
        attributesMap.map(mapItem => {
            val givenType = mapItem.get(ScalarType.TYPE).toLowerCase
            if (givenType == ScalarType.ID) mapItem.put(ScalarType.TYPE, ScalarType.CLASS_ID)
            mapItem
        })

        attributesMap.toList
    }.getOrElse(List[util.Map[String, String]]())

    /**
      * Build map from class name.
      */
    private def buildMapFrom(typeName: String): List[util.Map[String, String]] = Try {
        val resourceType = fhirXMLSchema.get().getChildrenComplexTypes.filter(_.getName == typeName)
            .collect(Collectors.toList[XsdComplexType]).get(0)
        listOfMaps(resourceType)
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
        if (fhirXMLSchema.isPresent) {
            val result = fhirXMLSchema.get().getChildrenComplexTypes
                .filter(_.getName == TypeDefinitions.EXTENSION).collect(Collectors.toList[XsdComplexType])
            result.get(0).getComplexContent.getXsdExtension.getChildAsSequence.getChildrenElements.forEach(item => {
                typeNames += item.getName
            })
        }
        typeNames.toList
    }

    /**
      * All the class definition extracted from the backbone element.
      *
      * @return mapOfClassDefinitions
      */
    private def backBoneElements: Map[String, List[util.Map[String, String]]] = {
        val allElements = mutable.Map[String, List[util.Map[String, String]]]()
        backboneComplexTypes.forEach(typeItem => {
            /**
              * Enumerated class.
              */
            if (isDerivedEnumeration(typeItem)) enumeratedClasses += typeItem

            allElements += (typeItem.getName -> listOfMaps(typeItem))
        })
        allElements.filter(_._2.nonEmpty).toMap
    }

    /**
      * Prepare all the types names in a set for validation.
      */
    private val namesSet = (typeNames :+ "ID").toSet

    private val absentNames = mutable.Set[String]()


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

        if (!namesSet.contains(allValuesRestriction)) absentNames += allValuesRestriction

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
    private def buildFromResourceClassProperties: List[ClassProperties] = {
        val typeElements = classTypeElements
        val listOfClassProperties = mutable.ListBuffer[ClassProperties]()
        typeElements.forEach(element => {
            val classInformation = classInformationOf(element).map(entry => buildClassProperties(entry)).reduce(_ ++ _)
            listOfClassProperties += ClassProperties.of(element.getName, classInformation)
        })
        listOfClassProperties.toList
    }

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

    private def buildEnumerationProperties: List[EnumerationProperties] = {
        val enumElementsType = enumeratedClasses.map(enumItem => {
            val enumType = Try {
                val name = enumItem.getComplexContent.getXsdExtension.getXsdAttributes.findFirst().get.getType
                ClassNameUtility.formatEnumClassName(name)
            }.getOrElse(ScalarType.EMPTY)

            val enumElementType: Option[XsdSimpleType] = allEnumerationTypes.get(enumType)
            if (enumElementType.isDefined) (enumItem.getName, enumType, enumElementType.get)
            else (ScalarType.EMPTY, ScalarType.EMPTY, null)

        }).filter(_._1.nonEmpty).map(entry => EnumerationProperties.of(entry._1, entry._3))

        /**
          * Resource Container for special purpose.
          */
        enumElementsType += EnumerationProperties.of(TypeDefinitions.RESOURCE_CONTAINER, typeNames)
        enumElementsType.toList
    }

    /**
      * Returns the list of class properties from the XSD schema file.
      */
    def listOfClassProperties: List[ClassProperties] = buildFromBackboneElements

    /**
      * Returns the list of enumeration properties from the XSD schema file.
      */
    def listOfEnumerationProperties: List[EnumerationProperties] = buildEnumerationProperties


    /**
      *
      * @param args
      */

    def main(args: Array[String]): Unit = {
        //println(buildFromBackboneElements.size)
        //println(buildEnumerationProperties.size)
        println(typeNames.size)
    }

}
