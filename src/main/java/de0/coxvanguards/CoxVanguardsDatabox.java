package de0.coxvanguards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class CoxVanguardsDatabox extends OverlayPanel {

  private CoxVanguardsPlugin plugin;
  private CoxVanguardsConfig config;

  @Inject
  public CoxVanguardsDatabox(CoxVanguardsPlugin plugin, CoxVanguardsConfig config, Client client) {
    super(plugin);
    this.plugin = plugin;
    this.config = config;
    setPosition(OverlayPosition.TOP_LEFT);
  }

  @Override
  public Dimension render(Graphics2D g) {
    if (!config.showDatabox()) return null;

    List<LayoutableRenderableEntity> elems = panelComponent.getChildren();
    elems.clear();
    elems.add(TitleComponent.builder().text("Vanguards").build());
    elems.add(LineComponent.builder().left("Melee").leftColor(Color.RED)
        .right(Integer.toString(plugin.melhp * 100 / 30)).build());
    elems.add(LineComponent.builder().left("Range").leftColor(Color.GREEN)
        .right(Integer.toString(plugin.rnghp * 100 / 30)).build());
    elems.add(LineComponent.builder().left("Mage").leftColor(Color.CYAN)
        .right(Integer.toString(plugin.maghp * 100 / 30)).build());
    return panelComponent.render(g);
  }
}
