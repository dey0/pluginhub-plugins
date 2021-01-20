package de0.coxvanguards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

import javax.inject.Inject;

import de0.util.CoxUtil;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import static de0.util.CoxUtil.*;

public class CoxVanguardsDatabox extends Overlay {

  private CoxVanguardsPlugin plugin;
  private CoxVanguardsConfig config;
  private Client client;
  private PanelComponent panelComponent;

  @Inject
  public CoxVanguardsDatabox(CoxVanguardsPlugin plugin, CoxVanguardsConfig config,
      Client client) {
    super(plugin);
    this.plugin = plugin;
    this.config = config;
    this.client = client;
    setPosition(OverlayPosition.TOP_LEFT);
    panelComponent = new PanelComponent();
    panelComponent.getChildren()
        .add(TitleComponent.builder().text("Vanguards").build());
    panelComponent.getChildren().add(
        LineComponent.builder().left("Melee").leftColor(Color.RED).build());
    panelComponent.getChildren().add(
        LineComponent.builder().left("Range").leftColor(Color.GREEN).build());
    panelComponent.getChildren().add(
        LineComponent.builder().left("Mage").leftColor(Color.CYAN).build());
  }

  @Override
  public Dimension render(Graphics2D g) {
    if (!config.showDatabox())
      return null;

    WorldPoint wp = client.getLocalPlayer().getWorldLocation();
    int plane = client.getPlane();
    int x = wp.getX() - client.getBaseX();
    int y = wp.getY() - client.getBaseY();
    int type = CoxUtil
        .getroom_type(client.getInstanceTemplateChunks()[plane][x / 8][y / 8]);
    if (type == VANGUARDS) {
      List<LayoutableRenderableEntity> elems = panelComponent.getChildren();
      ((LineComponent) elems.get(1))
          .setRight(Integer.toString(plugin.melhp * 100 / 30));
      ((LineComponent) elems.get(2))
          .setRight(Integer.toString(plugin.rnghp * 100 / 30));
      ((LineComponent) elems.get(3))
          .setRight(Integer.toString(plugin.maghp * 100 / 30));
      return this.panelComponent.render(g);
    }

    return null;
  }
}
