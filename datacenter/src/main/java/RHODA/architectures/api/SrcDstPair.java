package RHODA.architectures.api;

import java.util.Objects;

public class SrcDstPair {
  private final int nodeIdSrc;
  private final int nodeIdDst;

  public SrcDstPair(final int nodeIdSrc,
                    final int nodeIdDst) {
    this.nodeIdSrc = nodeIdSrc;
    this.nodeIdDst = nodeIdDst;
  }

  public int getNodeIdSrc() {
    return nodeIdSrc;
  }

  public int getNodeIdDst() {
    return nodeIdDst;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SrcDstPair srcDstPair = (SrcDstPair) o;
    return nodeIdSrc == srcDstPair.nodeIdSrc &&
        nodeIdDst == srcDstPair.nodeIdDst;
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeIdSrc, nodeIdDst);
  }

  @Override
  public String toString() {
    return "SrcDstPair{" +
        "nodeIdSrc=" + nodeIdSrc +
        ", nodeIdDst=" + nodeIdDst +
        '}';
  }
}
