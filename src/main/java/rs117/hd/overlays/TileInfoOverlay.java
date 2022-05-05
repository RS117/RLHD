package rs117.hd.overlays;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;
import net.runelite.api.Client;
import static net.runelite.api.Constants.MAX_Z;
import static net.runelite.api.Constants.SCENE_SIZE;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.apache.commons.lang3.tuple.Pair;
import rs117.hd.data.materials.Material;
import rs117.hd.data.materials.Overlay;
import rs117.hd.data.materials.Underlay;
import rs117.hd.utils.HDUtils;

public class TileInfoOverlay extends net.runelite.client.ui.overlay.Overlay
{
	private final Client client;
	private Point mousePos;

	@Inject
	public TileInfoOverlay(Client client)
	{
		this.client = client;
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!client.isMenuOpen())
		{
			mousePos = client.getMouseCanvasPosition();
		}

		if (mousePos != null && mousePos.getX() == -1 && mousePos.getY() == -1)
		{
			return null;
		}

		g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

		Scene scene = client.getScene();
		byte[][][] overlayIds = scene.getOverlayIds();
		byte[][][] underlayIds = scene.getUnderlayIds();
		Tile[][][] tiles = scene.getTiles();
		for (int x = 0; x < SCENE_SIZE; x++)
		{
			for (int y = 0; y < SCENE_SIZE; y++)
			{
				for (int plane = MAX_Z - 1; plane >= 0; plane--)
				{
					Tile tile = tiles[plane][x][y];
					if (tile == null)
					{
						continue;
					}

					LocalPoint loc = tile.getLocalLocation();
					Point tileCenter = Perspective.localToCanvas(client, loc, 0);
					if (tileCenter == null)
					{
						continue;
					}

					Polygon poly = Perspective.getCanvasTilePoly(client, loc);
					if (poly != null && poly.contains(mousePos.getX(), mousePos.getY()))
					{
						SceneTilePaint paint = tile.getSceneTilePaint();
						SceneTileModel model = tile.getSceneTileModel();

						if ((paint == null || paint.getNeColor() == 12345678) && model == null)
						{
							continue;
						}

						ArrayList<String> lines = new ArrayList<>();

						int overlayId = overlayIds[plane][x][y];
						Overlay overlay = Overlay.getOverlay(overlayId, tile, client);
						lines.add(String.format("Overlay: %s (%d)", overlay.name(), overlayId));

						int underlayId = underlayIds[plane][x][y];
						Underlay underlay = Underlay.getUnderlay(underlayId, tile, client);
						lines.add(String.format("Underlay: %s (%d)", underlay.name(), underlayId));

						Color polyColor;
						if (paint != null)
						{
							polyColor = Color.CYAN;
							lines.add("Tile type: Paint");
							lines.add("RGB: " + Arrays.toString(HDUtils.colorIntToRGB(paint.getRBG())));
							Material material = Material.getTexture(paint.getTexture());
							lines.add(String.format("Material: %s (%d)", material.name(), paint.getTexture()));
						}
						else
						{
							polyColor = Color.ORANGE;
							lines.add("Tile type: Model");
							lines.add(String.format("Face count: %d", model.getFaceX().length));
							HashSet<String> uniqueMaterials = new HashSet<>();
							int numChars = 0;
							if (model.getTriangleTextureId() != null)
							{
								for (int texture : model.getTriangleTextureId())
								{
									String material = String.format("%s (%d)", Material.getTexture(texture).name(), texture);
									boolean unique = uniqueMaterials.add(material);
									if (unique)
									{
										numChars += material.length();
									}
								}
							}

							ArrayList<String> materials = new ArrayList<>(uniqueMaterials);
							Collections.sort(materials);

							if (materials.size() <= 1 || numChars < 26)
							{
								StringBuilder sb = new StringBuilder("Materials: { ");
								if (materials.size() == 0)
								{
									sb.append("null");
								}
								else
								{
									String prefix = "";
									for (String m : materials)
									{
										sb.append(prefix).append(m);
										prefix = ", ";
									}
								}
								sb.append(" }");
								lines.add(sb.toString());
							}
							else
							{
								Iterator<String> iter = materials.iterator();
								lines.add("Materials: { " + iter.next() + ",");
								while (iter.hasNext())
								{
									lines.add("\t  " + iter.next() + (iter.hasNext() ? "," : " }"));
								}
							}
						}

						int padding = 4;
						FontMetrics fm = g.getFontMetrics();
						int lineHeight = fm.getHeight();
						int totalHeight = lineHeight * lines.size();
						int space = fm.charWidth(':');
						int indent = fm.stringWidth("{ ");
						int px = tileCenter.getX();
						int py = tileCenter.getY();
						int offsetY = -totalHeight - 20;

						int leftWidth = 0;
						int rightWidth = 0;

						Function<String, Pair<String, String>> splitter = line ->
						{
							int i = line.indexOf(":");
							String left = line;
							String right = "";
							if (i != -1)
							{
								left = line.substring(0, i);
								right = line.substring(i + 1);
							}
							else if (left.startsWith("\t"))
							{
								right = left;
								left = "";
							}

							return Pair.of(left, right);
						};

						for (String line : lines)
						{
							Pair<String, String> pair = splitter.apply(line);
							leftWidth = Math.max(leftWidth, fm.stringWidth(pair.getLeft()));
							rightWidth = Math.max(rightWidth, fm.stringWidth(pair.getRight()));
						}

						g.setColor(polyColor);
						g.drawPolygon(poly);

						g.setColor(new Color(0, 0, 0, 150));
						int totalWidth = leftWidth + rightWidth + space + padding * 4;
						g.fillRect(
							px - totalWidth / 2, py + offsetY - padding,
							totalWidth, totalHeight + padding * 3);
						px -= (leftWidth + rightWidth + space) / 2 - leftWidth;

						for (String line : lines)
						{
							Pair<String, String> pair = splitter.apply(line);
							offsetY += lineHeight;
							Point p = new Point(
								px - fm.stringWidth(pair.getLeft()) + (pair.getRight().startsWith("\t") ? indent : 0),
								py + offsetY);
							OverlayUtil.renderTextLocation(g, p, line, Color.WHITE);
						}

						return null;
					}
				}
			}
		}

		return null;
	}
}
