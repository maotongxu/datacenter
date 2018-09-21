package RHODA.architectures.rhoda;

import RHODA.architectures.api.Flow;
import RHODA.architectures.api.Rack;
import RHODA.architectures.common.FlowComparator;

import java.util.Iterator;
import java.util.PriorityQueue;

public class RackRHODA {
  private final Rack rack;
  private int clusterId;
  private int rackIdWithinCluster;

  private final PriorityQueue<Flow> flowsWaitingForInterClusterTx;
  private final PriorityQueue<Flow> flowsWaitingForIntraClusterTx;

  private double totalTrafficInterCluster;
  private double totalTrafficIntraCluster;

  private int rowSNIndex = -1;
  private int columnSNIndex = -1;

  private boolean installedInCluster = false;

  public RackRHODA(final Rack rack) {
    this.rack = rack;
    flowsWaitingForInterClusterTx = new PriorityQueue<>(new FlowComparator());
    flowsWaitingForIntraClusterTx = new PriorityQueue<>(new FlowComparator());
  }

  public void addFlowForInterClusterTx(final Flow flow) {
    totalTrafficInterCluster += flow.getTraffic();
    flowsWaitingForInterClusterTx.add(flow);
  }

  public void addFlowForIntraClusterTx(final Flow flow) {
    totalTrafficIntraCluster += flow.getTraffic();
    flowsWaitingForIntraClusterTx.add(flow);
  }

  public Flow peekFlowForInterClusterTx() {
    return flowsWaitingForInterClusterTx.peek();
  }

  public Flow peekFlowforIntraClusterTx() {
    return flowsWaitingForIntraClusterTx.peek();
  }

  public Iterator<Flow> flowForInterClusterTxIter() {
    return flowsWaitingForInterClusterTx.iterator();
  }

  public Iterator<Flow> flowForIntraClusterTxIter() {
    return flowsWaitingForIntraClusterTx.iterator();
  }

  public Rack getRack() {
    return rack;
  }

  public int getClusterId() {
    return clusterId;
  }

  public void setClusterId(int clusterId) {
    this.clusterId = clusterId;
  }

  public int getRackIdWithinCluster() {
    return rackIdWithinCluster;
  }

  public void setRackIdWithinCluster(int rackIdWithinCluster) {
    this.rackIdWithinCluster = rackIdWithinCluster;
  }

  public double getTotalTrafficInterCluster() {
    return totalTrafficInterCluster;
  }

  public void setTotalTrafficInterCluster(double totalTrafficInterCluster) {
    this.totalTrafficInterCluster = totalTrafficInterCluster;
  }

  public double getTotalTrafficIntraCluster() {
    return totalTrafficIntraCluster;
  }

  public void setTotalTrafficIntraCluster(double totalTrafficIntraCluster) {
    this.totalTrafficIntraCluster = totalTrafficIntraCluster;
  }

  public int getRowSNIndex() {
    return rowSNIndex;
  }

  public void setRowSNIndex(int rowSNIndex) {
    this.rowSNIndex = rowSNIndex;
  }

  public int getColumnSNIndex() {
    return columnSNIndex;
  }

  public void setColumnSNIndex(int columnIndex) {
    this.columnSNIndex = columnIndex;
  }

  public void setSNIndex(int rowSNIndex, int columnSNIndex) {
    this.rowSNIndex = rowSNIndex;
    this.columnSNIndex = columnSNIndex;
  }

  public boolean isInstalledInCluster() {
    return installedInCluster;
  }

  public void setInstalledInCluster(boolean installedInCluster) {
    this.installedInCluster = installedInCluster;
  }

  @Override
  public String toString() {
    return "RackRHODA{" +
        "rackId=" + rack.getRackId() +
        ", clusterId=" + clusterId +
        '}';
  }
}
