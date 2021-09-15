package rs117.hd.ui;

import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;

import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.runelite.client.util.ColorUtil;
import rs117.hd.environments.Environment;
import rs117.hd.environments.EnvironmentManager;

import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;

@Slf4j
public class EnvironmentConfigPanel extends JPanel {

    private final ColorPickerManager colorPickerManager;
    private final EnvironmentManager environmentManager;

    private Environment selectedEnvironment;
    private JComboBox<String> environmentDropdown;

    // Variables for handling the Properties Pane
    private Environment.Properties modifiedProperties = new Environment.Properties();
    private final Environment.Properties defaultEnvProps = new Environment.Properties();
    private final HashMap<PropertyField, JComponent> fieldComponentMap = new HashMap<>();

    // Holds the Environment.Properties fields for use in the Panel
    private enum PropertyField {
        FOG_DEPTH("Fog Depth",
                int.class,
                Environment::getFogDepth,
                (prop, v) -> { prop.setFogDepth((int)v); }
        ),
        FOG_COLOR("Fog color",
                Color.class,
                env -> UiUtils.colorFromFloatArray(env.getFogColor()),
                (prop, v) -> { prop.setFogColor((Color)v); }
        ),

        AMBIENT_STRENGTH("Ambient Strength",
                float.class,
                Environment::getAmbientStrength,
                (prop, v) -> { prop.setAmbientStrength((float)v); }),
        AMBIENT_COLOR("Ambient Color",
                Color.class,
                env -> UiUtils.colorFromFloatArray(env.getAmbientColor()),
                (prop, v) -> { prop.setAmbientColor((Color)v); }),

        DIRECTIONAL_STRENGTH("Directional Strength",
                float.class,
                Environment::getDirectionalStrength,
                (prop, v) -> { prop.setDirectionalStrength((float)v); }),
        DIRECTIONAL_COLOR("Directional Color",
                Color.class,
                env -> UiUtils.colorFromFloatArray(env.getDirectionalColor()),
                (prop, v) -> { prop.setDirectionalColor((Color)v); }),

        UNDERGLOW_STRENGTH("Underglow Strength",
                float.class,
                Environment::getUnderglowStrength,
                (prop, v) -> { prop.setUnderglowStrength((float)v); }),
        UNDERGLOW_COLOR("Underglow Color",
                Color.class,
                env -> UiUtils.colorFromFloatArray(env.getUnderglowColor()),
                (prop, v) -> { prop.setUnderglowColor((Color)v); }),

        LIGHTNING_ENABLED("Lightning Enabled",
                boolean.class,
                Environment::isLightningEnabled,
                (prop, v) -> { prop.enableLightning((boolean)v); }),

        GROUND_FOG_START("Ground Fog Start",
                int.class,
                Environment::getGroundFogStart,
                (prop, v) -> { prop.setGroundFogStart((int)v); }),
        GROUND_FOG_END("Ground Fog End",
                int.class,
                Environment::getGroundFogEnd,
                (prop, v) -> { prop.setGroundFogEnd((int)v); }),
        GROUND_FOG_OPACITY("Ground Fog Opacity",
                float.class,
                Environment::getGroundFogOpacity,
                (prop, v) -> { prop.setGroundFogOpacity((float)v); });

        private final String label;
        private final Class<?> type;
        private final Function<Environment, Object> environmentGetter;
        private final BiConsumer<Environment.Properties, Object> propertySetter;

        PropertyField(
                String label,
                Class<?> type,
                Function<Environment, Object> environmentGetter,
                BiConsumer<Environment.Properties, Object> propertySetter
        ) {
            this.label = label;
            this.type = type;
            this.environmentGetter = environmentGetter;
            this.propertySetter = propertySetter;
        }

        public String getLabel() {
            return this.label;
        }

        public Class<?> getType() {
            return type;
        }

        public Object getEnvironmentValue(Environment environment) {
            return environmentGetter.apply(environment);
        }

