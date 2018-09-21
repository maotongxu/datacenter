package architectures.fattree;

import RHODA.architectures.common.Configuration;
import RHODA.architectures.fattree.ConfigurationFT;
import RHODA.architectures.fattree.Routing;
import RHODA.architectures.fattree.Scheduler;
import RHODA.architectures.fattree.SchedulerInit;
import RHODA.architectures.output.Metrics;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

public class FatTreeTest {
  @Test
  public void testFatTreeSteps() {
    ConfigurationFT.setConfBasedOnMinNumOfRacks(16);
    Configuration.getInstance().setNumOfRacks(ConfigurationFT.TOTAL_NUM_OF_RACKS);

    SchedulerInit.initScheduler();
    SchedulerInit.initSwitch();
    SchedulerInit.assignIP();

    Routing.loadTrafficToHosts(importTestTraffic());
    System.out.println(Scheduler.host[0][1][0].getBufferedFlows() + " Address " + Scheduler.host[0][1][0].getAddress());
    Routing.txFlowHost();
    System.out.println(Scheduler.sw_edge[0][1].getBufferedFlows() + " Address " + Scheduler.sw_edge[0][1].getAddress());
    Routing.txFlowSw_Edge();
    System.out.println(Scheduler.sw_agg[0][0].getBufferedFlows() + " Address " + Scheduler.sw_agg[0][0].getAddress());
    Routing.txFlowSw_Agg();
    System.out.println(Scheduler.sw_core[0][1].getBufferedFlows() + " Address " + Scheduler.sw_core[0][1].getAddress());
    Routing.txFlowSw_Core();
    System.out.println(Scheduler.sw_agg[2][0].getBufferedFlows() + " Address " + Scheduler.sw_agg[2][0].getAddress());
    Routing.txFlowSw_Agg();
    System.out.println(Scheduler.sw_edge[2][0].getBufferedFlows() + " Address " + Scheduler.sw_edge[2][0].getAddress());
    Routing.txFlowSw_Edge();
    System.out.println(Scheduler.sw_edge[2][0].getBufferedFlows() + " Address " + Scheduler.sw_edge[2][0].getAddress());

    Scheduler.outputSwitchLoad();
    System.out.println(Metrics.rackIdRackLoadMap);
    System.out.println("NumOfHops " + Metrics.calculateAvgNumOfHops() +
        " AvgRackLoad " + Metrics.calculateAvgRackLoad() +
        " TotalTraffic " + Metrics.calculateTotalTraffic());
  }

  @Test
  public void testFatTree() {
    ConfigurationFT.setConfBasedOnMinNumOfRacks(16);
    Configuration.getInstance().setNumOfRacks(ConfigurationFT.TOTAL_NUM_OF_RACKS);

    SchedulerInit.initScheduler();
    SchedulerInit.initSwitch();
    SchedulerInit.assignIP();

    Routing.loadTrafficToHosts(importTestTraffic());
    Routing.txPktUp();
    Routing.txPktDown();
    Scheduler.outputSwitchLoad();
    System.out.println(Metrics.rackIdRackLoadMap);
    System.out.println("NumOfHops " + Metrics.calculateAvgNumOfHops() +
        " AvgRackLoad " + Metrics.calculateAvgRackLoad() +
        " TotalTraffic " + Metrics.calculateTotalTraffic());
  }

  public double[][] importTestTraffic() {
    int numOfRacks = ConfigurationFT.TOTAL_NUM_OF_RACKS;
    double[][] flowMatrix = new double[numOfRacks][numOfRacks];
    for (int i = 0; i < numOfRacks; i++) {
      for (int j = 0; j < numOfRacks; j++) {
        flowMatrix[i][j] = 1;
      }
    }
    return flowMatrix;
  }
}
