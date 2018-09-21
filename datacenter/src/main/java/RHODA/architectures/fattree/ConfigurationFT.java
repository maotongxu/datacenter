package RHODA.architectures.fattree;

public class ConfigurationFT {
  public static int NUM_OF_PORT_PER_SWITCH = 28;	// should be the same as num_queue_rack for optical
  public static int BUFFER_SIZE = 569;					//8M buffer size; total 699 pkt

  public static int TOTAL_NUM_OF_RACKS = NUM_OF_PORT_PER_SWITCH * NUM_OF_PORT_PER_SWITCH * NUM_OF_PORT_PER_SWITCH / 4;

  public static int K = NUM_OF_PORT_PER_SWITCH;
  public static int NUM_OF_CORE_GROUP = K /2;			// the number of groups
  public static int NUM_OF_CORE_SWITCH_PER_GROUP = K /2;	// the number of core switches in each group
  public static int NUM_OF_POD = K;					// the number of pods
  public static int NUM_OF_AGG_SWITCH_PER_POD = K /2;				// the number of aggregation switches in each pod
  public static int NUM_OF_EDGE_SWITCH_PER_POD = K /2;				// the number of edge switches in each pod
  public static int NUM_OF_HOST_PER_EDGE_SWITCH = K /2;			// the number of hosts under each edge switch

  public static void setConfBasedOnMinNumOfRacks(final int minNumOfRacks) {
    double result = Math.cbrt((double) minNumOfRacks * 4);
    NUM_OF_PORT_PER_SWITCH = (int) Math.ceil(result);
    if (NUM_OF_PORT_PER_SWITCH % 2 != 0) {
      NUM_OF_PORT_PER_SWITCH += 1;
    }
    TOTAL_NUM_OF_RACKS = NUM_OF_PORT_PER_SWITCH * NUM_OF_PORT_PER_SWITCH * NUM_OF_PORT_PER_SWITCH / 4;
    K = NUM_OF_PORT_PER_SWITCH;
    NUM_OF_CORE_GROUP = K /2;
    NUM_OF_CORE_SWITCH_PER_GROUP = K /2;
    NUM_OF_POD = K;
    NUM_OF_AGG_SWITCH_PER_POD = K /2;
    NUM_OF_EDGE_SWITCH_PER_POD = K /2;
    NUM_OF_HOST_PER_EDGE_SWITCH = K /2;
  }
}
