package com.wiseyak.xml

/**
  * This class is a part of the package com.wiseyak.xml and the package
  * is a part of the project ontologytool.
  *
  * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
  * https://www.integratedict.com.np
  *
  * Created by Santa on 2020-08-26.
  */
object TypeDefinitions {

    /**
      * Root Type Definitions, are used to create the class types, ResourceContainer literals.
      */
    val RESOURCE_CONTAINER: String = "ResourceContainer"

    /**
      * Extension Literal.
      */
    val EXTENSION: String = "Extension"

    /**
      * BackboneElement literal.
      */
    val BACKBONE_ELEMENT: String = "BackboneElement"

    /**
      * BackboneType literal.
      */
    val BACKBONE_TYPE: String = "BackboneType"

    /**
      * Base literal.
      */
    val BASE: String = "Base"

    /**
      * CanonicalResource literal.
      */
    val CANONICAL_RESOURCE: String = "CanonicalResource"

    /**
      * DataType literal.
      */
    val DATA_TYPE: String = "DataType"

    /**
      * DomainResource literal.
      */
    val DOMAIN_RESOURCE: String = "DomainResource"

    /**
      * An Element literal.
      */
    val ELEMENT: String = "Element"

    /**
      * MetadataResource literal.
      */
    val METADATA_RESOURCE = "MetadataResource"

    /**
      * Quantity literal.
      */
    val QUANTITY = "Quantity"

    /**
      * Resource literal.
      */
    val RESOURCE = "Resource"

    /**
      * Reserved Words.
      */
    private val dgraphReservedAttributes: Map[String, String] = Map(
        "uid" -> "uniqueId"
    )

    /**
      * Pass the variable name to reserved words.
      */
    def escapeReservedWords(attributeName: String): String = dgraphReservedAttributes.getOrElse(attributeName, attributeName)

    /**
      * All Basic types and other attribute associated types.
      */
    val baseTypes: List[String] = List(BACKBONE_ELEMENT, BASE, CANONICAL_RESOURCE, DATA_TYPE, DOMAIN_RESOURCE, ELEMENT, METADATA_RESOURCE, QUANTITY, RESOURCE)

    /**
      * Needs to do basic type replacements from the complex type.
      */
    val inheritanceTypes: List[String] = List(QUANTITY, METADATA_RESOURCE, CANONICAL_RESOURCE, DOMAIN_RESOURCE)

}
