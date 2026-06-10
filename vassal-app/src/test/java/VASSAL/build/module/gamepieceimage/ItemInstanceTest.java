package VASSAL.build.module.gamepieceimage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ItemInstanceTest {

  @BeforeEach
  public void setupColorManager() {
    new ColorManager().addTo(null);
  }

  @Test
  public void textItemDefaultValueUsesNamePrefix() {
    assertEquals("Xx", new TextItemInstance("", TextItem.TYPE, "", null).getValue());
    assertEquals("A", new TextItemInstance("A", TextItem.TYPE, "", null).getValue());
    assertEquals("Ab", new TextItemInstance("Abc", TextItem.TYPE, "", null).getValue());
  }

  @Test
  public void itemInstancesRoundTripThroughEncoding() {
    final List<ItemInstance> items = List.of(
      new TextItemInstance("Text", TextItem.TYPE, GamePieceLayout.N, "Hi"),
      new TextBoxItemInstance("Box", TextBoxItem.TYPE, GamePieceLayout.CENTER),
      new SymbolItemInstance(
        "Unit",
        SymbolItem.TYPE,
        GamePieceLayout.S,
        Symbol.NatoUnitSymbolSet.SZ_DIVISION,
        Symbol.NatoUnitSymbolSet.INFANTRY,
        Symbol.NatoUnitSymbolSet.NONE
      ),
      new ShapeItemInstance("Shape", ShapeItem.TYPE, GamePieceLayout.CENTER),
      new ImageItemInstance("Image", ImageItem.TYPE, GamePieceLayout.N, "counter.png")
    );

    final List<ItemInstance> decoded =
      InstanceConfigurer.StringToProperties(InstanceConfigurer.PropertiesToString(items), null);

    assertEquals(items.size(), decoded.size());
    assertInstanceOf(TextItemInstance.class, decoded.get(0));
    assertInstanceOf(TextBoxItemInstance.class, decoded.get(1));
    assertInstanceOf(SymbolItemInstance.class, decoded.get(2));
    assertInstanceOf(ShapeItemInstance.class, decoded.get(3));
    assertInstanceOf(ImageItemInstance.class, decoded.get(4));

    for (int i = 0; i < items.size(); i++) {
      assertEquals(items.get(i).encode(), decoded.get(i).encode());
    }
  }
}
