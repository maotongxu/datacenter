package RHODA.architectures.common;

import RHODA.architectures.api.*;

public class RoutingRack {

  public double startRouting(final DataCenter dataCenter,
                             final Path path,
                             final double bandwidthNeeded) {
    double bandwidthCanBeTx = getTrafficCanBeTx(dataCenter, path, bandwidthNeeded);
    route(dataCenter, path, bandwidthCanBeTx);
    return bandwidthCanBeTx;
  }

  private double getTrafficCanBeTx(final DataCenter dataCenter,
                                   final Path path,
                                   final double bandwidthNeeded) {
    double result = bandwidthNeeded;
    int numOfRacksInPath = path.pathNodeIdListSize();
    int rackIdSrc = path.getPathNodeId(0);
    for (int r = 1; r < numOfRacksInPath; r++) {
      int rackIdDst = path.getPathNodeId(r);

      Rack rackSrc = dataCenter.getRack(rackIdSrc);
      Rack rackDst = dataCenter.getRack(rackIdDst);

      SrcDstPair srcDstPair = new SrcDstPair(rackIdSrc, rackIdDst);
      Wavelength wavelength = rackSrc.getWavelengthOfRackPair(srcDstPair);

      double wavelengthCapacity = Configuration.getInstance().getWavelengthCapacity();
      if (wavelength == null) {
        int wavelengthId = findAvailableWavelengthId(rackSrc, rackDst);
        if (wavelengthId == -1) {
          // Cannot send any traffic between racks
          return 0.0;
        }
        // Do nothing
      } else if (wavelengthCapacity - wavelength.getBandwidthUsed() < bandwidthNeeded) {
        double availableBandwidth = wavelengthCapacity - wavelength.getBandwidthUsed();
        int wavelengthId = findAvailableWavelengthId(rackSrc, rackDst);
        if (wavelengthId == -1) {
          result = Math.min(result, availableBandwidth);
          continue;
        }
        // Do nothing
      } else {
        // Do nothing
      }
      rackIdSrc = rackIdDst;
    }
    return result;
  }

  private void route(final DataCenter dataCenter,
                     final Path path,
                     final double bandwidthCanBeTx) {
    int numOfRacksInPath = path.pathNodeIdListSize();
    int rackIdSrc = path.getPathNodeId(0);
    for (int r = 1; r < numOfRacksInPath; r++) {
      int rackIdDst = path.getPathNodeId(r);

      Rack rackSrc = dataCenter.getRack(rackIdSrc);
      Rack rackDst = dataCenter.getRack(rackIdDst);

      SrcDstPair srcDstPair = new SrcDstPair(rackIdSrc, rackIdDst);
      Wavelength wavelength = rackSrc.getWavelengthOfRackPair(srcDstPair);

      double wavelengthCapacity = Configuration.getInstance().getWavelengthCapacity();
      if (wavelength == null) {
        int wavelengthId = findAvailableWavelengthId(rackSrc, rackDst);
        assignWavelength(wavelengthId, bandwidthCanBeTx, rackSrc, rackDst);
      } else if (wavelengthCapacity - wavelength.getBandwidthUsed() < bandwidthCanBeTx) {
        double availableBandwidth = wavelengthCapacity - wavelength.getBandwidthUsed();
        double remainingNeeded = bandwidthCanBeTx - availableBandwidth;
        wavelength.setBandwidthUsed(wavelengthCapacity);

        int wavelengthId = findAvailableWavelengthId(rackSrc, rackDst);
        assignWavelength(wavelengthId, remainingNeeded, rackSrc, rackDst);
      } else {
        double wavelengthUsed = wavelength.getBandwidthUsed();
        wavelength.setBandwidthUsed(wavelengthUsed + bandwidthCanBeTx);
      }

      rackIdSrc = rackIdDst;
    }
  }

  public int findAvailableWavelengthId(final Rack rackSrc, final Rack rackDst) {
    int numOfWavelengths = Configuration.getInstance().getNumOfWavelengths();
    for (int w = 0; w < numOfWavelengths; w++) {
      if (rackSrc.getWavelengthTx(w) == null && rackDst.getWavelengthRx(w) == null) {
        return w;
      }
    }
    return -1;
  }

  public void assignWavelength(final int wavelengthId,
                               final double bandwidthAssigned,
                               final Rack rackSrc,
                               final Rack rackDst) {
    Wavelength wavelength = new Wavelength(wavelengthId,
        rackSrc.getRackId(), rackDst.getRackId());
    wavelength.setBandwidthUsed(bandwidthAssigned);
    rackSrc.addWavelengthTx(wavelength);
    rackDst.addWavelengthRx(wavelength);
  }
}
