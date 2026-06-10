package VASSAL.build.module.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import VASSAL.configure.StringArrayConfigurer;
import VASSAL.i18n.Resources;

import org.junit.jupiter.api.Test;

public class LayeredPieceCollectionTest {
  @Test
  public void constructorStoresDefaultLayerProperty() {
    final LayeredPieceCollection layers = new LayeredPieceCollection();

    assertEquals(
      Resources.getString("Editor.LayeredPieceCollection.layer"),
      layers.getAttributeValueString(LayeredPieceCollection.PROPERTY_NAME)
    );
    assertEquals("", layers.getAttributeValueString(LayeredPieceCollection.LAYER_ORDER));
  }

  @Test
  public void attributesUpdateCollectionSettings() {
    final LayeredPieceCollection layers = new LayeredPieceCollection();
    final String[] layerOrder = {"Low", "High"};

    layers.setAttribute(LayeredPieceCollection.PROPERTY_NAME, "Layer");
    layers.setAttribute(LayeredPieceCollection.LAYER_ORDER, layerOrder);

    assertEquals("Layer", layers.getAttributeValueString(LayeredPieceCollection.PROPERTY_NAME));
    assertEquals(
      StringArrayConfigurer.arrayToString(layerOrder),
      layers.getAttributeValueString(LayeredPieceCollection.LAYER_ORDER)
    );
  }

  @Test
  public void collectionFindsLayerByConfiguredName() {
    final LayeredPieceCollection.Collection collection =
      new LayeredPieceCollection.Collection("Layer", new String[] {"Low", "High"});

    assertEquals(0, collection.getLayerForName("Low"));
    assertEquals(1, collection.getLayerForName("High"));
    assertEquals(-1, collection.getLayerForName("Missing"));
  }
}
