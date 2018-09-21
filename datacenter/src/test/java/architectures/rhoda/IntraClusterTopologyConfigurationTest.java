package architectures.rhoda;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.rhoda.*;
import RHODA.architectures.api.Flow;

import RHODA.architectures.rhoda.IntraClusterTopology.IntraClusterTopologyConfigurationSN;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

public class IntraClusterTopologyConfigurationTest {
  private DataCenter dataCenter;
  private DataCenterRHODA dataCenterRHODA;
  private ClusterMembershipConfiguration clusterMembership;
  private IntraClusterTopologyConfigurationSN intraClusterTopologyConfiguration;

  @BeforeClass
  public void init() {
    Configuration.getInstance().setNumOfRacks(24);
    Configuration.getInstance().setMembershipRatio(24);
    Configuration.getInstance().setNumOfRacksPerCluster(24);

    dataCenter = new DataCenter();
    Flow flow = new Flow(0, 1, 1);
    dataCenter.getRack(0).addFlow(flow);

    dataCenterRHODA = new DataCenterRHODA(dataCenter);
    clusterMembership = new ClusterMembershipConfiguration(dataCenterRHODA);
    clusterMembership.configureClusterMembership();
    intraClusterTopologyConfiguration = new IntraClusterTopologyConfigurationSN(dataCenterRHODA);
  }

  @Test
  public void testSimple() {
    Configuration.getInstance().setNumOfTransceiversPerRackIntra(2);
    Configuration.getInstance().setNumOfRacksPerCluster(64);
    int K = intraClusterTopologyConfiguration.calculateK();
    Assert.assertEquals(K, 4);
  }

  @Test
  public void calculateHops() {
    int k = 4;
    int p = 2;
    int num = (int) (k * Math.pow(p, k) * (p - 1) * (3 * k - 1) - 2 * k * (Math.pow(p, k) - 1));
    int den = (int) (2 * (p - 1) * (k * Math.pow(p, k) - 1));
    System.out.println(num / den);
  }

  @Test
  public void test() {

    intraClusterTopologyConfiguration.buildConnections();

  }
}
