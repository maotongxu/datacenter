package RHODA.architectures.wavecube;

import RHODA.architectures.api.*;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.output.Metrics;

import java.util.HashMap;
import java.util.Map;

public class InterRackTx {
  private DataCenter dataCenter;
  private Map<SrcDstPair, Integer> rackIdPairWIdMap;

  public InterRackTx(final DataCenter dataCenter) {
    this.dataCenter = dataCenter;
    rackIdPairWIdMap = new HashMap<>();
  }

  public void interRackTx() {
    for (int srcId = 0; srcId < dataCenter.getNumOfRacks(); srcId++) {
      Rack rackSrc = dataCenter.getRack(srcId);
      for (Flow flow : rackSrc.getFlowsWaitingForTx()) {
        rackIdPairWIdMap.clear();

        int intermSrc = srcId;
        int dstId = flow.getNodeIdDst();
        double feasibleTraffic = flow.getTraffic();

        while (intermSrc != dstId) {
          int intermDst = findNextDst(intermSrc, dstId);
          SrcDstPair srcDstPair = new SrcDstPair(intermSrc, intermDst);
          Wavelength wavelength = dataCenter.getRack(intermSrc).getWavelengthOfRackPair(srcDstPair);

          if (wavelength == null) {
            wavelength = findCommonAvailableWavelength(dataCenter.getRack(intermSrc), dataCenter.getRack(intermDst));
            if (wavelength== null) {
              feasibleTraffic = 0.0;
              break;
            }
            dataCenter.getRack(intermSrc).addWavelengthTx(wavelength);
            dataCenter.getRack(intermDst).addWavelengthRx(wavelength);
          }

          rackIdPairWIdMap.put(srcDstPair, wavelength.getWavelengthId());

          feasibleTraffic = Math.min(feasibleTraffic,
                  (wavelength.getWavelengthCapacity() - wavelength.getBandwidthUsed()));
          intermSrc = intermDst;
        }

        Path path = new Path(srcId, dstId);

        intermSrc = srcId;
        while (feasibleTraffic > 0 && intermSrc != dstId) {
          path.addPathNodeId(intermSrc);
          int intermDst = findNextDst(intermSrc, dstId);
          SrcDstPair srcDstPair = new SrcDstPair(intermSrc, intermDst);
          int wavelengthId = rackIdPairWIdMap.get(srcDstPair);
          Wavelength wavelengthTx = dataCenter.getRack(intermSrc).getWavelengthTx(wavelengthId);
          wavelengthTx.setBandwidthUsed(wavelengthTx.getBandwidthUsed() + feasibleTraffic);
          Wavelength wavelengthRx = dataCenter.getRack(intermDst).getWavelengthRx(wavelengthId);
          wavelengthRx.setBandwidthUsed(wavelengthRx.getBandwidthUsed() + feasibleTraffic);
          intermSrc = intermDst;
        }

        /**Add info to metrics storage */
        path.addPathNodeId(dstId);
        SrcDstPair srcDstPair = new SrcDstPair(srcId, dstId);
        System.out.println(srcDstPair + " " + path.pathNodeIdListSize() + " " + feasibleTraffic);
        Metrics.addToOuptut(srcDstPair, path, feasibleTraffic);
      }
    }
  }

  private Wavelength findCommonAvailableWavelength(final Rack rackSrc, final Rack rackDst) {
    int numOfWavelengths = Configuration.getInstance().getNumOfWavelengths();
    for (int w = 0; w < numOfWavelengths; w++) {
      if (rackSrc.getWavelengthTx(w) == null && rackDst.getWavelengthRx(w) == null) {
        Wavelength wavelength = new Wavelength(w, rackSrc.getRackId(), rackDst.getRackId());
        return wavelength;
      }
    }
    return null;
  }

  private int findNextDst(int src, int dst) {
    int tmp = src ^ dst;
    src = leastSignificantOne(tmp) ^ src;
    return src;
  }

  private int leastSignificantOne(int value) {
    int result = 0;
    for (int i=0; i<20; i++) {
      int tmp = (value>>i)&1;
      if (tmp>0) {
        result = i;
        break;
      }
    }
    return (int) Math.pow(2, result);
  }
}
