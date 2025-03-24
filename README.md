## Ontology-to-GraphQL-Schema-Generation
GraphQL schema generation for DGraph from FHIR[https://hl7.org/FHIR/] ontology. Currently it supports XML format. 

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
4. 
