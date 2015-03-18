
package fr.jmmc.oiexplorer.core.model.plot;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import fr.jmmc.oiexplorer.core.model.OIBase;


/**
 * 
 *                 This type describes a list plot definition.
 *             
 * 
 * <p>Java class for PlotDefinitions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PlotDefinitions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="plotDefinition" type="{http://www.jmmc.fr/oiexplorer-core-plot-definition/0.1}PlotDefinition" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlotDefinitions", propOrder = {
    "plotDefinitions"
})
@XmlRootElement(name = "plotDefinitions")
public class PlotDefinitions
    extends OIBase
{

    @XmlElement(name = "plotDefinition", required = true)
    protected List<PlotDefinition> plotDefinitions;

    /**
     * Gets the value of the plotDefinitions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the plotDefinitions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlotDefinitions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PlotDefinition }
     * 
     * 
     */
    public List<PlotDefinition> getPlotDefinitions() {
        if (plotDefinitions == null) {
            plotDefinitions = new ArrayList<PlotDefinition>();
        }
        return this.plotDefinitions;
    }
    
//--simple--preserve
    /**
     * toString() implementation using string builder
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        super.toString(sb, full); // OIBase
        if (full) {
            sb.append("{plotDefinitions=");
            fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.plotDefinitions);
            sb.append('}');
        }
    }
//--simple--preserve

}
