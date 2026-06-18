package VASSAL.tools.image.tilecache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TileThreadPoolsTest {
  @Test
  public void defaultWorkerCountUsesAtLeastOneThread() {
    assertEquals(1, TileThreadPools.defaultWorkerCount(0));
  }

  @Test
  public void defaultWorkerCountUsesAvailableProcessorsBelowCap() {
    assertEquals(4, TileThreadPools.defaultWorkerCount(4));
  }

  @Test
  public void defaultWorkerCountCapsLargeMachines() {
    assertEquals(8, TileThreadPools.defaultWorkerCount(64));
  }

  @Test
  public void configuredWorkerCountOverridesDefault() {
    assertEquals(12, TileThreadPools.workerCount("12", 4));
  }

  @Test
  public void nonPositiveConfiguredWorkerCountFallsBackToDefault() {
    assertEquals(4, TileThreadPools.workerCount("0", 4));
    assertEquals(4, TileThreadPools.workerCount("-2", 4));
  }

  @Test
  public void invalidConfiguredWorkerCountFallsBackToDefault() {
    assertEquals(4, TileThreadPools.workerCount("fast", 4));
    assertEquals(4, TileThreadPools.workerCount(" ", 4));
  }
}
