/*
 *
 * Copyright (c) 2000-2003 by Ben smith
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.build.module.map;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.Command;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.configure.NamedHotKeyConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.i18n.Translatable;
import VASSAL.tools.LaunchButton;
import VASSAL.tools.NamedKeyStroke;

import org.w3c.dom.Element;

/**
 * This removes all game pieces from the (@link Map)
 * therefore providing an un-cluttered view.
 */
public class HidePiecesButton extends AbstractConfigurable implements GameComponent, Drawable {
  protected boolean piecesVisible = false;
  protected transient Map map;
  protected LaunchButton launch;
  protected String showingIcon;
  protected String hiddenIcon;
  public static final String DEFAULT_SHOWING_ICON = "/images/globe_unselected.gif"; //NON-NLS
  public static final String DEFAULT_HIDDEN_ICON = "/images/globe_selected.gif"; //NON-NLS

  public static final String LAUNCH_ICON = "icon"; //NON-NLS
  public static final String TOOLTIP = "tooltip"; //NON-NLS
  public static final String BUTTON_TEXT = "buttonText"; //NON-NLS
  public static final String HOTKEY = "hotkey"; //NON-NLS

  public static final String HIDDEN_ICON = "hiddenIcon"; //NON-NLS
  public static final String SHOWING_ICON = "showingIcon"; //NON-NLS


  @SuppressWarnings("this-escape")
  public HidePiecesButton() {
    final ActionListener al = e -> setPiecesVisible(!piecesVisible);
    launch = new LaunchButton(null, TOOLTIP, BUTTON_TEXT, HOTKEY, LAUNCH_ICON, al);
    launch.setAttribute(TOOLTIP, Resources.getString("Editor.HidePiecesButton.hide_all_pieces_on_this_map"));
  }

  /**
   * Expects to be added to a {@link Map}.  Adds itself as a {@link
   * GameComponent} and a {@link Drawable} component */
  @Override
  public void addTo(Buildable b) {
    map = (Map) b;

    GameModule.getGameModule().getGameState().addGameComponent(this);

    map.addDrawComponent(this);

    map.getToolBar().add(launch);

    if (b instanceof Translatable) {
      getI18nData().setOwningComponent((Translatable) b);
    }
  }

  protected void setPiecesVisible(boolean visible) {
    map.setPiecesVisible(visible);
    launch.setAttribute(LAUNCH_ICON, visible ? showingIcon : hiddenIcon);
    piecesVisible = visible;
    map.repaint();
  }

  @Override
  public void add(Buildable b) {
  }

  @Override
  public void remove(Buildable b) {
  }

  @Override
  public void removeFrom(Buildable b) {
    map = (Map) b;
    map.removeDrawComponent(this);
    map.getToolBar().remove(launch);
    GameModule.getGameModule().getGameState().removeGameComponent(this);
  }

  @Override
  public void setAttribute(String key, Object value) {
    if (SHOWING_ICON.equals(key)) {
      showingIcon = (String) value;
    }
    else if (HIDDEN_ICON.equals(key)) {
      hiddenIcon = (String) value;
    }
    else {
      launch.setAttribute(key, value);
    }
  }

  @Override
  public void build(Element e) {
    AutoConfigurable.Util.buildAttributes(e, this);
  }

  @Override
  public String[] getAttributeNames() {
    return new String[]{BUTTON_TEXT, TOOLTIP, HOTKEY, SHOWING_ICON, HIDDEN_ICON};
  }

  @Override
  public String getAttributeValueString(String key) {
    final String s;
    if (HIDDEN_ICON.equals(key)) {
      s = hiddenIcon;
    }
    else if (SHOWING_ICON.equals(key)) {
      s = showingIcon;
    }
    else {
      s = launch.getAttributeValueString(key);
    }
    return s;
  }

  @Override
  public String[] getAttributeDescriptions() {
    return new String[]{
      Resources.getString(Resources.BUTTON_TEXT),
      Resources.getString(Resources.TOOLTIP_TEXT),
      Resources.getString(Resources.HOTKEY_LABEL),
      Resources.getString("Editor.HidePiecesButton.show_icon"), //$NON-NLS-1$
      Resources.getString("Editor.HidePiecesButton.hide_icon"), //$NON-NLS-1$
    };
  }

  @Override
  public Class<?>[] getAttributeTypes() {
    return new Class<?>[]{
      String.class,
      String.class,
      NamedKeyStroke.class,
      ShowingIconConfig.class,
      HiddenIconConfig.class
    };
  }

  public static class ShowingIconConfig implements ConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, DEFAULT_SHOWING_ICON);
    }
  }

  public static class HiddenIconConfig implements ConfigurerFactory {
    @Override
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, DEFAULT_HIDDEN_ICON);
    }
  }

  @Override
  public void draw(Graphics g, Map m) {
    // HidePiecesButton contributes a toolbar button, not map graphics.
  }

  @Override
  public boolean drawAboveCounters() {
    return false;
  }

  @Override
  public Command getRestoreCommand() {
    return null;
  }

  @Override
  public void setup(boolean show) {
    if (show) {
      setPiecesVisible(true);
    }
  }

  public static String getConfigureTypeName() {
    return Resources.getString("Editor.HidePiecesButton.component_type"); //$NON-NLS-1$
  }

  @Override
  public Class<?>[] getAllowableConfigureComponents() {
    return new Class<?>[0];
  }

  @Override
  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("Map.html", "HidePieces"); //NON-NLS
  }

  @Override
  protected String getI18nPrefix() {
    return "HidePieces"; //NON-NLS
  }


  /**
   * {@link VASSAL.search.SearchTarget}
   * @return a list of any Menu/Button/Tooltip Text strings referenced in the Configurable, if any (for search)
   */
  @Override
  public List<String> getMenuTextList() {
    return List.of(getAttributeValueString(BUTTON_TEXT), getAttributeValueString(TOOLTIP));
  }

  /**
   * {@link VASSAL.search.SearchTarget}
   * @return a list of any Named KeyStrokes referenced in the Configurable, if any (for search)
   */
  @Override
  public List<NamedKeyStroke> getNamedKeyStrokeList() {
    return Arrays.asList(NamedHotKeyConfigurer.decode(getAttributeValueString(HOTKEY)));
  }

  /**
   * Classes extending {@link VASSAL.build.AbstractBuildable} should override this method in order to add
   * the names of any image files they use to the collection. For "find unused images" and "search".
   *
   * @param s Collection to add image names to
   */
  @Override
  public void addLocalImageNames(Collection<String> s) {
    String imageName = getAttributeValueString(SHOWING_ICON);
    if (imageName != null) { // Unfortunately these can sometimes be null
      s.add(imageName);
    }
    imageName = getAttributeValueString(HIDDEN_ICON);
    if (imageName != null) {
      s.add(imageName);
    }
  }
}
