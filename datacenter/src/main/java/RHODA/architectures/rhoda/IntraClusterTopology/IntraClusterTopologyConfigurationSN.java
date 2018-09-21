package RHODA.architectures.rhoda.IntraClusterTopology;

import RHODA.architectures.api.Flow;
import RHODA.architectures.api.Rack;
import RHODA.architectures.common.Configuration;
import RHODA.architectures.common.FlowComparator;
import RHODA.architectures.common.ShortestPathAmongNodes;
import RHODA.architectures.rhoda.Cluster;
import RHODA.architectures.rhoda.DataCenterRHODA;
import RHODA.architectures.rhoda.RackRHODA;
import com.google.common.annotations.VisibleForTesting;

import java.util.*;

public class IntraClusterTopologyConfigurationSN {
  private final DataCenterRHODA dataCenterRHODA;
  private final Map<Integer, Map<Integer, Set<Integer>>> withinClusterConnections;

  private final Map<ShuffleNetIndex, RackRHODA> shuffleNetIndexRackRHODAMap;

  public IntraClusterTopologyConfigurationSN(final DataCenterRHODA dataCenterRHODA) {
    this.dataCenterRHODA = dataCenterRHODA;
    withinClusterConnections = new HashMap<>();
    shuffleNetIndexRackRHODAMap = new HashMap<>();
  }

  public void buildConnections() {
    for (int c = 0; c < dataCenterRHODA.getNumOfClusters(); c++) {
      Cluster cluster = dataCenterRHODA.getCluster(c);
      RackRHODA rackRHODASelected = getRackRHODAWithLargestIntraTraffic(cluster);
      buildShuffleNet(rackRHODASelected);
      buildConnection(cluster);
    }
  }

  public RackRHODA getRackRHODAWithLargestIntraTraffic(final Cluster cluster) {
    RackRHODA result = null;
    double maxIntraTraffic = -1;
    for (RackRHODA rackRHODA : cluster.getRackList()) {
      if (rackRHODA.getTotalTrafficIntraCluster() > maxIntraTraffic) {
        maxIntraTraffic = rackRHODA.getTotalTrafficIntraCluster();
        result = rackRHODA;
      }
    }
    return result;
  }

  public void buildShuffleNet(final RackRHODA rackRHODASelected) {

    Set<ShuffleNetIndex> shuffleNetIndexSet = new HashSet<>();

    rackRHODASelected.setSNIndex(0, 0);
    LinkedList<RackRHODA> queue = new LinkedList<>();
    queue.add(rackRHODASelected);
    rackRHODASelected.setInstalledInCluster(true);

    ShuffleNetIndex shuffleNetIndex = new ShuffleNetIndex(0, 0);
    shuffleNetIndexRackRHODAMap.put(shuffleNetIndex, rackRHODASelected);
    shuffleNetIndexSet.add(shuffleNetIndex);

    int K = calculateK();
    int P = Configuration.getInstance().getNumOfTransceiversPerRackIntra();

    while (queue.size() != 0) {
      RackRHODA rackRHODA = queue.poll();

      int C = (int) (rackRHODA.getRowSNIndex() % Math.pow(P, K - 1)) * P;
      int D = (rackRHODA.getColumnSNIndex() + 1) % K;

      int v = 0;
      List<RackRHODA> list = getRackRHODAList(rackRHODA);
      for (RackRHODA rackRH : list) {
        shuffleNetIndex = new ShuffleNetIndex(C + v, D);
        while (shuffleNetIndexSet.contains(shuffleNetIndex)) {
          v++;

          shuffleNetIndex = new ShuffleNetIndex(C + v, D);
        }
        if (v > P - 1) {
          break;
        }
        rackRH.setSNIndex(C + v, D);
        rackRH.setInstalledInCluster(true);
        queue.add(rackRH);

        shuffleNetIndexRackRHODAMap.put(shuffleNetIndex, rackRH);
        shuffleNetIndexSet.add(shuffleNetIndex);

        v++;
      }
    }
  }

