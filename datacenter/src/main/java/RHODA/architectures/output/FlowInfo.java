package RHODA.architectures.output;

public class FlowInfo {
  private double traffic;
  private int numOfHops;

  public FlowInfo(final double traffic,
                  final int numOfHops) {
    this.traffic = traffic;
    this.numOfHops = numOfHops;
  }

  public double getTraffic() {
    return traffic;
  }

  public void setTraffic(double traffic) {
    this.traffic = traffic;
  }

  public int getNumOfHops() {
    return numOfHops;
  }

  public void setNumOfHops(int numOfHops) {
    this.numOfHops = numOfHops;
  }

  @Override
  public String toString() {
    return "FlowInfo{" +
        "traffic=" + traffic +
        ", numOfHops=" + numOfHops +
        '}';
  }
}
