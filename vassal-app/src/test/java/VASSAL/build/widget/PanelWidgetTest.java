package VASSAL.build.widget;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import VASSAL.configure.Configurer;

class PanelWidgetTest {
  @Test
  void savedColumnCountsBelowOneAreClamped() {
    final PanelWidget widget = new PanelWidget();

    widget.setAttribute(PanelWidget.COLS, "0");

    assertEquals("1", widget.getAttributeValueString(PanelWidget.COLS));
  }

  @Test
  void columnCountEditorDoesNotKeepValuesBelowOne() {
    final Configurer config = new PanelWidget.ColumnCountConfig()
      .getConfigurer(new PanelWidget(), PanelWidget.COLS, "");

    config.setValue("-4");

    assertEquals("1", config.getValueString());
  }

  @Test
  void columnCountUsesBoundedConfigurer() {
    final var types = new PanelWidget().getAttributeTypes();

    assertSame(PanelWidget.ColumnCountConfig.class, types[3]);
  }
}
