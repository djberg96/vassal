/*
 * Copyright (c) 2020 by The VASSAL development team
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
package VASSAL.configure;

import VASSAL.build.GameModule;
import VASSAL.i18n.Resources;
import VASSAL.tools.ReadErrorDialog;
import VASSAL.tools.filechooser.FileChooser;
import VASSAL.tools.filechooser.ImageFileFilter;
import VASSAL.tools.image.LabelUtils;
import VASSAL.tools.image.MultiResolutionRenderedImage;
import VASSAL.tools.imageop.Op;
import VASSAL.tools.imageop.OpIcon;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Allows a user to select from the images currently available in a module, or
 * to open a File Dialog to import a new image.
 *
 * Designed to be a drop-in replacement for {@link VASSAL.counters.ImagePicker},
 * except implemented as a proper Configurer.
 *
 * The value stored is the archive-relative image name.
 */
public final class ImageSelector extends Configurer implements ItemListener {

  private static final String NO_IMAGE = "(" + Resources.getString("Editor.ImagePicker.no_image") + ")";
  static final String ALL_BUCKETS = "(" + Resources.getString("Editor.imageSelector.all_buckets") + ")";
  static final String NO_BUCKET = "(" + Resources.getString("Editor.imageSelector.no_bucket") + ")";

  private static final int DEFAULT_SIZE = 64;

  private JPanel controls;
  private JComboBox<String> bucketSelect;
  private JComboBox<ImageChoice> select;
  private Icon noImage;
  private JLabel imageViewer;
  private String imageName;
  private final JLabel imageScale = new JLabel();
  private final OpIcon icon = new OpIcon();
  private final int maxWidth;
  private final int maxHeight;
  private String[] imageNames = new String[0];
  private boolean updatingControls;

  public ImageSelector(String key, String name, String val, int maxWidth, int maxheight) {
    super(key, name, val);
    this.maxWidth = maxWidth;
    this.maxHeight = maxheight;
    initControls();
    setValue(val);
  }

  public ImageSelector(String key, String name, String val) {
    this(key, name, val, -1, -1);
  }

  public ImageSelector(String key, String name) {
    this(key, name, null);
  }

  public ImageSelector(String val, int maxWidth, int maxheight) {
    this(null, "", val, maxWidth, maxheight);
  }

  public ImageSelector(String val) {
    this(null, "", val);
  }

  public ImageSelector() {
    this (null, "", "", DEFAULT_SIZE, DEFAULT_SIZE);
  }

  @Override
  public String getValueString() {
    return (String) value;
  }

  @Override
  public void setValue(String s) {
    if (s == null || s.isBlank()) {
      imageName = null;
      imageViewer.setIcon(getNoImageIcon());
      imageViewer.setPreferredSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
    }
    else {
      if (s.equals(imageName)) {
        // We have to do this because we have no way of calling update on
        // any ImageOps which depend on this image.
        Op.clearCache();
      }
      else {
        imageName = s;
      }

      icon.setOp(Op.load(s));

      // Is the image too large?
      if (maxWidth > 0 && (icon.getIconWidth() > maxWidth || icon.getIconHeight() > maxHeight)) {
        final double xRatio = (double) maxWidth / icon.getIconWidth();
        final double yRatio = (double) maxHeight / icon.getIconHeight();
        final double newScale = Math.min(xRatio, yRatio);
        icon.setOp(Op.scale(Op.load(s), newScale));
        imageScale.setText("(" + (int) (newScale * 100) + "%)");
        imageScale.setVisible(true);
      }
      else {
        imageScale.setVisible(false);
      }

      imageViewer.setIcon(icon);
      imageViewer.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
    }

    if (s != null) {
      final String bucket = bucketOf(s);
      setSelectedBucket(bucket.isEmpty() ? NO_BUCKET : bucket);
    }

    updatingControls = true;
    try {
      select.setSelectedItem(ImageChoice.of(s, selectedBucket()));
      if (s == null || s.isBlank()) {
        select.setSelectedIndex(0);
      }
      else if (!Objects.equals(s, selectedImageName())) {
        final String gifName = s + ".gif"; // NON-NLS
        setSelectedBucket(bucketOf(gifName));
        select.setSelectedItem(ImageChoice.of(gifName, selectedBucket()));
      }
    }
    finally {
      updatingControls = false;
    }

    controls.revalidate();
    repack(controls);

    setValue((Object) s);
  }

  @Override
  public Component getControls() {
    if (controls == null) {
      initControls();
    }
    return controls;
  }

