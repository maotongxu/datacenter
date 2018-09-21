package RHODA.architectures.rhoda;

import RHODA.architectures.api.*;
import RHODA.architectures.common.Configuration;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

public class RoutingWithinCluster {
  public double startRouting(final Cluster cluster,
                             final Path path,
                             final double bandwidthNeeded) {
    double bandwidthCanBeTx = getTrafficCanBeTx(cluster, path, bandwidthNeeded);
    route(cluster, path, bandwidthCanBeTx);
    return bandwidthCanBeTx;
  }

  private double getTrafficCanBeTx(final Cluster cluster,
                                   final Path path,
                                   final double bandwidthNeeded) {
    double result = bandwidthNeeded;
    int numOfRacksInPath = path.pathNodeIdListSize();
    int rackIdWithinClusterSrc = path.getPathNodeId(0);
    for (int r = 1; r < numOfRacksInPath; r++) {
      int rackIdWithinClusterDst = path.getPathNodeId(r);

      RackRHODA rackRHODASrc = cluster.getRackBasedOnRackIdWithinCluster(rackIdWithinClusterSrc);
      RackRHODA rackRHODADst = cluster.getRackBasedOnRackIdWithinCluster(rackIdWithinClusterDst);

      Rack rackSrc = rackRHODASrc.getRack();
      Rack rackDst = rackRHODADst.getRack();

      /** Wavelengths are stored based on rackId, instead of rackIdWithinCluster*/
      SrcDstPair srcDstPair = new SrcDstPair(rackSrc.getRackId(), rackDst.getRackId());
      Wavelength wavelength = rackSrc.getWavelengthOfRackPair(srcDstPair);

      double wavelengthCapacity = Configuration.getInstance().getWavelengthCapacity();
      if (wavelength == null) {
        int wavelengthId = findAvailableWavelengthId(rackRHODASrc, rackRHODADst);
        if (wavelengthId == -1) {
          // Cannot send any traffic between racks
          return 0.0;
        }
        // Do nothing
      } else if (wavelengthCapacity - wavelength.getBandwidthUsed() < bandwidthNeeded) {
        double availableBandwidth = wavelengthCapacity - wavelength.getBandwidthUsed();
        int wavelengthId = findAvailableWavelengthId(rackRHODASrc, rackRHODADst);
        if (wavelengthId == -1) {
          result = Math.min(result, availableBandwidth);
          continue;
        }
        // Do nothing
      } else {
        // Do nothing
      }
      rackIdWithinClusterSrc = rackIdWithinClusterDst;
    }
    return result;
  }

  private void route(final Cluster cluster,
                     final Path path,
                     final double bandwidthCanBeTx) {
    int numOfRacksInPath = path.pathNodeIdListSize();
    int rackIdSrc = path.getPathNodeId(0);
    for (int r = 1; r < numOfRacksInPath; r++) {
      int rackIdDst = path.getPathNodeId(r);

      RackRHODA rackRHODASrc = cluster.getRackBasedOnRackIdWithinCluster(rackIdSrc);
      RackRHODA rackRHODADst = cluster.getRackBasedOnRackIdWithinCluster(rackIdDst);

      Rack rackSrc = rackRHODASrc.getRack();
      Rack rackDst = rackRHODADst.getRack();

      SrcDstPair srcDstPair = new SrcDstPair(rackSrc.getRackId(), rackDst.getRackId());
      Wavelength wavelength = rackSrc.getWavelengthOfRackPair(srcDstPair);
      double wavelengthCapacity = Configuration.getInstance().getWavelengthCapacity();
      if (wavelength == null) {
        int wavelengthId = findAvailableWavelengthId(rackRHODASrc, rackRHODADst);
        assignWavelength(wavelengthId, bandwidthCanBeTx, rackSrc, rackDst);
      } else if (wavelengthCapacity - wavelength.getBandwidthUsed() < bandwidthCanBeTx) {
        double availableBandwidth = wavelengthCapacity - wavelength.getBandwidthUsed();
        double remainingNeeded = bandwidthCanBeTx - availableBandwidth;
        wavelength.setBandwidthUsed(wavelengthCapacity);

        int wavelengthId = findAvailableWavelengthId(rackRHODASrc, rackRHODADst);
        assignWavelength(wavelengthId, remainingNeeded, rackSrc, rackDst);
      } else {
        double wavelengthUsed = wavelength.getBandwidthUsed();
        wavelength.setBandwidthUsed(wavelengthUsed + bandwidthCanBeTx);
      }

      rackIdSrc = rackIdDst;
    }
  }

  private int findAvailableWavelengthId(final RackRHODA rackRHODASrc,
                                        final RackRHODA rackRHODADst) {

    /**To get a list of available wavelengths, we need to use rackIdWithinCluster*/
    int rackIdSrcWithinCluster = rackRHODASrc.getRackIdWithinCluster();
    int rackIdDstWithinCluster = rackRHODADst.getRackIdWithinCluster();

    Rack rackSrc = rackRHODASrc.getRack();
    Rack rackDst = rackRHODADst.getRack();

    List<Integer> wavelengthList = getAvailableWavelengths(
        rackIdSrcWithinCluster, rackIdDstWithinCluster);
    for (int w : wavelengthList) {
      if (rackSrc.getWavelengthTx(w) == null && rackDst.getWavelengthRx(w) == null) {
        return w;
      }
    }
    int id = wavelengthList.get(0);
    System.out.println(rackSrc.getWavelengthTx(id) + " === " + rackDst.getWavelengthRx(id) + " " + rackSrc.getRackId() + " " + rackDst.getRackId());
    return -1;
  }

  @VisibleForTesting
  public static List<Integer> getAvailableWavelengths(final int rackIdSrcWithinCluster,
                                                final int rackIdDstWithinCluster) {
    int totalNumOfWavelengths = Configuration.getInstance().getNumOfWavelengths();

    List<Integer> result = new ArrayList<>();
    int w = baseWavelengthIndex(rackIdSrcWithinCluster, rackIdDstWithinCluster);
    while (w < totalNumOfWavelengths) {
      result.add(w);
      w += Configuration.getInstance().getNumOfRacksPerCluster();
    }
    return result;
  }

  @VisibleForTesting
  public static int baseWavelengthIndex(final int rackIdSrcWithinCluster,
                                  final int rackIdDstWithinCluster) {
    if (rackIdSrcWithinCluster <= rackIdDstWithinCluster) {
      return (rackIdDstWithinCluster - rackIdSrcWithinCluster);
    } else {
      return (Configuration.getInstance().getNumOfRacksPerCluster() -
          rackIdSrcWithinCluster) + rackIdDstWithinCluster - 1;
    }
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
