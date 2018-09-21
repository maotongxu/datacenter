package RHODA.architectures.wavecube;

import RHODA.architectures.api.DataCenter;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.TrafficPattern;
import RHODA.architectures.output.Metrics;

public class Scheduler {

  public void start() {
    int numOfRacks = calculateNumOfRacks(Configuration.getInstance().getNumOfRacks());
    Configuration.getInstance().setNumOfRacks(numOfRacks);

    DataCenter dataCenter = new DataCenter();
    TrafficPattern trafficPattern = new TrafficPattern(dataCenter);
    trafficPattern.importTraffic();

    InterRackTx interRackTx = new InterRackTx(dataCenter);

    interRackTx.interRackTx();
  }

  private int calculateNumOfRacks(final int minNumOfRacks) {
    int numOfRacks = 0;
    for (int i = 0; i <= 20; i++) {
      if (Math.pow(2, i) >= minNumOfRacks) {
        numOfRacks = (int) Math.pow(2, i);
        break;
      }
    }
    return numOfRacks;
  }

  public static void main(String[] args) {
    Metrics.init();
    Scheduler scheduler = new Scheduler();
    scheduler.start();

    System.out.println("NumOfHops " + Metrics.calculateAvgNumOfHops() +
        " AvgRackLoad " + Metrics.calculateAvgRackLoad() +
        " TotalTraffic " + Metrics.calculateTotalTraffic());
  }
}
