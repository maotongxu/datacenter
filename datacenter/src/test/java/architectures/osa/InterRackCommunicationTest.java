package architectures.osa;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.Path;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.ShortestPathAmongNodes;
import RHODA.architectures.osa.InterRackCommunication;
import RHODA.architectures.osa.InterRackTopologyConfiguration;
import RHODA.architectures.osa.Scheduler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

import java.util.Set;
import java.util.Map;

public class InterRackCommunicationTest {
  private InterRackTopologyConfiguration interRackTopologyConfiguration;
  private InterRackCommunication interRackCommunication;

  @BeforeClass
  public void init() {
    Configuration.getInstance().setNumOfRacks(8);
    Configuration.getInstance().setNumOfTransceiversPerRack(2);

    DataCenter dataCenter = new DataCenter();
    interRackTopologyConfiguration = new InterRackTopologyConfiguration(dataCenter);
  }

  @Test
  public void test() {
    Scheduler scheduler = new Scheduler();

    interRackTopologyConfiguration.buildInterRackTopology();
    Map<Integer, Set<Integer>> connections = interRackTopologyConfiguration.getConnections();
    ShortestPathAmongNodes shortestPath = new ShortestPathAmongNodes(connections);
    shortestPath.findShortestPath();
    Map<SrcDstPair, Path> paths = shortestPath.getPairPathMap();
    interRackCommunication = new InterRackCommunication(scheduler, paths);
    interRackCommunication.interTx();
  }
}
