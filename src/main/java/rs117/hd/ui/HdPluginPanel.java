package rs117.hd.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class HdPluginPanel extends PluginPanel {

    @Inject
    private ConfigManager configManager;
    @Inject
    private EnvironmentConfigPanel environmentConfigPanel;

    private final boolean developerMode;

    @Inject
    HdPluginPanel(
            final @Named("developerMode") boolean developerMode
    ) {
        super();
        this.developerMode = developerMode;

        setBackground(ColorScheme.DARK_GRAY_COLOR);
    }

    public void populatePanel() {
        add(UiUtils.createSectionHeader("Environment Configs"));
        add(environmentConfigPanel);
    }
}
