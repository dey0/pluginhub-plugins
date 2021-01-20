
package de0.coxthieving;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import de0.coxthieving.CoxThievingPlugin.GrubCollection;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class CoxThievingDatabox extends Overlay {

  private CoxThievingPlugin plugin;
  private CoxThievingConfig config;
  private PanelComponent panelComponent = new PanelComponent();

  @Inject
  public CoxThievingDatabox(CoxThievingPlugin plugin, CoxThievingConfig config) {
    super(plugin);
    this.plugin = plugin;
    this.config = config;
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    int myindex;
    if (plugin.gc_local == null || plugin.gc_local.num_opened == 0) {
      myindex = -1;
    } else {
      myindex = Arrays.binarySearch(plugin.gc_others, 0, plugin.gc_others_count,
          plugin.gc_local, plugin.comparator);
      if (myindex < 0)
        myindex = -myindex - 1;
    }
    int sum_grubs = plugin.num_grubs;
    for (int i = 0; i < plugin.gc_others_count; i++)
      sum_grubs += plugin.gc_others[i].num_with_grubs * config.grubRate() / 100;

    List<LayoutableRenderableEntity> elems = panelComponent.getChildren();
    elems.clear();
    elems.add(TitleComponent.builder().color(Color.WHITE).text(
        (sum_grubs == plugin.num_grubs ? "Grub count: " : "Est. grub count: ")
            + sum_grubs)
        .build());
    for (int i = 0; i < plugin.gc_others_count; i++) {
      if (i == myindex)
        add_gc_line(elems, plugin.gc_local);
      add_gc_line(elems, plugin.gc_others[i]);
    }
    if (myindex == plugin.gc_others_count)
      add_gc_line(elems, plugin.gc_local);
    return this.panelComponent.render(graphics);
  }

  private void add_gc_line(List<LayoutableRenderableEntity> elems,
      GrubCollection gc) {
    elems.add(LineComponent.builder().left(gc.displayname)
        .right(gc.num_with_grubs + "/" + gc.num_opened).build());
  }

}
