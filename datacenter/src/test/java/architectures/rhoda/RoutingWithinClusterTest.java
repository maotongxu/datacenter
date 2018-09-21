package architectures.rhoda;

import RHODA.architectures.common.Configuration;
import RHODA.architectures.rhoda.RoutingWithinCluster;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.testng.Assert;

public class RoutingWithinClusterTest {

  @Test
  public void test() {
    Configuration.getInstance().setNumOfRacksPerCluster(6);
    Configuration.getInstance().setNumOfWavelengths(12);
    List<Integer> result = RoutingWithinCluster.getAvailableWavelengths(2, 5);
    System.out.println(result);
  }
}
