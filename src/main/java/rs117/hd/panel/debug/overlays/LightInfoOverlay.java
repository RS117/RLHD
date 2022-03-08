package rs117.hd.panel.debug.overlays;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import rs117.hd.HdPlugin;

import java.awt.*;

public class LightInfoOverlay extends OverlayPanel
{
	private final Client client;
	private final HdPlugin plugin;

	@Inject
	LightInfoOverlay(Client client, HdPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.getPanel().getDebugPanel().getButtons().getLightInfo().isToggled())
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Light Information")
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Visible Lights")
			.right("" + plugin.getLightManager().visibleLightsCount + "/" + plugin.getConfig().maxDynamicLights().getValue())
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Scene Lights")
			.right("" + plugin.getLightManager().getSceneLights().size())
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Projectile Lights")
			.right("" + plugin.getLightManager().getSceneProjectiles().size())
			.build());

		return super.render(graphics);
	}
}