  private void initControls() {
    controls = new JPanel(new MigLayout("hidemode 3", "[][grow][]", "[]rel[]rel[]")); // NON-NLS
    controls.setBorder(BorderFactory.createEtchedBorder());

    final JButton addButton = new JButton(Resources.getString("Editor.imageSelector.add_image"));
    addButton.addActionListener(e -> pickImage());
    final JButton copyButton = new JButton(Resources.getString("Editor.imageSelector.copy_to_bucket"));
    copyButton.addActionListener(e -> copySelectedImageToBucket());
    final JButton clearButton = new JButton(Resources.getString("Editor.imageSelector.clear_image"));
    clearButton.addActionListener(e -> clearImage());

    imageNames = GameModule.getGameModule().getDataArchive().getImageNames();
    bucketSelect = new JComboBox<>(bucketsFor(imageNames));
    bucketSelect.setEditable(true);
    bucketSelect.addPopupMenuListener(new RefreshImagesPopupListener());
    bucketSelect.addItemListener(e -> {
      if (!updatingControls && e.getStateChange() == ItemEvent.SELECTED) {
        refreshImageChoices(null);
      }
    });

    select = new JComboBox<>();
    select.addPopupMenuListener(new RefreshImagesPopupListener());
    refreshImageChoices(null);
    select.addItemListener(this);

    imageViewer = new JLabel(getNoImageIcon());
    imageViewer.setPreferredSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));

    controls.add(new JLabel(Resources.getString("Editor.imageSelector.bucket")), "alignx right"); // NON-NLS
    controls.add(bucketSelect, "grow,wrap"); // NON-NLS
    controls.add(new JLabel(Resources.getString("Editor.imageSelector.image")), "alignx right"); // NON-NLS
    controls.add(select, "grow"); // NON-NLS
    controls.add(addButton, "split,sg 1"); // NON-NLS
    controls.add(copyButton, "sg 1"); // NON-NLS
    controls.add(clearButton, "sg 1,wrap"); // NON-NLS
    controls.add(imageViewer, "span 3,alignx center,wrap"); // NON-NLS
    controls.add(imageScale, "span 3,alignx center"); // NON-NLS
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    if (!updatingControls && e.getStateChange() == ItemEvent.SELECTED) {
      setValue(selectedImageName());
    }
  }

  private void pickImage() {
    final GameModule gm = GameModule.getGameModule();
    final FileChooser fc = gm.getEditorImageChooser();
    fc.setFileFilter(new ImageFileFilter());

    if (fc.showOpenDialog(gm.getPlayerWindow()) == FileChooser.APPROVE_OPTION) {
      final String bucket = normalizedSelectedBucket();
      final String name = imageNameForBucket(bucket, fc.getSelectedFile().getName());
      gm.getArchiveWriter().addImage(fc.getSelectedFile().getPath(), name);
      imageNames = gm.getDataArchive().getImageNames();
      refreshBuckets(bucket);
      refreshImageChoices(name);
      setValue(name);
    }
    else {
      setValue(null);
    }
  }

  private void clearImage() {
    setValue(null);
  }

  private void copySelectedImageToBucket() {
    final String currentImage = imageName;
    final String bucket = normalizedSelectedBucket();
    if (currentImage == null || currentImage.isBlank() || bucket.isBlank() || bucket.equals(bucketOf(currentImage))) {
      return;
    }

    final String bucketedImage = imageNameForBucket(bucket, baseName(currentImage));
    if (copyImage(currentImage, bucketedImage)) {
      imageNames = GameModule.getGameModule().getDataArchive().getImageNames();
      refreshBuckets(bucket);
      refreshImageChoices(bucketedImage);
      setValue(bucketedImage);
    }
  }

  private Icon getNoImageIcon() {
    if (noImage == null) {
      noImage = new ImageIcon(new MultiResolutionRenderedImage(
        DEFAULT_SIZE,
        DEFAULT_SIZE,
        LabelUtils::noImageBoxImage
      ));
    }
    return noImage;
  }

  public String getImageName() {
    return imageName;
  }

  private void refreshBuckets(String preferredBucket) {
    updatingControls = true;
    try {
      bucketSelect.setModel(new DefaultComboBoxModel<>(bucketsFor(imageNames)));
      bucketSelect.setSelectedItem(preferredBucket == null || preferredBucket.isBlank() ? NO_BUCKET : preferredBucket);
    }
    finally {
      updatingControls = false;
    }
  }

  private void refreshImageNames() {
    final String[] latestImageNames = GameModule.getGameModule().getDataArchive().getImageNames();
    if (Arrays.equals(imageNames, latestImageNames)) {
      return;
    }

    final String bucket = selectedBucket();
    final String selectedImage = selectedImageName();
    imageNames = latestImageNames;
    refreshBuckets(bucket);
    refreshImageChoices(selectedImage);
  }

  private void refreshImageChoices(String selectedImage) {
    final String currentImage = selectedImage != null ? selectedImage : imageName;
    updatingControls = true;
    try {
      select.setModel(new DefaultComboBoxModel<>(imageChoicesForBucket(imageNames, selectedBucket())));
      select.setSelectedItem(ImageChoice.of(currentImage, selectedBucket()));
      if (currentImage == null || !Objects.equals(currentImage, selectedImageName())) {
        select.setSelectedIndex(0);
      }
    }
    finally {
      updatingControls = false;
    }
  }

  private void setSelectedBucket(String bucket) {
    final String selected = bucket == null || bucket.isBlank() ? NO_BUCKET : bucket;
    if (!Objects.equals(selected, bucketSelect.getSelectedItem())) {
      updatingControls = true;
      try {
        bucketSelect.setSelectedItem(selected);
      }
      finally {
        updatingControls = false;
      }
      refreshImageChoices(imageName);
    }
  }

  private String selectedBucket() {
    final Object selected = bucketSelect.getSelectedItem();
    return selected == null ? ALL_BUCKETS : selected.toString();
  }

  private String normalizedSelectedBucket() {
    final String selected = selectedBucket();
    if (selected.equals(ALL_BUCKETS) || selected.equals(NO_BUCKET)) {
      return "";
    }
    return normalizeBucket(selected);
  }

  private String selectedImageName() {
    final Object selected = select.getSelectedItem();
    return selected instanceof ImageChoice choice ? choice.imageName() : null;
  }

  private boolean copyImage(String sourceName, String targetName) {
    if (sourceName.equals(targetName)) {
      return true;
    }

    final GameModule gm = GameModule.getGameModule();
    final String imagePrefix = gm.getDataArchive().getImagePrefix();
    try {
      if (gm.getDataArchive().contains(imagePrefix + targetName)) {
        return true;
      }

      final byte[] imageBytes;
      try (InputStream in = gm.getDataArchive().getInputStream(imagePrefix + sourceName)) {
        imageBytes = in.readAllBytes();
      }

      gm.getArchiveWriter().addImage(targetName, imageBytes);
      return true;
    }
    catch (IOException e) {
      ReadErrorDialog.error(e, sourceName);
      return false;
    }
  }

  static String[] bucketsFor(String[] images) {
    final SortedSet<String> buckets = new TreeSet<>();
    buckets.add(ALL_BUCKETS);
    buckets.add(NO_BUCKET);

    Arrays.stream(images)
      .map(ImageSelector::bucketOf)
      .filter(bucket -> !bucket.isBlank())
      .forEach(buckets::add);

    return buckets.toArray(new String[0]);
  }

  static ImageChoice[] imageChoicesForBucket(String[] images, String bucket) {
    final String normalizedBucket = normalizeBucket(bucket);
    final boolean allBuckets = bucket == null || bucket.equals(ALL_BUCKETS);

    return ArrayUtils.addFirst(
      Arrays.stream(images)
        .filter(image -> allBuckets || bucketOf(image).equals(normalizedBucket))
        .map(image -> ImageChoice.of(image, bucket))
        .toArray(ImageChoice[]::new),
      ImageChoice.NO_IMAGE
    );
  }

  static String imageNameForBucket(String bucket, String fileName) {
    final String normalizedBucket = normalizeBucket(bucket);
    return normalizedBucket.isEmpty() ? fileName : normalizedBucket + "/" + fileName;
  }

  static String bucketOf(String imageName) {
    if (imageName == null) {
      return "";
    }

    final int index = imageName.lastIndexOf('/');
    return index == -1 ? "" : imageName.substring(0, index);
  }

  static String baseName(String imageName) {
    if (imageName == null) {
      return "";
    }

    final int index = imageName.lastIndexOf('/');
    return index == -1 ? imageName : imageName.substring(index + 1);
  }

  static String normalizeBucket(String bucket) {
    if (bucket == null || bucket.equals(ALL_BUCKETS) || bucket.equals(NO_BUCKET)) {
      return "";
    }

    return Arrays.stream(bucket.trim().replace('\\', '/').split("/"))
      .map(String::trim)
      .filter(segment -> !segment.isBlank())
      .filter(segment -> !segment.equals("."))
      .filter(segment -> !segment.equals(".."))
      .reduce((left, right) -> left + "/" + right)
      .orElse("");
  }

  record ImageChoice(String imageName, String displayName) {
    static final ImageChoice NO_IMAGE = new ImageChoice(null, ImageSelector.NO_IMAGE);

    static ImageChoice of(String imageName, String bucket) {
      if (imageName == null || imageName.isBlank()) {
        return NO_IMAGE;
      }

      final boolean allBuckets = bucket == null || bucket.equals(ALL_BUCKETS);
      return new ImageChoice(imageName, allBuckets ? imageName : baseName(imageName));
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  private final class RefreshImagesPopupListener implements PopupMenuListener {
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      if (!updatingControls) {
        refreshImageNames();
      }
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }
  }
}
