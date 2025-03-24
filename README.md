## Ontology-to-GraphQL-Schema-Generation
GraphQL schema generation for DGraph from FHIR[https://hl7.org/FHIR/] ontology. This work is intended for schema auto generation to DGraph[https://dgraph.io/] database. Currently it supports XML format. 

For Example: 

1. Input: FHIR schema definition in XML.
   
```XML
<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://hl7.org/fhir" xmlns:xhtml="http://www.w3.org/1999/xhtml" targetNamespace="http://hl7.org/fhir" elementFormDefault="qualified" version="1.0">
  <xs:include schemaLocation="fhir-base.xsd"/>
  <xs:element name="Organization" type="Organization">
    <xs:annotation>
      <xs:documentation xml:lang="en">A formally or informally recognized grouping of people or organizations formed for the purpose of achieving some form of collective action.  Includes companies, institutions, corporations, departments, community groups, healthcare practice groups, payer/insurer, etc.</xs:documentation>
    </xs:annotation>
  </xs:element>
  <xs:complexType name="Organization">
    <xs:annotation>
      <xs:documentation xml:lang="en">A formally or informally recognized grouping of people or organizations formed for the purpose of achieving some form of collective action.  Includes companies, institutions, corporations, departments, community groups, healthcare practice groups, payer/insurer, etc.</xs:documentation>
      <xs:documentation xml:lang="en">If the element is present, it must have either a @value, an @id, or extensions</xs:documentation>
    </xs:annotation>
    <xs:complexContent>
      <xs:extension base="DomainResource">
        <xs:sequence>
          <xs:element name="identifier" minOccurs="0" maxOccurs="unbounded" type="Identifier">
            <xs:annotation>
              <xs:documentation xml:lang="en">Identifier for the organization that is used to identify the organization across multiple disparate systems.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="active" minOccurs="0" maxOccurs="1" type="boolean">
            <xs:annotation>
              <xs:documentation xml:lang="en">Whether the organization's record is still in active use.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="type" minOccurs="0" maxOccurs="unbounded" type="CodeableConcept">
            <xs:annotation>
              <xs:documentation xml:lang="en">The kind(s) of organization that this is.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="name" minOccurs="0" maxOccurs="1" type="string">
            <xs:annotation>
              <xs:documentation xml:lang="en">A name associated with the organization.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="alias" minOccurs="0" maxOccurs="unbounded" type="string">
            <xs:annotation>
              <xs:documentation xml:lang="en">A list of alternate names that the organization is known as, or was known as in the past.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="telecom" minOccurs="0" maxOccurs="unbounded" type="ContactPoint">
            <xs:annotation>
              <xs:documentation xml:lang="en">A contact detail for the organization.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="address" minOccurs="0" maxOccurs="unbounded" type="Address">
            <xs:annotation>
              <xs:documentation xml:lang="en">An address for the organization.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="partOf" minOccurs="0" maxOccurs="1" type="Reference">
            <xs:annotation>
              <xs:documentation xml:lang="en">The organization of which this organization forms a part.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="contact" type="Organization.Contact" minOccurs="0" maxOccurs="unbounded">
            <xs:annotation>
              <xs:documentation xml:lang="en">Contact for the organization for a certain purpose.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="endpoint" minOccurs="0" maxOccurs="unbounded" type="Reference">
            <xs:annotation>
              <xs:documentation xml:lang="en">Technical endpoints providing access to services operated for the organization.</xs:documentation>
           </xs:annotation>
          </xs:element>
        </xs:sequence>
      </xs:extension>
    </xs:complexContent>
  </xs:complexType>
  <xs:complexType name="Organization.Contact">
    <xs:annotation>
      <xs:documentation xml:lang="en">A formally or informally recognized grouping of people or organizations formed for the purpose of achieving some form of collective action.  Includes companies, institutions, corporations, departments, community groups, healthcare practice groups, payer/insurer, etc.</xs:documentation>
    </xs:annotation>
    <xs:complexContent>
      <xs:extension base="BackboneElement">
        <xs:sequence>
          <xs:element name="purpose" minOccurs="0" maxOccurs="1" type="CodeableConcept">
            <xs:annotation>
              <xs:documentation xml:lang="en">Indicates a purpose for which the contact can be reached.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="name" minOccurs="0" maxOccurs="1" type="HumanName">
            <xs:annotation>
              <xs:documentation xml:lang="en">A name associated with the contact.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="telecom" minOccurs="0" maxOccurs="unbounded" type="ContactPoint">
            <xs:annotation>
              <xs:documentation xml:lang="en">A contact detail (e.g. a telephone number or an email address) by which the party may be contacted.</xs:documentation>
           </xs:annotation>
          </xs:element>
          <xs:element name="address" minOccurs="0" maxOccurs="1" type="Address">
            <xs:annotation>
              <xs:documentation xml:lang="en">Visiting or postal addresses for the contact.</xs:documentation>
           </xs:annotation>
          </xs:element>
        </xs:sequence>
      </xs:extension>
    </xs:complexContent>
  </xs:complexType>
</xs:schema>
```   
   
2. Output:
```GraphQL
#----------------------------------*** Schema Definition Started ***----------------------------------

# ----------------------------------------
# Author: Santa Basnet
# Company: Integrated ICT.
# Date: Fri Jul 30 11:21:07 NPT 2021
# ----------------------------------------

#----------------------------------*** Enumeration Types Section ***----------------------------------

#...
enum AddressUse {
    Home
	Work
	Temp
	Old
	Billing
}

enum AddressType {
    Postal
	Physical
	Both
}
#... and so on.
#--------------------------------------*** Union Types Section ***-------------------------------------        

union ContractActionContext = Encounter | EpisodeOfCare

union ObservationDevice = Device | DeviceMetric

union ObservationHasMember = MolecularSequence | Observation | QuestionnaireResponse
#... and so on.


type Money {
    id: String! @id
	value: Float
	currency: String
}

type Organization {
      name: String @search(by: [fulltext])
      identifier: [Identifier]
      contact: [OrganizationContact]
     	alias: [String] @search(by: [fulltext])
     	id: String! @id
     	partOf: Organization
     	endpoint: [Endpoint]
     	telecom: [ContactPoint]
     	address: [Address]
     	type: [CodeableConcept]
     	active: Boolean @search
}

# ... and so on.
#----------------------------------*** Schema Definition Completed ***----------------------------------
```

3. Core Implementation:
```Scala
    /**
      * Perform schema generation here.
      */
    def buildGraphQLSchema(): Unit = {
        /**
          * Schema are divided according to their sections.
          */
        val splitSchemas: Map[String, List[String]] = SchemaClasses
            .of(enumerationProperties, uniqueClassProperties, inverseRelation)
            .splitSchema

        val singleSchema: List[String] = SchemaClasses.schemaTypes.flatMap(schemaType => {
            val schema = splitSchemas(schemaType)
            val generatedSchema = mutable.ListBuffer[String]()
            generatedSchema += ClassNameUtility.openingMessage()
            generatedSchema += ClassNameUtility.authorInfo()
            schemaType match {
                case SchemaClasses.ENUM_SCHEMA => generatedSchema += ClassNameUtility.enumTypeHeader()
                case SchemaClasses.UNION_DEFINITION => generatedSchema += ClassNameUtility.unionTypeHeader()
                case _ => generatedSchema += ClassNameUtility.classTypeHeader()
            }
            generatedSchema ++= schema
            generatedSchema += ClassNameUtility.closingMessage()
            generatedSchema.toList
        })

        writeSchema(singleSchema)
        writeSchemaInformation(enumerationProperties, uniqueClassProperties)
    }
```   
4. Implementation example of Inverse Relation:
   
```Scala
/**
  * This class is a part of the package com.wiseyak.ontology and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-18.
  *
  * * Build the property class triple.
  * * Example:
  * *
  * * Step 1.
  * * C1  --  P1  --  C13
  * * C2  --  P2  --  C10
  * * ...
  * * C10 --  P11 --  C2
  * *
  * * Step 2.
  * * P2  =>  [C2, C10]
  * * P11 =>  [C2, C10]
  * *
  * * Step 3.
  * * P2 and P11 has inverse relation with some criteria.
  *     a. P1   --  C13 should have single cardinality.
  *     b. P11  --  C2 should have exactly one cardinality.
  *     c. P11 has inverse relation with P1 with field P11.
  *
  */
case class InverseRelation(propertyClasses: List[ClassProperties]) {

    /**
      * Defines the association of class name and the property class.
      */
    private val classDefinitionWithProperties: Map[String, ClassProperties] =
        propertyClasses.map(propertyClass => propertyClass.ontologyClassName() -> propertyClass).toMap

    /**
      * Generates the pair of class names.
      *
      * @param outerClass
      * @param innerClass
      * @return classesPair
      */
    private def namePair(outerClass: String, innerClass: String): String =
        List(outerClass, innerClass).sorted.mkString(ScalarType.UNDERSCORE)

    /**
      * Pair Classes.
      */
    private val allClassPairs: Map[String, List[String]] = {
        propertyClasses.flatMap(_.listTriples())
            .filterNot(triple => triple._1 == ScalarType.REFERENCE || triple._3 == ScalarType.REFERENCE)
            .map(entry => (entry._2, namePair(entry._1, entry._3)))
            .groupBy(_._2).map(entry => entry._1 -> entry._2.map(_._1).distinct)
            .filter(_._2.size > 1) // Should have at least two properties in common.
    }

    /**
      * Cyclic relations of the class properties to the parent class.
      *
      * @param sourceClass
      * @param givenClassName
      * @return listOfProperties with classes.
      */
    private def extractCyclicProperties(sourceClass: String, givenClassName: String): List[(String, String)] = Try {
        classDefinitionWithProperties(sourceClass).listOfProperties.filter(_._2 == givenClassName)
    }.getOrElse(List[(String, String)]())

    /**
      * Returns the property cardinality in the given class context.
      *
      * @param className
      * @param propertyName
      * @return definedCardinality
      */
    private def cardinalityOf(className: String, propertyName: String): String =
        classDefinitionWithProperties(className).propertiesCardinality.getOrElse(propertyName, ScalarType.NONE)

    /**
      * Validity criteria for inverse relation extraction.
      *
      * @param source
      * @param target
      * @return true if the inverse relation extraction criteria is valid.
      */
    private def validCardinality(source: String, target: String): Boolean =
        source.equals(ClassProperties.ZERO_OR_MORE) && (target.equals(ClassProperties.EXACTLY_ONE) || target.equals(ClassProperties.ZERO_OR_ONE))

    /**
      * Build Inverse Property relation from the given property definitions in tuples.
      *
      * @param givenProperty
      * @param targetProperty
      * @return inverseProperty relation.
      */
    private def buildInverseProperty(givenProperty: (String, String), targetProperty: (String, String)): InverseProperty = {
        val sourceCardinality = cardinalityOf(targetProperty._2, givenProperty._1)
        val targetCardinality = cardinalityOf(givenProperty._2, targetProperty._1)
        if (!validCardinality(sourceCardinality, targetCardinality)) InverseProperty.empty
        else InverseProperty.of(givenProperty, targetProperty)
    }

    /**
      * Verify the eligibility of inverse relation, and returns the
      * formatted inverse directive.
      *
      * @return (true, valueString) after validation of eligibility.
      */
    def eligible(propertyName: String, instanceOfClass: String): (Boolean, String) = {
        if (!Configuration.needsInverseRelation) (false, ScalarType.EMPTY)
        else if (!classDefinitionWithProperties.contains(instanceOfClass)) (false, ScalarType.EMPTY)
        else {
            val eligibleProperties = classDefinitionWithProperties(instanceOfClass).listOfProperties
                .filter(propertyWithClass => allClassPairs.contains(namePair(instanceOfClass, propertyWithClass._2)))
            val cyclicProperties = eligibleProperties.map(item => {
                val cyclicProperties = extractCyclicProperties(item._2, instanceOfClass).headOption
                if (cyclicProperties.isEmpty) InverseProperty.empty else buildInverseProperty((propertyName, instanceOfClass), item)
            }).filter(_.nonEmpty)
            if (cyclicProperties.isEmpty) (false, ScalarType.EMPTY) else (true, cyclicProperties.head.getDefinition)
        }
    }

    /**
      * Get all the unique classes.
      */
    def uniquePropertyClasses: List[ClassProperties] = propertyClasses
}

/**
  * The companion object for Inverse Relation information.
  */
object InverseRelation {
    /**
      * Property Name type.
      */
    type PropertyName = String

    /**
      * Type definition for field name.
      */
    type FieldName = String

    /**
      * Factory definition.
      */
    def of(propertyClasses: List[ClassProperties]): InverseRelation = InverseRelation(propertyClasses)

    def empty() = InverseRelation(List())
}
```
