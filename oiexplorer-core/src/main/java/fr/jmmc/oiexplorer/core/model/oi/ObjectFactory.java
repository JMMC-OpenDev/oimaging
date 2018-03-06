
package fr.jmmc.oiexplorer.core.model.oi;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fr.jmmc.oiexplorer.core.model.oi package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fr.jmmc.oiexplorer.core.model.oi
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link OiDataCollection }
     * 
     */
    public OiDataCollection createOiDataCollection() {
        return new OiDataCollection();
    }

    /**
     * Create an instance of {@link OIDataFile }
     * 
     */
    public OIDataFile createOIDataFile() {
        return new OIDataFile();
    }

    /**
     * Create an instance of {@link SubsetDefinition }
     * 
     */
    public SubsetDefinition createSubsetDefinition() {
        return new SubsetDefinition();
    }

    /**
     * Create an instance of {@link Plot }
     * 
     */
    public Plot createPlot() {
        return new Plot();
    }

    /**
     * Create an instance of {@link View }
     * 
     */
    public View createView() {
        return new View();
    }

    /**
     * Create an instance of {@link TargetUID }
     * 
     */
    public TargetUID createTargetUID() {
        return new TargetUID();
    }

    /**
     * Create an instance of {@link TableUID }
     * 
     */
    public TableUID createTableUID() {
        return new TableUID();
    }

    /**
     * Create an instance of {@link Identifiable }
     * 
     */
    public Identifiable createIdentifiable() {
        return new Identifiable();
    }

}
