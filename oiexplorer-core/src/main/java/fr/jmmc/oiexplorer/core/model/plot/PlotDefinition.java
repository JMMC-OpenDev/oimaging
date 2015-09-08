
package fr.jmmc.oiexplorer.core.model.plot;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import fr.jmmc.oiexplorer.core.model.oi.Identifiable;


/**
 * 
 *                 This type describes a plot definition.
 *             
 * 
 * <p>Java class for PlotDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PlotDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.jmmc.fr/oiexplorer-base/0.1}Identifiable">
 *       &lt;sequence>
 *         &lt;element name="skipFlaggedData" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="drawLine" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="colorMapping" type="{http://www.jmmc.fr/oiexplorer-core-plot-definition/0.1}ColorMapping"/>
 *         &lt;element name="xAxis" type="{http://www.jmmc.fr/oiexplorer-core-plot-definition/0.1}Axis"/>
 *         &lt;element name="yAxes" type="{http://www.jmmc.fr/oiexplorer-core-plot-definition/0.1}Axis" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlotDefinition", propOrder = {
    "skipFlaggedData",
    "drawLine",
    "colorMapping",
    "xAxis",
    "yAxes"
})
public class PlotDefinition
    extends Identifiable
{

    protected boolean skipFlaggedData;
    protected boolean drawLine;
    @XmlElement(required = true)
    protected ColorMapping colorMapping;
    @XmlElement(required = true)
    protected Axis xAxis;
    @XmlElement(required = true)
    protected List<Axis> yAxes;

    /**
     * Gets the value of the skipFlaggedData property.
     * 
     */
    public boolean isSkipFlaggedData() {
        return skipFlaggedData;
    }

    /**
     * Sets the value of the skipFlaggedData property.
     * 
     */
    public void setSkipFlaggedData(boolean value) {
        this.skipFlaggedData = value;
    }

    /**
     * Gets the value of the drawLine property.
     * 
     */
    public boolean isDrawLine() {
        return drawLine;
    }

    /**
     * Sets the value of the drawLine property.
     * 
     */
    public void setDrawLine(boolean value) {
        this.drawLine = value;
    }

    /**
     * Gets the value of the colorMapping property.
     * 
     * @return
     *     possible object is
     *     {@link ColorMapping }
     *     
     */
    public ColorMapping getColorMapping() {
        return colorMapping;
    }

    /**
     * Sets the value of the colorMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link ColorMapping }
     *     
     */
    public void setColorMapping(ColorMapping value) {
        this.colorMapping = value;
    }

    /**
     * Gets the value of the xAxis property.
     * 
     * @return
     *     possible object is
     *     {@link Axis }
     *     
     */
    public Axis getXAxis() {
        return xAxis;
    }

    /**
     * Sets the value of the xAxis property.
     * 
     * @param value
     *     allowed object is
     *     {@link Axis }
     *     
     */
    public void setXAxis(Axis value) {
        this.xAxis = value;
    }

    /**
     * Gets the value of the yAxes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the yAxes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getYAxes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Axis }
     * 
     * 
     */
    public List<Axis> getYAxes() {
        if (yAxes == null) {
            yAxes = new ArrayList<Axis>();
        }
        return this.yAxes;
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
        final PlotDefinition plotDef = (PlotDefinition) other;

        // copy skipFlaggedData, drawLine, colorMapping:
        this.skipFlaggedData = plotDef.isSkipFlaggedData();
        this.drawLine = plotDef.isDrawLine();
        this.colorMapping = plotDef.getColorMapping();

        // deep copy xAxis, yAxes:
        this.xAxis = (plotDef.getXAxis() != null) ? (Axis) plotDef.getXAxis().clone() : null;
        this.yAxes = fr.jmmc.jmcs.util.ObjectUtils.deepCopyList(plotDef.getYAxes());
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) { // Identifiable
            return false;
        }
        final PlotDefinition other = (PlotDefinition) obj;
        if (this.skipFlaggedData != other.skipFlaggedData) {
            return false;
        }
        if (this.drawLine != other.drawLine) {
            return false;
        }
        if (this.colorMapping != other.colorMapping) {
            return false;
        }
        if (this.xAxis != other.xAxis && (this.xAxis == null || !this.xAxis.equals(other.xAxis))) {
            return false;
        }
        if (this.yAxes != other.yAxes && (this.yAxes == null || !this.yAxes.equals(other.yAxes))) {
            return false;
        }
        return true;
    }

    /**
     * toString() implementation using string builder
     * @param sb string builder to append to
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        super.toString(sb, full); // Identifiable
        if (full) {
            sb.append(", skipFlaggedData='").append(this.skipFlaggedData).append('\'');
            sb.append(", drawLine='").append(this.drawLine).append('\'');
            sb.append(", colorMapping='").append(this.colorMapping).append('\'');

            sb.append(", xAxis=");
            if (this.xAxis != null) {
                this.xAxis.toString(sb, full);
            }
            sb.append(", yAxes=");
            fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.yAxes);
        }
        sb.append('}');
    }
//--simple--preserve

}
