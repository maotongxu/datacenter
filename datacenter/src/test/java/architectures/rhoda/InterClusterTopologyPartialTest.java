package architectures.rhoda;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.api.Path;
import RHODA.architectures.api.SrcDstPair;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.rhoda.*;
import RHODA.architectures.rhoda.InterClusterTopology.InterClusterTopologyPartial;
import RHODA.architectures.rhoda.IntraClusterTopology.IntraClusterTopologyConfigurationSN;
import architectures.common.TrafficPattern;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;

public class InterClusterTopologyPartialTest {

  private DataCenterRHODA dataCenterRHODA;
  private Scheduler scheduler;
  private ClusterMembershipConfiguration clusterMembership;
  private IntraClusterTopologyConfigurationSN intraClusterTopologyConfiguration;
  private IntraClusterCommunication intraClusterCommunication;
  private InterClusterTopologyPartial interClusterTopologyPartial;

  @BeforeClass
  public void init() {
    Configuration.getInstance().setNumOfRacks(16);
    Configuration.getInstance().setMembershipRatio(1);
    Configuration.getInstance().setNumOfRacksPerCluster(4);
    Configuration.getInstance().setNumOfTransceiversPerRack(2);
    Configuration.getInstance().setOutDegreePerCluster(2);

    DataCenter dataCenter = new DataCenter();
    TrafficPattern trafficPattern = new TrafficPattern(dataCenter);
    trafficPattern.importTrafficToRacks();

    dataCenterRHODA = new DataCenterRHODA(dataCenter);
    scheduler = new Scheduler();

    clusterMembership = new ClusterMembershipConfiguration(dataCenterRHODA);
    clusterMembership.configureClusterMembership();

    intraClusterTopologyConfiguration = new IntraClusterTopologyConfigurationSN(dataCenterRHODA);
    intraClusterTopologyConfiguration.buildConnections();

    interClusterTopologyPartial = new InterClusterTopologyPartial(dataCenterRHODA);
  }

  @Test
  public void testRackToClusterHeadTx() {
    Map<Integer, Map<Integer, Set<Integer>>> withinClusterConnections =
        intraClusterTopologyConfiguration.getWithinClusterConnections();

    Map<Integer, Map<SrcDstPair, Path>> pathsWithinCluster =
        scheduler.findShortestPathIntra(withinClusterConnections);
    intraClusterCommunication =
        new IntraClusterCommunication(scheduler, dataCenterRHODA, pathsWithinCluster);

    interClusterTopologyPartial.buildInterClusterTopology();
    System.out.println(interClusterTopologyPartial.getInterClusterTopology());
  }
}
