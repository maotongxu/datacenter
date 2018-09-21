package RHODA.architectures.rhoda.InterClusterTopology;

import RHODA.architectures.api.Rack;
import RHODA.architectures.api.Flow;

import RHODA.architectures.common.Configuration;
import RHODA.architectures.rhoda.DataCenterRHODA;
import RHODA.architectures.rhoda.Cluster;
import RHODA.architectures.rhoda.InterClusterTopology.wbm.HungarianAlgorithm;
import RHODA.architectures.rhoda.RackRHODA;

import java.util.*;

public class InterClusterTopologyPartial {
  private final DataCenterRHODA dataCenterRHODA;
  private final Map<Integer, Set<Integer>> connections;

  public InterClusterTopologyPartial(final DataCenterRHODA dataCenterRHODA) {
    this.dataCenterRHODA = dataCenterRHODA;
    connections = new HashMap<>();
    for (int i = 0; i < dataCenterRHODA.getNumOfClusters(); i++) {
      connections.put(i, new HashSet<>());
    }
  }

  public void buildInterClusterTopology() {
    connectClustersBasedOnTraffic();
  }

  public Map<Integer, Set<Integer>> getInterClusterTopology() {
    return connections;
  }

  private void connectClustersBasedOnTraffic() {
    double[][] weights = calculateWeights();
    double[][] costs = convertWeightsToCosts(weights);
    applyWBM(costs);
  }

  private double[][] calculateWeights() {
    int numOfClusters = dataCenterRHODA.getNumOfClusters();
    double[][] weights = new double[numOfClusters][numOfClusters];

    for (int clusterIdSrc = 0; clusterIdSrc < numOfClusters; clusterIdSrc++) {
      Cluster clusterSrc = dataCenterRHODA.getCluster(clusterIdSrc);
      for (int r = 0; r < clusterSrc.getNumOfRacksInstalled(); r++) {
        RackRHODA rackRHODA = clusterSrc.getRackBasedOnRackIdWithinCluster(r);
        Rack rack = rackRHODA.getRack();
        Set<Flow> flowSet = rack.getFlowsWaitingForTx();

        for (Flow flow : flowSet) {
          int rackIdDst = flow.getNodeIdDst();
          int clusterIdDst = dataCenterRHODA.getClusterIdFromRackId(rackIdDst);
          if (clusterIdSrc == clusterIdDst) {
            continue;
          }
          weights[clusterIdSrc][clusterIdDst] += flow.getTraffic();
        }
      }
    }
    return weights;
  }

  private double[][] convertWeightsToCosts(double[][] weights) {
    int numOfClusters = dataCenterRHODA.getNumOfClusters();
    double[][] costs = new double[numOfClusters][numOfClusters];

    for (int i = 0; i < numOfClusters; i++) {
      for (int j = 0; j < numOfClusters; j++) {
        if (i == j) {
          costs[i][j] = 1000;
        } else {
          costs[i][j] = -weights[i][j];
        }
      }
    }
    return costs;
  }

  private void applyWBM(double[][] costs) {
    int outDegree = Configuration.getInstance().getOutDegreePerCluster();

    for (int i = 0; i < outDegree; i++) {
      int[] output = new HungarianAlgorithm(costs).execute();
      disableEdges(output, costs);
      buildConnections(output);
    }
  }

  private void disableEdges(int[] output, double[][] costs) {
    for (int i = 0; i < output.length; i++) {
      costs[i][output[i]] = 1000;
    }
  }

  private void buildConnections(int[] output) {
    int numOfClusters = dataCenterRHODA.getNumOfClusters();

    for (int i = 0; i < numOfClusters; i++) {
      connections.get(i).add(output[i]);
    }
  }

  private void printCosts(double[][] costs) {
    System.out.println("Start to print costs");
    for (int i = 0; i < costs.length; i++) {
      System.out.println(Arrays.toString(costs[i]));
    }
    System.out.println("finish");
  }
}