        public void setPropertiesValue(Environment.Properties properties, Object value) {
            propertySetter.accept(properties, value);
        }
    }

    @Inject
    public EnvironmentConfigPanel(
            EnvironmentManager environmentManager,
            ColorPickerManager colorPickerManager
    ) {
        this.colorPickerManager = colorPickerManager;
        this.environmentManager = environmentManager;

        setLayout(new DynamicGridLayout(0, 1, 0, 5));
        setMinimumSize(new Dimension(PANEL_WIDTH, 0));
        add(createDropdown());
        add(generatePropertyFields());
        add(generateButtonBox());

        updatePanel(Environment.valueOf((String) environmentDropdown.getSelectedItem(), false));
    }

    // Update the entries in the panel with the values from the passed environment
    public void updatePanel(Environment environment) {
        if (environment == null) {
            return;
        }

        selectedEnvironment = environment;

        // Loop through all Property Fields tha we have, and update them using the passed Environment
        Arrays.stream(PropertyField.values()).forEach( field -> {
            setFieldEntry(field, field.getEnvironmentValue(environment));
            field.setPropertiesValue(modifiedProperties, field.getEnvironmentValue(environment));
        });
    }

    private JPanel createDropdown() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 0, 5));
        panel.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

        environmentDropdown = UiUtils.createComboBox(
                Arrays.stream(Environment.values())
                        .map(Environment::name)
                        .toArray(String[]::new),
                new FieldNameRenderer()
        );
        environmentDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) environmentDropdown.getSelectedItem();
                updatePanel(Environment.valueOf(selected, false));
            }
        });
        panel.add(environmentDropdown);

        JButton button = new JButton("Get Current Environment");
        button.addActionListener(e -> {
            log.debug("GETTING CURRENT ENVIRONMENT");
            Environment selected = environmentManager.getCurrentEnvironment();
            environmentDropdown.setSelectedItem(selected.name());
            environmentDropdown.setToolTipText(selected.name());
            log.debug("GOT CURRENT ENVIRONMENT");
        });
        panel.add(button);

        return panel;
    }

    // Generates an entry into the panel for each Environment Property we want to edit
    private JPanel generatePropertyFields() {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));

        Arrays.stream(PropertyField.values()).forEach( field -> {
            JPanel fieldEntry = createFieldEntry(field, false);
            if (fieldEntry != null) {
                fieldPanel.add(fieldEntry);
            }
        });

        return fieldPanel;
    }

    private JPanel createFieldEntry(PropertyField field, boolean devOnly) {
        JPanel entry = new JPanel();
        entry.setLayout(new BorderLayout());

        // Create the label
        JLabel label = new JLabel(field.label);
        label.setForeground(Color.WHITE);
        if (devOnly) {
            label.setForeground(Color.ORANGE);
        }
        entry.add(label, BorderLayout.WEST);

        // Create the control
        JComponent fieldControl = null;
        Class<?> type = field.type;
        if (type == Color.class) {
            // Create color picker control
            fieldControl = UiUtils.createColorPicker(colorPickerManager, entry, field.label, Color.WHITE, color -> {
                field.setPropertiesValue(modifiedProperties, color);
            });
        } else if (type == int.class || type == Integer.class) {
            JTextField textField = new JTextField();
            textField.setForeground(Color.WHITE);
            textField.setPreferredSize(new Dimension(100, 25));
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    field.setPropertiesValue(modifiedProperties, Integer.parseInt(textField.getText()));
                }
            });
            fieldControl = textField;
        } else if (type == float.class || type == Float.class) {
            JTextField textField = new JTextField();
            textField.setForeground(Color.WHITE);
            textField.setPreferredSize(new Dimension(100, 25));
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    field.setPropertiesValue(modifiedProperties, Float.parseFloat(textField.getText()));
                }
            });
            fieldControl = textField;
        } else if (type == boolean.class || type == Boolean.class) {
            JTextField textField = new JTextField();
            textField.setForeground(Color.RED);
            textField.setPreferredSize(new Dimension(100, 25));
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    field.setPropertiesValue(modifiedProperties, Integer.parseInt(textField.getText()));
                }
            });
            fieldControl = textField;
        }

        if (fieldControl != null) {
            fieldComponentMap.put(field, fieldControl);
            entry.add(fieldControl, BorderLayout.EAST);
            return entry;
        } else {
            // Error, should not be null
            log.error("Could not create a field entry for property : {}", field.getLabel());
            return null;
        }
    }

    private Object getFieldEntry(PropertyField field) {
        JComponent component = fieldComponentMap.get(field);

        if (component instanceof ColorJButton) {
            return ((ColorJButton) component).getColor();
        } else if (component instanceof JTextField) {
            return ((JTextField) component).getText();
        } else {
            return null;
        }
    }

    private void setFieldEntry(PropertyField field, Object value) {
        JComponent component = fieldComponentMap.get(field);

        if (component instanceof ColorJButton) {
            ((ColorJButton) component).setColor((Color) value);
            ((ColorJButton) component).setText("#" + (ColorUtil.colorToHexCode((Color) value)).toUpperCase());
        } else if (component instanceof JTextField) {
            ((JTextField) component).setText(value.toString());
        }
    }

    private JPanel generateButtonBox() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 5));
        // Button to reset Environment to default settings
        JButton resetDefaults = new JButton("Reset to Environment Defaults");
        resetDefaults.setToolTipText("<html>This is used to reset the Environment Properties panel " +
                "<br>to the default values of the currently selected Environment</html>");
        resetDefaults.setPreferredSize(new Dimension(PANEL_WIDTH, 25));
        resetDefaults.addActionListener(e -> {
            Environment selectedDefault = Environment.valueOf((String) environmentDropdown.getSelectedItem(), true);
            updatePanel(selectedDefault);
            if (selectedDefault != null) {
                modifiedProperties = new Environment.Properties(selectedDefault);
            } else {
                modifiedProperties = new Environment.Properties();
            }
        });
        buttonPanel.add(resetDefaults);

        // Button to Apply settings to currently selected environment
        JButton applySettingsSelected = new JButton("Apply to Current Environment");
        applySettingsSelected.setToolTipText("<html>This applies the current settings to the Environment you are currently in." +
                "<br>Also creates an override so the override persists for this session.</html>");
        applySettingsSelected.setPreferredSize(new Dimension(PANEL_WIDTH, 25));
        applySettingsSelected.addActionListener(e -> {
            applyPropertiesOverride(selectedEnvironment, modifiedProperties);
        });
        buttonPanel.add(applySettingsSelected);

        // Button to Apply settings to environment you are currently in
        JButton applySettingsCurrent = new JButton("Apply to Selected Environment");
        applySettingsCurrent.setToolTipText("<html>This applies the current settings to the Environment you have currently selected in the dropdown. " +
                "<br>Also creates an override so the override persists for this session.</html>");
        applySettingsCurrent.setPreferredSize(new Dimension(PANEL_WIDTH, 25));
        applySettingsCurrent.addActionListener(e -> {
            applyPropertiesOverride(environmentManager.getCurrentEnvironment(), modifiedProperties);
        });
        buttonPanel.add(applySettingsCurrent);

        return buttonPanel;
    }

    // Apply the passed in properties to the passed Environment, and create an override for it
    private void applyPropertiesOverride(Environment environment, Environment.Properties properties) {
        Environment newOverride = new Environment(environment.getArea(), properties);
        Environment.addOverride(newOverride);
        environmentManager.loadSceneEnvironments();
        environmentManager.update();
    }

    // Custom ListCellRenderer for a list of field names
    private static class FieldNameRenderer implements ListCellRenderer<String> {
        protected ComboBoxListRenderer<String> defaultRenderer = new ComboBoxListRenderer<>();

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel component = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, value);

            component.setText(text);

            return component;
        }
    }

}
