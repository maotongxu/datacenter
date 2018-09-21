package RHODA.architectures.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import RHODA.architectures.common.Configuration;
import com.google.common.collect.ImmutableSet;

public class Rack {
  private final int rackId;
  private final Set<Flow> flowsWaitingForTx;
  private final Map<SrcDstPair, Wavelength> rackPairWavelength;
  private Wavelength[] wavelengthTxArray;
  private Wavelength[] wavelengthRxArray;

  private int numOfDstConnected;
  private int numOfSrcConnected;

  private double totalTraffic;

  public Rack(final int rackId) {
    this.rackId = rackId;
    flowsWaitingForTx = new HashSet<Flow>();
    rackPairWavelength = new HashMap<>();
    int numOfWavelength = Configuration.getInstance().getNumOfWavelengths();
    wavelengthTxArray = new Wavelength[numOfWavelength];
    wavelengthRxArray = new Wavelength[numOfWavelength];
  }

  public void addFlow(final Flow flow) {
    flowsWaitingForTx.add(flow);
    totalTraffic += flow.getTraffic();
  }

  public void removeFlow(final Flow flow) {
    flowsWaitingForTx.remove(flow);
    totalTraffic -= flow.getTraffic();
  }

  public Set<Flow> getFlowsWaitingForTx() {
    return flowsWaitingForTx;
  }

  public int getRackId() {
    return rackId;
  }

  public Wavelength getWavelengthTx(final int wavelengthId) {
    return wavelengthTxArray[wavelengthId];
  }

  public Wavelength getWavelengthRx(final int wavelengthId) {
    return wavelengthRxArray[wavelengthId];
  }

  public void addWavelengthTx(final Wavelength w) {
    wavelengthTxArray[w.getWavelengthId()] = w;
    SrcDstPair srcDstPair = new SrcDstPair(w.getSrc(), w.getDst());
    rackPairWavelength.put(srcDstPair, w);
  }

  public void addWavelengthRx(final Wavelength w) {
    wavelengthRxArray[w.getWavelengthId()] = w;
  }

  public Wavelength getWavelengthOfRackPair(SrcDstPair srcDstPair) {
    return rackPairWavelength.get(srcDstPair);
  }

  public int getNumOfDstConnected() {
    return numOfDstConnected;
  }

  public void incrNumOfDstConnected() {
    numOfDstConnected++;
  }

  public void setNumOfDstConnected(int numOfDstConnected) {
    this.numOfDstConnected = numOfDstConnected;
  }

  public int getNumOfSrcConnected() {
    return numOfSrcConnected;
  }

  public void incrNumOfSrcConnected() {
    numOfSrcConnected++;
  }

  public void setNumOfSrcConnected(int numOfSrcConnected) {
    this.numOfSrcConnected = numOfSrcConnected;
  }

  public double getTotalTraffic() {
    return totalTraffic;
  }

  public void setTotalTraffic(double totalTraffic) {
    this.totalTraffic = totalTraffic;
  }

  @Override
  public String toString() {
    return "Rack{" +
        "rackId=" + rackId +
        '}';
  }
}
