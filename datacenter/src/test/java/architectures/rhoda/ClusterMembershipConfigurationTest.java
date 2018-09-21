package architectures.rhoda;

import RHODA.architectures.rhoda.Cluster;
import RHODA.architectures.api.DataCenter;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.rhoda.ClusterMembershipConfiguration;
import RHODA.architectures.rhoda.DataCenterRHODA;

import RHODA.architectures.rhoda.RackRHODA;
import architectures.common.TrafficPattern;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

public class ClusterMembershipConfigurationTest {
  private DataCenterRHODA dataCenterRHODA;
  private ClusterMembershipConfiguration clusterMembership;

  @BeforeClass
  public void init() {
    Configuration.getInstance().setNumOfRacks(18);
    Configuration.getInstance().setMembershipRatio(3);
    Configuration.getInstance().setNumOfRacksPerCluster(6);

    DataCenter dataCenter = new DataCenter();
    TrafficPattern trafficPattern = new TrafficPattern(dataCenter);
    trafficPattern.importTrafficToRacks();

    dataCenterRHODA = new DataCenterRHODA(dataCenter);
    clusterMembership = new ClusterMembershipConfiguration(dataCenterRHODA);
  }

  @Test
  public void testClusterMembershipConfiguration() {
    clusterMembership.configureClusterMembership();

    int[] numOfRacksFromMSwitch = new int[dataCenterRHODA.getNumOfMSwitches()];
    for (int c = 0 ; c < dataCenterRHODA.getNumOfClusters(); c++) {
      Cluster cluster = dataCenterRHODA.getCluster(c);
      List<RackRHODA> rackRHODAList = cluster.getRackList();
      System.out.println(rackRHODAList);
      for (RackRHODA rackRHODA : rackRHODAList) {
        int mSwitchOfRack = rackRHODA.getRack().getRackId() / dataCenterRHODA.getNumOfRacksPerMSwitch();
        numOfRacksFromMSwitch[mSwitchOfRack] ++;
      }
    }

    for (int i = 0; i < dataCenterRHODA.getNumOfMSwitches(); i++) {
      Assert.assertEquals(numOfRacksFromMSwitch[i], dataCenterRHODA.getNumOfRacksPerMSwitch());
    }
  }
}
