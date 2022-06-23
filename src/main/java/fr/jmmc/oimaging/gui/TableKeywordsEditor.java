/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import static fr.jmmc.jmcs.gui.util.ResourceImage.DOWN_ARROW;
import static fr.jmmc.jmcs.gui.util.ResourceImage.UP_ARROW;
import fr.jmmc.jmcs.util.SpecialChars;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oimaging.services.software.MiraInputParam;
import static fr.jmmc.oimaging.services.software.MiraInputParam.KEYWORD_SMEAR_FC;
import static fr.jmmc.oimaging.services.software.MiraInputParam.KEYWORD_SMEAR_FN;
import fr.jmmc.oimaging.services.software.SparcoInputParam;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_DEX;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_DEY;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_FLU;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_IDX;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_MOD;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_PAR;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_SNMODS;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_SPEC;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_SWAVE0;
import static fr.jmmc.oimaging.services.software.SparcoInputParam.KEYWORD_TEM;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.image.FitsUnit;
import fr.jmmc.oitools.meta.KeywordMeta;
import static fr.jmmc.oitools.meta.Types.TYPE_CHAR;
import static fr.jmmc.oitools.meta.Types.TYPE_DBL;
import static fr.jmmc.oitools.meta.Types.TYPE_INT;
import static fr.jmmc.oitools.meta.Types.TYPE_LOGICAL;
import fr.jmmc.oitools.meta.Units;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class displays and edit table params using swing widgets.
 */
