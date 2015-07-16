
package fr.jmmc.oiexplorer.core.model.oi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import fr.jmmc.oiexplorer.core.model.OIBase;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;


/**
 * 
 *                 This type describes a collection of oidata ressources.
 *             
 * 
 * <p>Java class for OIDataCollection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OIDataCollection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="schemaVersion" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="file" type="{http://www.jmmc.fr/oiexplorer-data-collection/0.1}OIDataFile" maxOccurs="unbounded"/>
 *         &lt;element name="subsetDefinition" type="{http://www.jmmc.fr/oiexplorer-data-collection/0.1}SubsetDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="plotDefinition" type="{http://www.jmmc.fr/oiexplorer-core-plot-definition/0.1}PlotDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="plot" type="{http://www.jmmc.fr/oiexplorer-data-collection/0.1}Plot" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OIDataCollection", propOrder = {
    "schemaVersion",
    "files",
    "subsetDefinitions",
    "plotDefinitions",
    "plots"
})
@XmlRootElement(name = "oiDataCollection")
public class OiDataCollection
    extends OIBase
{

    protected float schemaVersion;
    @XmlElement(name = "file", required = true)
    protected List<OIDataFile> files;
    @XmlElement(name = "subsetDefinition")
    protected List<SubsetDefinition> subsetDefinitions;
    @XmlElement(name = "plotDefinition")
    protected List<PlotDefinition> plotDefinitions;
    @XmlElement(name = "plot")
    protected List<Plot> plots;

    /**
     * Gets the value of the schemaVersion property.
     * 
     */
    public float getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Sets the value of the schemaVersion property.
     * 
     */
    public void setSchemaVersion(float value) {
        this.schemaVersion = value;
    }

    /**
     * Gets the value of the files property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the files property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFiles().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OIDataFile }
     * 
     * 
     */
    public List<OIDataFile> getFiles() {
        if (files == null) {
            files = new ArrayList<OIDataFile>();
        }
        return this.files;
    }

    /**
     * Gets the value of the subsetDefinitions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subsetDefinitions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubsetDefinitions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubsetDefinition }
     * 
     * 
     */
    public List<SubsetDefinition> getSubsetDefinitions() {
        if (subsetDefinitions == null) {
            subsetDefinitions = new ArrayList<SubsetDefinition>();
        }
        return this.subsetDefinitions;
    }

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

    /**
     * Gets the value of the plots property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the plots property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlots().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Plot }
     * 
     * 
     */
    public List<Plot> getPlots() {
        if (plots == null) {
            plots = new ArrayList<Plot>();
        }
        return this.plots;
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
            sb.append("{files=");
            fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.files);

            sb.append(", subsetDefinitions=");
            fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.subsetDefinitions);

            sb.append(", plotDefinitions=");
            fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.plotDefinitions);

            sb.append(", plots=");
            fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.plots);

            sb.append('}');
        }
    }
//--simple--preserve

}
