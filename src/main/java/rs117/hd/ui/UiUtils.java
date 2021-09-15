package rs117.hd.ui;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ColorUtil;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import static net.runelite.client.ui.PluginPanel.PANEL_WIDTH;

public class UiUtils {

    // Creates and returns a ColorJButton object
    public static ColorJButton createColorPicker(
            ColorPickerManager colorPickerManager,
            JPanel panel,
            String label,
            Color defaultColor,
            Consumer<Color> onClose
    ) {
        return createColorPicker(colorPickerManager, label, defaultColor, onClose, panel, true);
    }
    private static ColorJButton createColorPicker(
            ColorPickerManager colorPickerManager,
            String label,
            Color defaultColor,
            Consumer<Color> onClose,
            JPanel panel,
            boolean alphaHidden
    ) {
        ColorJButton colorPickerBtn;
        if (defaultColor == null) {
            colorPickerBtn = new ColorJButton("Pick a color", Color.BLACK);
        } else {
            colorPickerBtn = new ColorJButton("#" + (alphaHidden ? ColorUtil.colorToHexCode(defaultColor) : ColorUtil.colorToAlphaHexCode(defaultColor)).toUpperCase(), defaultColor);
        }
        colorPickerBtn.setPreferredSize(new Dimension(100, 25));
        colorPickerBtn.setFocusable(false);
        colorPickerBtn.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent evt)
            {
                RuneliteColorPicker colorPicker = colorPickerManager.create(
                        SwingUtilities.windowForComponent(panel),
                        colorPickerBtn.getColor(),
                        label,
                        alphaHidden);
                colorPicker.setLocation(panel.getLocationOnScreen());
                colorPicker.setOnColorChange(c ->
                {
                    colorPickerBtn.setColor(c);
                    colorPickerBtn.setText("#" + (alphaHidden ? ColorUtil.colorToHexCode(c) : ColorUtil.colorToAlphaHexCode(c)).toUpperCase());
                });
                colorPicker.setOnClose(onClose);
                colorPicker.setVisible(true);
            }
        });

        return colorPickerBtn;
    }

    public static Color colorFromFloatArray(float[] arr) {
        // 3 values = RGB, 4 values = RGBA
        if (arr.length == 3) {
            return new Color(arr[0], arr[1], arr[2]);
        } else if (arr.length > 3) {
            return new Color(arr[0], arr[1], arr[2], arr[3]);
        } else {
            return null;
        }
    }

    public static JPanel createSectionHeader(String title) {
        final JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setMinimumSize(new Dimension(PANEL_WIDTH, 0));

        /* Border */
        final JPanel sectionHeader = new JPanel();
        sectionHeader.setLayout(new BorderLayout());
        sectionHeader.setMinimumSize(new Dimension(PANEL_WIDTH, 0));
        // For whatever reason, the header extends out by a single pixel when closed. Adding a single pixel of
        // border on the right only affects the width when closed, fixing the issue.
        sectionHeader.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR),
                new EmptyBorder(0, 0, 3, 1)));
        section.add(sectionHeader, BorderLayout.SOUTH);

        /* Title */
        final JLabel sectionName = new JLabel(title);
        sectionName.setForeground(ColorScheme.BRAND_ORANGE);
        sectionName.setFont(FontManager.getRunescapeBoldFont());
        sectionName.setToolTipText(title);
        sectionHeader.add(sectionName, BorderLayout.CENTER);

        return section;
    }

    public static <T> JComboBox<T> createComboBox(T[] values, ListCellRenderer<T> renderer) {
        JComboBox<T> comboBox = new JComboBox<>(values);
        comboBox.setRenderer(renderer);
        comboBox.setPreferredSize(new Dimension(PANEL_WIDTH, 30));
        comboBox.setForeground(Color.WHITE);
        comboBox.setFocusable(false);

        return comboBox;
    }

}
