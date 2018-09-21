package RHODA.architectures.api;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class Path {
  private final int nodeIdSrc;
  private final int nodeIdDst;
  private final List<Integer> pathNodeIdList;

  public Path(final int nodeIdSrc,
              final int nodeIdDst) {
    this.nodeIdSrc = nodeIdSrc;
    this.nodeIdDst = nodeIdDst;
    pathNodeIdList = new ArrayList<>();
  }

  public int getNodeIdSrc() {
    return nodeIdSrc;
  }

  public int getNodeIdDst() {
    return nodeIdDst;
  }

  public void addPathNodeId(final int rackId) {
    pathNodeIdList.add(rackId);
  }

  public int getPathNodeId(final int index) {
    return pathNodeIdList.get(index);
  }

  public int pathNodeIdListSize() {
    return pathNodeIdList.size();
  }

  public List<Integer> getPathNodeIdList() {
    return pathNodeIdList;
  }

  @Override
  public String toString() {
    return "Path{" +
        "nodeIdSrc=" + nodeIdSrc +
        ", nodeIdDst=" + nodeIdDst +
        ", pathNodeIdList=" + StringUtils.join(pathNodeIdList, "->") +
        '}';
  }
}
