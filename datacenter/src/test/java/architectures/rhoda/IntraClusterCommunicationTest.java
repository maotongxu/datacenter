package architectures.rhoda;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.Path;
import RHODA.architectures.api.Rack;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.rhoda.*;
import RHODA.architectures.rhoda.IntraClusterTopology.IntraClusterTopologyConfigurationSN;
import architectures.common.TrafficPattern;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

import java.util.Map;
import java.util.Set;

public class IntraClusterCommunicationTest {
  private DataCenterRHODA dataCenterRHODA;
  private Scheduler scheduler;
  private ClusterMembershipConfiguration clusterMembership;
  private IntraClusterTopologyConfigurationSN intraClusterTopologyConfiguration;
  private IntraClusterCommunication intraClusterCommunication;

  @BeforeClass
  public void init() {
    Configuration.getInstance().setNumOfRacks(8);
    Configuration.getInstance().setMembershipRatio(1);
    Configuration.getInstance().setNumOfRacksPerCluster(4);
    Configuration.getInstance().setNumOfTransceiversPerRack(2);

    DataCenter dataCenter = new DataCenter();
    TrafficPattern trafficPattern = new TrafficPattern(dataCenter);
    trafficPattern.importTrafficToRacks();

    dataCenterRHODA = new DataCenterRHODA(dataCenter);
    scheduler = new Scheduler();

    clusterMembership = new ClusterMembershipConfiguration(dataCenterRHODA);
    clusterMembership.configureClusterMembership();

    intraClusterTopologyConfiguration = new IntraClusterTopologyConfigurationSN(dataCenterRHODA);
    intraClusterTopologyConfiguration.buildConnections();
  }

  @Test
  public void testRackToRackTx() {
    Map<Integer, Map<Integer, Set<Integer>>> withinClusterConnections =
        intraClusterTopologyConfiguration.getWithinClusterConnections();

    Map<Integer, Map<SrcDstPair, Path>> pathsWithinCluster =
        scheduler.findShortestPathIntra(withinClusterConnections);
    intraClusterCommunication = new IntraClusterCommunication(scheduler, dataCenterRHODA, pathsWithinCluster);

    intraClusterCommunication.rackToRackTx();

    for (int clusterId : pathsWithinCluster.keySet()) {
      Cluster cluster = dataCenterRHODA.getCluster(clusterId);
      for (int r = 0; r < cluster.getNumOfRacksInstalled(); r++) {
        RackRHODA rackRHODA = cluster.getRackBasedOnRackIdWithinCluster(r);
        Rack rack = rackRHODA.getRack();
        System.out.println(rack + " " + rackRHODA.getTotalTrafficIntraCluster());
        Assert.assertEquals(rackRHODA.getTotalTrafficIntraCluster(), 0.0);
      }
    }
  }
}
