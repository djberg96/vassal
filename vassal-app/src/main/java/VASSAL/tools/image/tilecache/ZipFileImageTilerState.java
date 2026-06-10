package VASSAL.tools.image.tilecache;

public class ZipFileImageTilerState {
  private ZipFileImageTilerState() {}

  public static final String READY = "TILER READY";
  public static final char IMAGE_BEGIN = 'i';
  public static final char IMAGE_END = 'I';
  public static final char TILE_END = '*';
  public static final char DONE = 'D';
}
