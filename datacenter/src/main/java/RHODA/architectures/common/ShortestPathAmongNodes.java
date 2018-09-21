package RHODA.architectures.common;

import RHODA.architectures.api.Path;
import RHODA.architectures.api.SrcDstPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class ShortestPathAmongNodes {
  private final int numOfNodes;
  private final Map<Integer, Set<Integer>> connections;
  private final Map<SrcDstPair, Path> pairPathMap;

  private Set<Integer> settledNodes;
  private Set<Integer> unSettledNodes;
  private Map<Integer, Integer> distance;
  private Map<Integer, Integer> predecessors;

  public ShortestPathAmongNodes(final Map<Integer, Set<Integer>> connections) {
    this.numOfNodes = connections.keySet().size();
    this.connections = connections;
    pairPathMap = new HashMap<>();
  }

  public Map<SrcDstPair, Path> getPairPathMap() {
    return pairPathMap;
  }

  public void findShortestPath() {
    for (int c = 0; c < numOfNodes; c++) {
      findShortestPathFromNode(c);
      buildPath(c);
    }
  }

  private void findShortestPathFromNode(final int source) {
    settledNodes = new HashSet<>();
    unSettledNodes = new HashSet<>();
    distance = new HashMap<>();
    predecessors = new HashMap<>();
    distance.put(source, 0);
    unSettledNodes.add(source);

    while (unSettledNodes.size() > 0) {
      int nodeIndex = getMinimumFromUnSettledNodes();
      settledNodes.add(nodeIndex);
      unSettledNodes.remove(nodeIndex);
      findMinimalDistances(nodeIndex);
    }
  }

  private void buildPath(int src) {
    for (int dst : predecessors.keySet()) {
      List<Integer> pathList = new ArrayList<>();
      int nodeIndex = dst;
      pathList.add(dst);
      while(predecessors.get(nodeIndex) != null) {
        nodeIndex = predecessors.get(nodeIndex);
        pathList.add(nodeIndex);
      }

      Path path = new Path(src, dst);
      for (int n=pathList.size()-1; n>=0; n--) {
        path.addPathNodeId(pathList.get(n));
      }

      SrcDstPair pair = new SrcDstPair(src, dst);
      pairPathMap.put(pair, path);
    }
  }

  private int getMinimumFromUnSettledNodes() {
    int minNodeIndex = -1;
    for (int index : unSettledNodes) {
      if (minNodeIndex == -1) {
        minNodeIndex = index;
      } else {
        if (getShortestDistance(index) < getShortestDistance(minNodeIndex)) {
          minNodeIndex = index;
        }
      }
    }
    return minNodeIndex;
  }

  private int getShortestDistance(int nodeIndex) {
    Integer d = distance.get(nodeIndex);
    if (d == null) {
      return Integer.MAX_VALUE;
    } else {
      return d;
    }
  }

  private void findMinimalDistances(int nodeIndex) {
    List<Integer> adjacentNodeIndexes = getNeighbors(nodeIndex);
    for (int target : adjacentNodeIndexes) {
      if (getShortestDistance(target) > getShortestDistance(nodeIndex) + getDistance(nodeIndex, target)) {
        distance.put(target, getShortestDistance(nodeIndex)+getDistance(nodeIndex, target));
        predecessors.put(target, nodeIndex);
        unSettledNodes.add(target);
      }
    }
  }

  private int getDistance(int nodeIndex, int target) {
    return 1;
  }

  private List<Integer> getNeighbors(int source) {
    List<Integer> neighbors = new ArrayList<>();
    for (int dst : connections.get(source)) {
      if (!settledNodes.contains(dst)) {
        neighbors.add(dst);
      }
    }

    return neighbors;
  }
}
