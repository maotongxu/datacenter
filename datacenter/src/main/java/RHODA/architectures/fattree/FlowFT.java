package RHODA.architectures.fattree;

import RHODA.architectures.api.SrcDstPair;

import java.util.Objects;

public class FlowFT {
  private final int srcId;
  private final int dstId;

  private Address addressDst;

  private int portIndex;

  private double trafficUnit;
  private int numOfHops;

  public FlowFT(final int srcId, final int dstId) {
    this.srcId = srcId;
    this.dstId = dstId;
  }

  public Address getAddressDst() {
    return addressDst;
  }

  public void setAddressDst(Address addressDst) {
    this.addressDst = addressDst;
  }

  public int getPortIndex() {
    return portIndex;
  }

  public void setPortIndex(int portIndex) {
    this.portIndex = portIndex;
  }

  public double getTrafficUnit() {
    return trafficUnit;
  }

  public void setTrafficUnit(double trafficUnit) {
    this.trafficUnit = trafficUnit;
  }

  public int getNumOfHops() {
    return numOfHops;
  }

  public void setNumOfHops(int numOfHops) {
    this.numOfHops = numOfHops;
  }

  public void incrNumOfHops() {
    numOfHops++;
  }

  public SrcDstPair getSrcDstPair() {
    return new SrcDstPair(srcId, dstId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FlowFT flowFT = (FlowFT) o;
    return srcId == flowFT.srcId &&
        dstId == flowFT.dstId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(srcId, dstId);
  }

  @Override
  public String toString() {
    return "FlowFT{" +
        "srcId=" + srcId +
        ", dstId=" + dstId +
        ", trafficUnit=" + trafficUnit +
        '}';
  }
}
