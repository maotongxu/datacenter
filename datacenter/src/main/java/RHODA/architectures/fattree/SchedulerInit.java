package RHODA.architectures.fattree;

public class SchedulerInit {

  public static void initScheduler() {
    Scheduler.sw_core = new Switch[ConfigurationFT.NUM_OF_CORE_GROUP][ConfigurationFT.NUM_OF_CORE_SWITCH_PER_GROUP];
    Scheduler.sw_agg = new Switch[ConfigurationFT.NUM_OF_POD][ConfigurationFT.NUM_OF_AGG_SWITCH_PER_POD];
    Scheduler.sw_edge = new Switch[ConfigurationFT.NUM_OF_POD][ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD];
    Scheduler.host = new Host[ConfigurationFT.NUM_OF_POD][ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD][ConfigurationFT.NUM_OF_HOST_PER_EDGE_SWITCH];
  }

  public static void initSwitch() {
    for (int i = 0; i < ConfigurationFT.NUM_OF_CORE_GROUP; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_CORE_SWITCH_PER_GROUP; j++) {
        Scheduler.sw_core[i][j] = new Switch();
        Scheduler.sw_core[i][j].setSwitchType(Switch.CORE_SWITCH);
        Scheduler.sw_core[i][j].setSwitchId1(i);
        Scheduler.sw_core[i][j].setSwitchId2(j);
      }
    }

    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_AGG_SWITCH_PER_POD; j++) {
        Scheduler.sw_agg[i][j] = new Switch();
        Scheduler.sw_agg[i][j].setSwitchType(Switch.AGG_SWITCH);
        Scheduler.sw_agg[i][j].setSwitchId1(i);
        Scheduler.sw_agg[i][j].setSwitchId2(j);
      }
    }

    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD; j++) {
        Scheduler.sw_edge[i][j] = new Switch();
        Scheduler.sw_edge[i][j].setSwitchType(Switch.EDGE_SWITCH);
        Scheduler.sw_edge[i][j].setSwitchId1(i);
        Scheduler.sw_edge[i][j].setSwitchId2(j);
      }
    }

    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD; j++) {
        for (int z = 0; z < ConfigurationFT.NUM_OF_HOST_PER_EDGE_SWITCH; z++) {
          Scheduler.host[i][j][z] = new Host();
        }
      }
    }
  }

  public static void assignIP() {
    System.out.println(Scheduler.sw_core.length + " " + Scheduler.sw_core[0].length);
    for (int j = 1; j <= ConfigurationFT.NUM_OF_CORE_GROUP; j++) {			// j-1 is pod index
      for (int i = 1; i <= ConfigurationFT.NUM_OF_CORE_SWITCH_PER_GROUP; i++) {		// i-1 is core index in pod j
        Scheduler.sw_core[j-1][i-1].getAddress().setAddress1(10);
        Scheduler.sw_core[j-1][i-1].getAddress().setAddress2(ConfigurationFT.K);
        Scheduler.sw_core[j-1][i-1].getAddress().setAddress3(j);
        Scheduler.sw_core[j-1][i-1].getAddress().setAddress4(i);
      }
    }

    for (int x = 0; x <= ConfigurationFT.K - 1; x++) {
      for (int z = ConfigurationFT.K / 2; z <= ConfigurationFT.K - 1; z++) {
        for (int i = 0; i <= ConfigurationFT.K / 2 - 1; i++) {
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].getAddress().setAddress1(10);
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].getAddress().setAddress2(x);
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].getAddress().setAddress3(z);
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].getAddress().setAddress4(1);
        }
        for (int i = 2; i <= ConfigurationFT.K / 2 + 1; i++) {
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].getAddress().setAddress1(10);
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].getAddress().setAddress2(x);
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].getAddress().setAddress3(z);
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].getAddress().setAddress4(1);
          int portIndex = (i - 2 + z) % (ConfigurationFT.K / 2) + (ConfigurationFT.K / 2);
          Scheduler.sw_agg[x][z - ConfigurationFT.K / 2].setSurPort(portIndex);
        }
      }
    }

    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD; j++) {
        Scheduler.sw_edge[i][j].getAddress().setAddress1(10);
        Scheduler.sw_edge[i][j].getAddress().setAddress2(i);
        Scheduler.sw_edge[i][j].getAddress().setAddress3(j);
        Scheduler.sw_edge[i][j].getAddress().setAddress4(1);
      }
    }

    for (int i = 0; i < ConfigurationFT.NUM_OF_POD; i++) {
      for (int j = 0; j < ConfigurationFT.NUM_OF_EDGE_SWITCH_PER_POD; j++) {
        for (int z = 0; z < ConfigurationFT.NUM_OF_HOST_PER_EDGE_SWITCH; z++) {
          Scheduler.host[i][j][z].getAddress().setAddress1(10);
          Scheduler.host[i][j][z].getAddress().setAddress2(i);
          Scheduler.host[i][j][z].getAddress().setAddress3(j);
          Scheduler.host[i][j][z].getAddress().setAddress4(z + 2);
        }
      }
    }
  }
}
