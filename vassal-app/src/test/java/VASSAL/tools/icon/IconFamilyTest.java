package VASSAL.tools.icon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import VASSAL.build.Configurable;

public class IconFamilyTest {

  @Test
  public void constructorStoresNameAndIconPaths() {
    final IconFamily family = new IconFamily(
      "family",
      "icons/scalable/family.svg",
      new String[] {
        "icons/16x16/family.png",
        "icons/22x22/family.png",
        "icons/32x32/family.png",
        "icons/48x48/family.png"
      }
    );

    assertEquals("family", family.getConfigureName());
    assertEquals("family", family.getLocalizedConfigureName());
    assertEquals("icons/scalable/family.svg", family.getAttributeValueString(IconFamily.SCALABLE_ICON));
    assertEquals("icons/16x16/family.png", family.getAttributeValueString(IconFamily.ICON0));
    assertEquals("icons/22x22/family.png", family.getAttributeValueString(IconFamily.ICON1));
    assertEquals("icons/32x32/family.png", family.getAttributeValueString(IconFamily.ICON2));
  }

  @Test
  public void setConfigureNameFiresPropertyChangeAfterListenerRegistration() {
    final IconFamily family = new IconFamily("old");
    final AtomicReference<PropertyChangeEvent> event = new AtomicReference<>();

    family.addPropertyChangeListener(event::set);
    family.setConfigureName("new");

    assertSame(family, event.get().getSource());
    assertEquals(Configurable.NAME_PROPERTY, event.get().getPropertyName());
    assertEquals("old", event.get().getOldValue());
    assertEquals("new", event.get().getNewValue());
  }
}
