
package fr.jmmc.oiexplorer.core.model.oi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 This type describes a subset definition.
 *             
 * 
 * <p>Java class for SubsetDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubsetDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.jmmc.fr/oiexplorer-base/0.1}Identifiable">
 *       &lt;sequence>
 *         &lt;element name="target" type="{http://www.jmmc.fr/oiexplorer-data-collection/0.1}TargetUID"/>
 *         &lt;element name="table" type="{http://www.jmmc.fr/oiexplorer-data-collection/0.1}TableUID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="filter" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubsetDefinition", propOrder = {
    "target",
    "tables",
    "filters"
})
public class SubsetDefinition
    extends Identifiable
{

    @XmlElement(required = true)
    protected TargetUID target;
    @XmlElement(name = "table")
    protected List<TableUID> tables;
    @XmlElement(name = "filter")
    protected List<String> filters;

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link TargetUID }
     *     
     */
    public TargetUID getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetUID }
     *     
     */
    public void setTarget(TargetUID value) {
        this.target = value;
    }

    /**
     * Gets the value of the tables property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tables property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTables().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TableUID }
     * 
     * 
     */
    public List<TableUID> getTables() {
        if (tables == null) {
            tables = new ArrayList<TableUID>();
        }
        return this.tables;
    }

    /**
     * Gets the value of the filters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the filters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFilters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFilters() {
        if (filters == null) {
            filters = new ArrayList<String>();
        }
        return this.filters;
    }
    
//--simple--preserve
    /**
     * Perform a deep-copy of the given other instance into this instance
     * 
     * Note: to be overriden in child class to perform deep-copy of class fields
     * @see OIBase#clone() 
     * 
     * @param other other instance
     */
    @Override
    public void copy(final fr.jmmc.oiexplorer.core.model.OIBase other) {
        super.copy(other); // Identifiable
        final SubsetDefinition subset = (SubsetDefinition) other;

        // deep copy target, tables:
        this.target = (subset.getTarget() != null) ? (TargetUID) subset.getTarget().clone() : null;
        this.tables = fr.jmmc.jmcs.util.ObjectUtils.deepCopyList(subset.getTables());

        // copy filters until filter are defined (TODO):
        this.filters = fr.jmmc.jmcs.util.ObjectUtils.copyList(subset.getFilters());
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) { // Identifiable
            return false;
        }
        final SubsetDefinition other = (SubsetDefinition) obj;
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        if (this.tables != other.tables && (this.tables == null || !this.tables.equals(other.tables))) {
            return false;
        }
        if (this.filters != other.filters && (this.filters == null || !this.filters.equals(other.filters))) {
            return false;
        }
        return true;
    }
    /** subset oiFitsFile structure (read only) */
    @javax.xml.bind.annotation.XmlTransient
    private fr.jmmc.oitools.model.OIFitsFile oiFitsSubset = null;

    /**
     * Return the subset oiFitsFile structure
     * @return subset oiFitsFile structure
     */
    public final fr.jmmc.oitools.model.OIFitsFile getOIFitsSubset() {
        return this.oiFitsSubset;
    }

    /**
     * Return the subset oiFitsFile structure
     * @param oiFitsSubset subset oiFitsFile structure
     */
    public final void setOIFitsSubset(final fr.jmmc.oitools.model.OIFitsFile oiFitsSubset) {
        this.oiFitsSubset = oiFitsSubset;
    }

    /**
     * toString() implementation using string builder
     * @param sb string builder to append to
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        super.toString(sb, full); // Identifiable

        if (full) {
            sb.append(", target='").append(this.target).append('\'');

            sb.append(", tables=");
            fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.tables);

            sb.append(", filters=");

            // TODO: fix filter impl:
            sb.append(this.filters);
            // fr.jmmc.jmcs.util.ObjectUtils.toString(sb, full, this.filters);
        }
        sb.append('}');
    }
//--simple--preserve

}
