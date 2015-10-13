
package fr.jmmc.oiexplorer.core.model.oi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;


/**
 * 
 *                 This type describes a plot instance.
 *             
 * 
 * <p>Java class for Plot complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Plot">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.jmmc.fr/oiexplorer-data-collection/0.1}View">
 *       &lt;sequence>
 *         &lt;element name="plotDefinition" type="{http://www.w3.org/2001/XMLSchema}IDREF"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Plot", propOrder = {
    "plotDefinition"
})
public class Plot
    extends View
{

    @XmlElement(required = true, type = Object.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected PlotDefinition plotDefinition;

    /**
     * Gets the value of the plotDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public PlotDefinition getPlotDefinition() {
        return plotDefinition;
    }

    /**
     * Sets the value of the plotDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setPlotDefinition(PlotDefinition value) {
        this.plotDefinition = value;
    }
    
//--simple--preserve
    /**
     * Perform a deep-copy EXCEPT Identifiable attributes of the given other instance into this instance
     * 
     * Note: to be overriden in child class to perform deep-copy of class fields
     * 
     * @param other other instance
     */
    @Override
    public void copyValues(final fr.jmmc.oiexplorer.core.model.OIBase other) {
        super.copyValues(other); // View
        
        final Plot plot = (Plot) other;

        // copy plotDefinition (reference):
        this.plotDefinition = plot.getPlotDefinition();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) { // View
            return false;
        }
        final Plot other = (Plot) obj;
        if (this.plotDefinition != other.getPlotDefinition() && (this.plotDefinition == null || !this.plotDefinition.equals(other.getPlotDefinition()))) {
            return false;
        }
        return true;
    }

    /**
     * toString() implementation using string builder
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        super.toString(sb, full); // View
        if (this.plotDefinition != null) {
            sb.append(", plotDefinition=");
            this.plotDefinition.toString(sb, full);
        }
        sb.append('}');
    }
//--simple--preserve

}