  public void buildConnection(final Cluster cluster) {
    Map<Integer, Set<Integer>> connection = new HashMap<>();

    int K = calculateK();
    int P = Configuration.getInstance().getNumOfTransceiversPerRackIntra();

    for (int a = 0; a <= Math.pow(P, K) - 1; a++) {
      for (int b = 0; b <= K - 1; b++) {
        ShuffleNetIndex siSrc = new ShuffleNetIndex(a, b);
        RackRHODA rackRHODASrc = shuffleNetIndexRackRHODAMap.get(siSrc);
        connection.put(rackRHODASrc.getRackIdWithinCluster(), new HashSet<>());
        int C = (int) (a % Math.pow(P, K - 1)) * P;
        int D = (b + 1) % K;
        for (int v = 0; v <= P - 1; v++) {
          ShuffleNetIndex siDst = new ShuffleNetIndex(C + v, D);
          RackRHODA rackRHODADst = shuffleNetIndexRackRHODAMap.get(siDst);
          connection.get(rackRHODASrc.getRackIdWithinCluster()).add(rackRHODADst.getRackIdWithinCluster());
        }
      }
    }
    withinClusterConnections.put(cluster.getClusterId(), connection);
  }

  public List<RackRHODA> getRackRHODAList(final RackRHODA rackRHODASelected) {
    List<RackRHODA> rackRHODAList = new ArrayList<>();

    Set<Integer> rackIdStoredToList = new HashSet<>();
    Iterator<Flow> flowIterator = rackRHODASelected.flowForIntraClusterTxIter();

    while (flowIterator.hasNext()) {
      Flow flow = flowIterator.next();
      int rackIdDst = flow.getNodeIdDst();
      RackRHODA rackRHODADst = dataCenterRHODA.getRackRHODABasedOnRackId(rackIdDst);
      if (!rackRHODADst.isInstalledInCluster()) {
        rackRHODAList.add(rackRHODADst);
        rackIdStoredToList.add(rackIdDst);
        if (rackRHODAList.size() == Configuration.getInstance().getNumOfTransceiversPerRackIntra()) {
          return rackRHODAList;
        }
      }
    }

    for (int r = 0; r < dataCenterRHODA.getNumOfRacks(); r++) {
      if (r == rackRHODASelected.getRack().getRackId() ||
          rackIdStoredToList.contains(r)) {
        continue;
      }
      RackRHODA rackRHODADst = dataCenterRHODA.getRackRHODABasedOnRackId(r);
      if (!rackRHODADst.isInstalledInCluster()) {
        rackRHODAList.add(rackRHODADst);
        if (rackRHODAList.size() == Configuration.getInstance().getNumOfTransceiversPerRackIntra()) {
          return rackRHODAList;
        }
      }
    }
    return rackRHODAList;
  }

  public int calculateK() {
    int k = 1;
    for (k = 1; k < Integer.MAX_VALUE; k++) {
      int P = Configuration.getInstance().getNumOfTransceiversPerRackIntra();
      int value = (int) (k * Math.pow(P, k));
      if (value >= Configuration.getInstance().getNumOfRacksPerCluster()) {
        break;
      }
    }
    return k;
  }

  public Map<Integer, Map<Integer, Set<Integer>>> getWithinClusterConnections() {
    return withinClusterConnections;
  }

  class ShuffleNetIndex {
    int rowIndex;
    int columnIndex;
    public ShuffleNetIndex(int rowIndex, int columnIndex) {
      this.rowIndex = rowIndex;
      this.columnIndex = columnIndex;
    }

    public int getRowIndex() {
      return rowIndex;
    }

    public int getColumnIndex() {
      return columnIndex;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ShuffleNetIndex that = (ShuffleNetIndex) o;
      return rowIndex == that.rowIndex &&
          columnIndex == that.columnIndex;
    }

    @Override
    public int hashCode() {
      return Objects.hash(rowIndex, columnIndex);
    }

    @Override
    public String toString() {
      return "ShuffleNetIndex{" +
          "rowIndex=" + rowIndex +
          ", columnIndex=" + columnIndex +
          '}';
    }
  }
}
