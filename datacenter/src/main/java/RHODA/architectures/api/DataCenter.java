package RHODA.architectures.api;

import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.TrafficPattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataCenter {

  private final int numOfRacks;
  private final int numOfWavelengths;
  private final List<Rack> rackList;

  public DataCenter() {
    this.numOfRacks = Configuration.getInstance().getNumOfRacks();
    this.numOfWavelengths = Configuration.getInstance().getNumOfWavelengths();
    rackList = new ArrayList<>(numOfRacks);
    addRackToDataCenter();
  }

  private void addRackToDataCenter() {
    for (int r = 0; r < numOfRacks; r++) {
      Rack rack = new Rack(r);
      rackList.add(rack);
    }
  }

  public int getNumOfRacks() {
    return numOfRacks;
  }

  public int getNumOfWavelengths() {
    return numOfWavelengths;
  }

  public void addRack(final Rack rack) {
    rackList.add(rack);
  }

  public Rack getRack(final int rackId) {
    return rackList.get(rackId);
  }

  public Iterator<Rack> getRackListIterator() {
    return rackList.iterator();
  }
}
