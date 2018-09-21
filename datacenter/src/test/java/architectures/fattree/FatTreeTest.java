package architectures.fattree;

import RHODA.architectures.fattree.Scheduler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

public class FatTreeTest {
  @Test
  public void testFatTree() {
    Scheduler scheduler = new Scheduler();
    scheduler.start();
  }
}
