package architectures.osa;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.osa.InterRackTopologyConfiguration;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

import java.util.Set;
import java.util.Map;

public class InterRackTopologyConfigurationTest {

  private InterRackTopologyConfiguration interRackTopologyConfiguration;

  @BeforeClass
  public void init() {
    Configuration.getInstance().setNumOfRacks(8);
    Configuration.getInstance().setNumOfTransceiversPerRack(2);

    DataCenter dataCenter = new DataCenter();
    interRackTopologyConfiguration = new InterRackTopologyConfiguration(dataCenter);
  }

  @Test
  public void test() {
    interRackTopologyConfiguration.buildInterRackTopology();
    Map<Integer, Set<Integer>> connections = interRackTopologyConfiguration.getConnections();
    System.out.println(connections);
  }
}