public final class TableKeywordsEditor extends javax.swing.JPanel
        implements ActionListener, PropertyChangeListener, ChangeListener {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(TableKeywordsEditor.class.getName());

    private static final long serialVersionUID = 1L;

    private final static Insets DEFAULT_INSETS = new Insets(2, 2, 2, 2);
    private final static Insets INSET_20_2_2_2 = new Insets(20, 2, 2, 2);
    private final static Insets INSET_40_2_2_2 = new Insets(40, 2, 2, 2);
    private final static Insets INSET_2_2_20_2 = new Insets(2, 2, 20, 2);

    /**
     * Sparco "dynamic" keyword which have a number suffix. Please don't move the order as it is also used as display
     * order.
     */
    private final static List<String> sparcoDynamicKeywordList = Arrays.asList(
            KEYWORD_MOD, KEYWORD_FLU,
            KEYWORD_SPEC, KEYWORD_IDX, KEYWORD_TEM,
            KEYWORD_PAR, KEYWORD_DEX, KEYWORD_DEY);

    // members
    private SoftwareSettingsPanel notifiedParent = null;
    private FitsTable fitsTable = null;
    /** editor conversions */
    private final HashMap<String, FitsUnit> unitKeywords = new HashMap<String, FitsUnit>();
    private final HashMap<String, FitsUnit> unitFields = new HashMap<String, FitsUnit>();

    /**
     * state of the button show/hide for bandwith smearing (MIRA & SPARCO only)
     */
    private final ToggleButtonModel miraSmearingModel = new ToggleButtonModel();

    /**
     * state of the button show/hide for models (SPARCO only)
     */
    private final List<ToggleButtonModel> sparcoToggleButtonModels = new ArrayList<>();

    /** Creates new form TableEditor */
    public TableKeywordsEditor() {
        initComponents();
    }

    public SoftwareSettingsPanel getNotifiedParent() {
        return notifiedParent;
    }

    public void setNotifiedParent(final SoftwareSettingsPanel notifiedParent) {
        this.notifiedParent = notifiedParent;
    }

    void setModel(final FitsTable fitsTable, final Service currentService) {
        setModel(fitsTable, null, currentService);
    }

    void setModel(final FitsTable fitsTable, final Set<String> keywordNamesParams, final Service currentService) {
        this.fitsTable = fitsTable;

        // copy the set as it will be modified by the display functions
        final Set<String> keywordNames = new LinkedHashSet<>();
        if (keywordNamesParams != null) {
            keywordNames.addAll(keywordNamesParams);
        }

        removeAll();
        updateUI(); // to resolve paint glitch on macos

        // reset converters:
        this.unitKeywords.clear();
        this.unitFields.clear();

        if (fitsTable == null) {
            return;
        }

        setLayout(new GridBagLayout());
        if (currentService == null) {
            defaultDisplay(0, keywordNames);
        }
        else {
            switch (currentService.getName()) {
                case ServiceList.SERVICE_MIRA:
                case ServiceList.SERVICE_MIRA + " (local)":
                    miraDisplay(0, keywordNames);
                    break;
                case ServiceList.SERVICE_SPARCO:
                case ServiceList.SERVICE_SPARCO + " (local)":
                    sparcoDisplay(0, keywordNames);
                    break;
                default:
                    defaultDisplay(0, keywordNames);
            }
        }
    }

    /**
     * Displays keywords given in @keywordNames
     *
     * @param gridy starting y coord on the layout
     * @param keywordNames
     * @return resulting y coord on the layout
     */
    private int defaultDisplay(int gridy, final Set<String> keywordNames) {
        for (final String name : keywordNames) {
            addFormKeyword(name, getLabel(name), gridy, DEFAULT_INSETS);
            gridy++;
        }
        return gridy;
    }

    /**
     * Displays Mira keywords and any keywords given in @keywordNames
     *
     * @param gridy starting y coord on the layout
     * @param keywordNames will be modified !
     * @return resulting y coord on the layout
     */
    private int miraDisplay(int gridy, final Set<String> keywordNames) {

        // remove mira keywords
        keywordNames.remove(KEYWORD_SMEAR_FN);
        keywordNames.remove(KEYWORD_SMEAR_FC);

        // calling default display for non-mira keywords
        gridy = defaultDisplay(gridy, keywordNames);

        // display mira keywords

        ShowHideComponentsButton button = new ShowHideComponentsButton(
                "Bandwith smearing", "Bandwith smearing",
                miraSmearingModel, gridy, INSET_20_2_2_2);
        gridy++;

        button.addComponents(addFormKeyword(KEYWORD_SMEAR_FN, getLabel(KEYWORD_SMEAR_FN), gridy, DEFAULT_INSETS));
        gridy++;

        button.addComponents(addFormKeyword(KEYWORD_SMEAR_FC, getLabel(KEYWORD_SMEAR_FC), gridy, DEFAULT_INSETS));
        gridy++;

        return gridy;
    }

    /**
     * Displays sparco keywords, mira keywords, and any keywords given in @keywordNames
     *
     * @param gridy starting y coord on the layout
     * @param keywordNames will be modified !
     * @return resulting y coord on the layout
     */
    private int sparcoDisplay(int gridy, final Set<String> keywordNames) {

        // remove sparco static keywords
        keywordNames.remove(KEYWORD_SWAVE0);
        keywordNames.remove(KEYWORD_SNMODS);

        // get the number of models
        final int snmods = this.fitsTable.getKeywordInt(KEYWORD_SNMODS);

        // remove sparco dynamic keywords
        for (int i = 0; i <= snmods; i++) {
            for (String sparcoDynamicKeyword : sparcoDynamicKeywordList) {
                keywordNames.remove(sparcoDynamicKeyword + i);
            }
        }

        // calling mira display (since sparco has also mira keywords)
        gridy = miraDisplay(gridy, keywordNames);

        // display sparco keywords
        addFormKeyword(KEYWORD_SWAVE0, getLabel(KEYWORD_SWAVE0), gridy, INSET_20_2_2_2);
        gridy++;
        addFormKeyword(KEYWORD_SNMODS, getLabel(KEYWORD_SNMODS), gridy, INSET_2_2_20_2);
        gridy++;

        // we need a ToggleButtonModel for each model. But not for the first model (which is always shown)
        if (snmods > sparcoToggleButtonModels.size()) {
            for (int i = 0, s = snmods - sparcoToggleButtonModels.size(); i < s; i++) {
                sparcoToggleButtonModels.add(new ToggleButtonModel());
            }
        }

        // for each model
        for (int i = 0; i <= snmods; i++) {

            // creating show hide button for the model and its fields
            ShowHideComponentsButton button = null;
            if (i > 0) {
                ToggleButtonModel model = sparcoToggleButtonModels.get(i - 1); // - 1 because first model has no button
                button = new ShowHideComponentsButton("Model n°" + i, "Model n°" + i, model, gridy, INSET_20_2_2_2);
                gridy++;
            }

            // for each keyword of this model
            for (String sparcoDynamicKeyword : sparcoDynamicKeywordList) {
                if (this.fitsTable.hasKeywordMeta(sparcoDynamicKeyword + i)) {

                    // display components (label + field)
                    List<JComponent> comps = addFormKeyword(
                            sparcoDynamicKeyword + i, getLabel(sparcoDynamicKeyword + i), gridy, DEFAULT_INSETS);
                    gridy++;

                    // add the components to the showhide group (unless we are in the first model)
                    if (button != null) {
                        button.addComponents(comps);
                    }
                }
            }
        }

        return gridy;
    }

    /**
     * Creates a button, associated to a ToggleButtonModel. You gives components to be shown/hidden when the button is
     * clicked.
     */
    private class ShowHideComponentsButton {

        private final String labelSelected;
        private final String labelUnselected;
        private final JToggleButton button;
        private final List<JComponent> components;
        /**
         * add a show/hide button to show/hide other components
         *
         * @param labelSelected label worn by the button when selected
         * @param labelUnselected label worn by the button when unselected
         * @param buttonModel ToggleButtonModel of the button.
         * @param gridy the y coord of the button in the layout.
         * @param insets the insets of the button in the layout.
         */
        public ShowHideComponentsButton(
                final String labelSelected, final String labelUnselected,
                final ToggleButtonModel buttonModel, final int gridy, final Insets insets) {


            this.labelSelected = labelSelected;
            this.labelUnselected = labelUnselected;
            this.button = new JToggleButton();
            this.button.setModel(buttonModel);
            this.components = new ArrayList<>();

            // button display
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = gridy;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridwidth = 5;
            gridBagConstraints.insets = insets;
            gridBagConstraints.anchor = GridBagConstraints.CENTER;
            add(button, gridBagConstraints);

            // event handler: show/hide components, change button label
            button.addItemListener((ItemEvent e) -> this.showHide());

            // init label
            this.showHide();
        }

        public void addComponents(final List<JComponent> components) {
            components.forEach(jComponent -> jComponent.setVisible(this.button.isSelected()));
            this.components.addAll(components);
        }

        private void showHide() {
            this.components.forEach(jComponent -> jComponent.setVisible(this.button.isSelected()));
            button.setText(button.isSelected() ? labelSelected : labelUnselected);
            button.setIcon(button.isSelected() ? DOWN_ARROW.icon() : UP_ARROW.icon());
        }
    }

    /**
     * Add to the panel a field
     * @param name name of the field
     * @param gridy the next gridbaglayout y coord available
     * @param insets swing insets
     * @return created components
     */
    private List<JComponent> addFormKeyword(final String name, String label, final int gridy, Insets insets) {

        final KeywordMeta meta = fitsTable.getKeywordsDesc(name);
        final Object value = fitsTable.getKeywordValue(name);

        final JComponent component;
        boolean supportedKeyword = true;

        // special cases
        if (SparcoInputParam.KEYWORD_SNMODS.equals(name)) {
            int intValue = (value instanceof Integer) ? (Integer) value : 1;
            component = new JSpinner(new SpinnerNumberModel(intValue, 1, Integer.MAX_VALUE, 1));
        } // normal cases
        else {
            switch (meta.getDataType()) {
                case TYPE_CHAR:
                    if (meta.getStringAcceptedValues() == null) {
                        component = new JTextField((value == null) ? "" : value.toString());
                    } else {
                        JComboBox comboBox = new JComboBox(new GenericListModel(Arrays.asList(meta.getStringAcceptedValues()), true));
                        comboBox.setPrototypeDisplayValue("XXXX");
                        if (value != null) {
                            comboBox.setSelectedItem(value);
                        }
                        comboBox.setRenderer(new LabelListCellRenderer());
                        component = comboBox;
                    }
                    break;
                case TYPE_DBL:
                    component = createFormattedTextField(SoftwareSettingsPanel.getDecimalFormatterFactory(),
                            convertValueToField(name, meta.getUnits(), value));
                    break;
                case TYPE_INT:
                    component = createFormattedTextField(SoftwareSettingsPanel.getIntegerFormatterFactory(), value);
                    break;
                case TYPE_LOGICAL:
                    final JCheckBox checkbox = new JCheckBox();
                    checkbox.setSelected(Boolean.TRUE.equals(value));
                    component = checkbox;
                    break;
                default:
                    component = new JTextField(meta.getDataType() + " UNSUPPORTED");
                    supportedKeyword = false;
            }
        }

        // show description in tooltips:
        String description = meta.getDescription();

        FitsUnit unit = this.unitFields.get(name);
        if (unit != null) {
            description = "<html>" + description + "<br/><b>Editor unit is '" + unit.getRepresentation() + "'</b></html>";
        } else {
            unit = this.unitKeywords.get(name);
        }

        // define label and optionally the unit:
        if (unit != null) {
            label += " [" + ((unit == FitsUnit.WAVELENGTH_MICRO_METER)
                    ? SpecialChars.UNIT_MICRO_METER : unit.getStandardRepresentation()) + ']';
        }
        final JLabel jLabel = new JLabel(label);
        jLabel.setToolTipText(description);
        component.setToolTipText(description);

        if (supportedKeyword) {
            // store name to retieve back on edit
            component.setName(name);
            component.addPropertyChangeListener("value", this);

            if (component instanceof JTextField) {
                ((JTextField) component).addActionListener(this);
            } else if (component instanceof JComboBox) {
                ((JComboBox) component).addActionListener(this);
            } else if (component instanceof JCheckBox) {
                ((JCheckBox) component).addActionListener(this);
            } else if (component instanceof JSpinner) {
                ((JSpinner) component).addChangeListener(this);
            }
        } else {
            ((JTextField) component).setEditable(false);
        }

        GridBagConstraints gridBagConstraints;

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = insets;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        add(jLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = gridy;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = insets;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;

        add(component, gridBagConstraints);

        return Arrays.asList(jLabel, component);
    }

    /**
     * return a label associated to a keyword name.
     * The labels are specific to this form: they are chosen to be short, and clear in this context.
     *
     * @param keywordName the keyword name for which we want a label.
     * @return the label. return the keywordName if no label found.
     */
    private static String getLabel(final String keywordName) {
        switch (keywordName) {
            // === mira parameters ===
            case MiraInputParam.KEYWORD_SMEAR_FN:
                return "Function";
            case MiraInputParam.KEYWORD_SMEAR_FC:
                return "Factor";
            // === sparco parameters ===
            case SparcoInputParam.KEYWORD_SWAVE0:
                return "Reference wavelength";
            case SparcoInputParam.KEYWORD_SNMODS:
                return "Number of models";
            // === sparco list items ===
            case SparcoInputParam.KEYWORD_SPEC_POW:
                return "power";
            case SparcoInputParam.KEYWORD_SPEC_BB:
                return "black body";
            case SparcoInputParam.KEYWORD_MODEL_STAR:
                return "star";
            case SparcoInputParam.KEYWORD_MODEL_UD:
                return "uniform disc";
            case SparcoInputParam.KEYWORD_MODEL_BG:
                return "background";
            default:
                break;
        }
        // in case of Sparco Model keyword, we must first remove the integer at the end of the keyword :
        {
            int startInteger = keywordName.length();
            while (startInteger > 0 && Character.isDigit(keywordName.charAt(startInteger - 1))) {
                startInteger--;
            }
            if (startInteger < keywordName.length()) {
                switch (keywordName.substring(0, startInteger)) {
                    case SparcoInputParam.KEYWORD_SPEC:
                        if ("0".equals(keywordName.substring(startInteger))) {
                            return "Image spectrum"; // special rule for keyword name SPEC0
                        } else {
                            return "Model spectrum";
                        }
                    case SparcoInputParam.KEYWORD_IDX:
                        return "Spectral index";
                    case SparcoInputParam.KEYWORD_TEM:
                        return "Temperature";
                    case SparcoInputParam.KEYWORD_MOD:
                        return "Model n°" + keywordName.substring(startInteger);
                    case SparcoInputParam.KEYWORD_PAR:
                        return "UD diameter";
                    case SparcoInputParam.KEYWORD_FLU:
                        return "Flux ratio";
                    case SparcoInputParam.KEYWORD_DEX:
                        return "RA shift";
                    case SparcoInputParam.KEYWORD_DEY:
                        return "DEC shift";
                }
            }
        }
        // else, return the keywordName as label
        return keywordName;
    }

    /**
     * Render labels instead of raw values.
     */
    private static class LabelListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList jList, Object value, int index, boolean selected, boolean focused
        ) {
            super.getListCellRendererComponent(jList, value, index, selected, focused);
            if (value instanceof String) {
                this.setText(getLabel((String) value));
            }
            return this;
        }
    }

    private static JFormattedTextField createFormattedTextField(final JFormattedTextField.AbstractFormatterFactory formatterFactory, final Object value) {
        final JFormattedTextField jFormattedTextField = new JFormattedTextField(value);
        jFormattedTextField.setFormatterFactory(formatterFactory);
        return jFormattedTextField;
    }

    /** 
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createTitledBorder("Specific params"));
        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void actionPerformed(final ActionEvent ae) {
        final JComponent component = (JComponent) ae.getSource();
        if (component != null) {
            final String name = component.getName();
            String value = null;
            if (component instanceof JTextField) {
                value = ((JTextField) component).getText();
            } else if (component instanceof JCheckBox) {
                value = Boolean.toString(((JCheckBox) component).isSelected());
            } else if (component instanceof JComboBox) {
                value = (String) ((JComboBox) component).getSelectedItem();
            }
            update(name, value);
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent pce) {
        final JComponent component = (JComponent) pce.getSource();
        if (component != null) {
            final String name = component.getName();
            if (pce.getNewValue() != null) {
                update(name, pce.getNewValue().toString());
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        final JComponent component = (JComponent) ce.getSource();
        if (component != null) {
            final String name = component.getName();
            if (component instanceof JSpinner) {
                JSpinner jSpinner = (JSpinner) component;
                update(name, jSpinner.getValue().toString());
            }
        }
    }

    private void update(final String name, final String value) {
        // Store content as a string even for every types
        if (name != null && value != null) {
            // handle unit conversion:
            final FitsUnit unitField = this.unitFields.get(name);

            if (unitField != null) {
                fitsTable.setKeywordValue(name,
                        convertFieldToValue(name, unitField, value));
            } else {
                fitsTable.updateKeyword(name, value);
            }
            notifiedParent.updateModel(true);
        }
    }

    private Object convertValueToField(final String name, final Units unit, final Object value) {
        Object output = value;

        if (unit != Units.NO_UNIT) {
            try {
                // parse unit:
                final FitsUnit unitKeyword = FitsUnit.parseUnit(unit.getStandardRepresentation());

                if (unitKeyword != FitsUnit.NO_UNIT) {
                    this.unitKeywords.put(name, unitKeyword);

                    final FitsUnit unitField;
                    switch (unitKeyword) {
                        case WAVELENGTH_METER:
                            // implictely suppose wavelength argument:
                            unitField = FitsUnit.WAVELENGTH_MICRO_METER;
                            break;
                        case ANGLE_DEG:
                            unitField = FitsUnit.ANGLE_MILLI_ARCSEC;
                            break;
                        default:
                            // no conversion
                            unitField = null;
                    }

                    if (unitField != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Keyword[{}] unitKeyword: {}, unitField: {}", name, unitKeyword, unitField);
                        }
                        this.unitFields.put(name, unitField);

                        // perform conversion:
                        if (value instanceof Double) {
                            final double val = (Double) value;
                            final double converted = unitKeyword.convert(val, unitField);

                            if (logger.isDebugEnabled()) {
                                logger.debug("Keyword[{}] val: {}, converted: {}", name, val, converted);
                            }
                            output = Double.valueOf(converted);
                        }
                    }
                }
            } catch (IllegalArgumentException iae) {
                logger.info("convertValueToField: failure:", iae);
            }
        }
        return output;
    }

    private Double convertFieldToValue(final String name, final FitsUnit unitField, final String value) {
        Double output = null;

        final FitsUnit unitKeyword = this.unitKeywords.get(name);

        if (unitKeyword != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Keyword[{}] unitKeyword: {}, unitField: {}", name, unitKeyword, unitField);
            }

            if (!StringUtils.isEmpty(value)) {
                // perform conversion:
                final double val = Double.valueOf(value);
                final double converted = unitField.convert(val, unitKeyword);

                if (logger.isDebugEnabled()) {
                    logger.debug("Keyword[{}] val: {}, converted: {}", name, val, converted);
                }
                output = Double.valueOf(converted);
            }
        }
        return output;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
