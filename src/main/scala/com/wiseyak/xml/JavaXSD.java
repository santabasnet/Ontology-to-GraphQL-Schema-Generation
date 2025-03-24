package com.wiseyak.xml;

import tr.com.srdc.ontmalizer.XML2OWLMapper;
import tr.com.srdc.ontmalizer.XSD2OWLMapper;

import java.io.File;
import java.io.FileOutputStream;

/**
 * This class is a part of the package com.wiseyak.xml and the package
 * is a part of the project ontologytool.
 * <p>
 * Integrated ICT Pvt. Ltd. Jwagal, Lalitpur, Nepal.
 * https://www.integratedict.com.np
 * <p>
 * Created by Santa on 2020-09-04.
 */
public class JavaXSD {
    
    public static void main(String[] args) {
        // This part converts XML schema to OWL ontology.
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File("resources/xml/fhir-single.xsd"));
        mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();
    
        // This part converts XML instance to RDF data model.
        //XML2OWLMapper generator = new XML2OWLMapper(new File("src/test/resources/CDA/SALUS-sample-full-CDA-instance.xml"), mapping);
        //generator.convertXML2OWL();
    
        // This part prints the RDF data model to the specified file.
        try{
            File f = new File("resources/ttl/demo.n3");
            f.getParentFile().mkdirs();
            FileOutputStream fout = new FileOutputStream(f);
            mapping.writeOntology(fout, "N3");
            fout.close();
        
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
