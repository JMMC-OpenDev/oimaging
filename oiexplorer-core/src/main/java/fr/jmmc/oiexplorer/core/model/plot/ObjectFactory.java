
package fr.jmmc.oiexplorer.core.model.plot;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fr.jmmc.oiexplorer.core.model.plot package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fr.jmmc.oiexplorer.core.model.plot
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PlotDefinition }
     * 
     */
    public PlotDefinition createPlotDefinition() {
        return new PlotDefinition();
    }

    /**
     * Create an instance of {@link PlotDefinitions }
     * 
     */
    public PlotDefinitions createPlotDefinitions() {
        return new PlotDefinitions();
    }

    /**
     * Create an instance of {@link Axis }
     * 
     */
    public Axis createAxis() {
        return new Axis();
    }

    /**
     * Create an instance of {@link Range }
     * 
     */
    public Range createRange() {
        return new Range();
    }

}
