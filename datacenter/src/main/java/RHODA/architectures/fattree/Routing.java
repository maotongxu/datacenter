package RHODA.architectures.fattree;

public class Routing {

  public static void loadTrafficToHosts(double[][] trafficMatrix) {
    int numOfRacks = ConfigurationFT.TOTAL_NUM_OF_RACKS;
    System.out.println("numOfRacks " + numOfRacks);
    for (int rackIdSrc = 0; rackIdSrc < numOfRacks; rackIdSrc++) {
      for (int rackIdDst = 0; rackIdDst < numOfRacks; rackIdDst++) {
        if (rackIdSrc == rackIdDst || trafficMatrix[rackIdSrc][rackIdDst] <= Double.MIN_VALUE) {
          continue;
        }

        int podIdSrc = (int) (rackIdSrc / Math.ceil((double) numOfRacks / ConfigurationFT.NUM_OF_POD));
        int edgeIdSrc = (int) ((rackIdSrc % Math.ceil((double) numOfRacks / ConfigurationFT.NUM_OF_POD)) /
            Math.ceil(Math.ceil((double) numOfRacks / ConfigurationFT.NUM_OF_POD) / ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD));
        int hostIdSrc = (int) (rackIdSrc % Math.ceil(Math.ceil((double) numOfRacks / ConfigurationFT.NUM_OF_POD) / ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD));

        int podIdDst = (int) (rackIdDst / Math.ceil((double) numOfRacks / ConfigurationFT.NUM_OF_POD));
        int edgeIdDst = (int) ((rackIdDst % Math.ceil((double) numOfRacks / ConfigurationFT.NUM_OF_POD)) /
            Math.ceil(Math.ceil((double) numOfRacks / ConfigurationFT.NUM_OF_POD) / ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD));
        int hostIdDst = (int) (rackIdDst % Math.ceil(Math.ceil((double) numOfRacks / ConfigurationFT.NUM_OF_POD) / ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD));

        FlowFT flowFT = new FlowFT(rackIdSrc, rackIdDst);
        Address addressDstHost = new Address(
            Scheduler.host[podIdDst][edgeIdDst][hostIdDst].getAddress().getAddress1(),
            Scheduler.host[podIdDst][edgeIdDst][hostIdDst].getAddress().getAddress2(),
            Scheduler.host[podIdDst][edgeIdDst][hostIdDst].getAddress().getAddress3(),
            Scheduler.host[podIdDst][edgeIdDst][hostIdDst].getAddress().getAddress4());
        flowFT.setAddressDst(addressDstHost);
        flowFT.setTrafficUnit(trafficMatrix[rackIdSrc][rackIdDst]);
        flowFT.setNumOfHops(0);

        Scheduler.host[podIdSrc][edgeIdSrc][hostIdSrc].addFlow(flowFT);
      }
    }
  }

  public static void txPktUp() {
    txFlowHost();

    txFlowSw_Edge();

    txFlowSw_Agg();
  }

  public static void txPktDown() {
    txFlowSw_Core();

    txFlowSw_Agg();

    txFlowSw_Edge();
  }

  public static void txFlowHost() {
    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD; j++) {
        for (int z = 0; z < ConfigurationFT.NUM_OF_HOST_PER_EDGE_SWITCH; z++) {
          Scheduler.host[i][j][z].txFlow();
        }
      }
    }
  }

  public static void txFlowSw_Edge() {
    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD; j++) {
        Scheduler.sw_edge[i][j].txFlow();
      }
    }
  }

  public static void txFlowSw_Agg() {
    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_AGG_SWITCH_PER_POD; j++) {
        Scheduler.sw_agg[i][j].txFlow();
      }
    }
  }

  public static void txFlowSw_Core() {
    for (int i = 0; i < ConfigurationFT.NUM_OF_CORE_GROUP; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_CORE_SWITCH_PER_GROUP; j++) {
        Scheduler.sw_core[i][j].txFlow();
      }
    }
  }
}
