package RHODA.architectures.api;

public class Flow {
  private final int nodeIdSrc;
  private final int nodeIdDst;
  private double traffic;

  public Flow(final int nodeIdSrc,
              final int nodeIdDst,
              final double traffic) {
    this.nodeIdSrc = nodeIdSrc;
    this.nodeIdDst = nodeIdDst;
    this.traffic = traffic;
  }

  public int getNodeIdSrc() {
    return nodeIdSrc;
  }

  public int getNodeIdDst() {
    return nodeIdDst;
  }

  public double getTraffic() {
    return traffic;
  }

  public void setTraffic(final double traffic) {
    this.traffic = traffic;
  }

  @Override
  public String toString() {
    return "FlowFT{" +
        "nodeIdSrc=" + nodeIdSrc +
        ", nodeIdDst=" + nodeIdDst +
        ", traffic=" + traffic +
        '}';
  }
}
