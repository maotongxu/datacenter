package architectures.wavecube;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.TrafficPattern;
import RHODA.architectures.fattree.ConfigurationFT;
import RHODA.architectures.output.Metrics;
import RHODA.architectures.wavecube.InterRackTx;
import RHODA.architectures.wavecube.Scheduler;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

public class WavecubeTest {

  @Test
  public void testWavecube() {
    Configuration.getInstance().setNumOfRacks(8);

    DataCenter dataCenter = new DataCenter();
    TrafficPattern trafficPattern = new TrafficPattern(dataCenter);
    trafficPattern.loadTrafficToRacks(importTestTraffic());

    InterRackTx interRackTx = new InterRackTx(dataCenter);
    interRackTx.interRackTx();
    System.out.println("NumOfHops " + Metrics.calculateAvgNumOfHops() +
        " AvgRackLoad " + Metrics.calculateAvgRackLoad() +
        " TotalTraffic " + Metrics.calculateTotalTraffic());
  }

  public double[][] importTestTraffic() {
    int numOfRacks = Configuration.getInstance().getNumOfRacks();
    double[][] flowMatrix = new double[numOfRacks][numOfRacks];
    flowMatrix[0][6] = 1;
    return flowMatrix;
  }
}
