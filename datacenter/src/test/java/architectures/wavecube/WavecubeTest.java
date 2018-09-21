package architectures.wavecube;

import RHODA.architectures.common.Configuration;
import RHODA.architectures.wavecube.Scheduler;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

public class WavecubeTest {

  @Test
  public void testWavecube() {
    Configuration.getInstance().setNumOfRacks(8);
    Scheduler scheduler = new Scheduler();
    scheduler.start();
  }
}
