package RHODA.architectures.common;

public class Configuration {
  private static Configuration INSTANCE = null;

  private int trafficPattern = TrafficPattern.FACEBOOK_TRAFFIC;

  private int numOfRacks = 1024;
  private int numOfWavelengths = 128;
  private int wavelengthCapacity = 100;
  private int numOfTransceiversPerRack = 4;

  private int degreeOfRackOSA = 4;

  /**RHODA parameters*/
  private int membershipRatio = 4;
  private int numOfRacksPerCluster = 64;
  private int outDegreePerCluster = 4;
  private int numOfTransceiversPerRackIntra = 2;

  private Configuration() {

  }

  public static Configuration getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Configuration();
    }
    return INSTANCE;
  }

  public int getTrafficPattern() {
    return trafficPattern;
  }

  public void setTrafficPattern(int trafficPattern) {
    this.trafficPattern = trafficPattern;
  }

  public int getNumOfRacks() {
    return numOfRacks;
  }

  public int getNumOfWavelengths() {
    return numOfWavelengths;
  }

  public int getWavelengthCapacity() {
    return wavelengthCapacity;
  }

  public int getNumOfTransceiversPerRack() {
    return numOfTransceiversPerRack;
  }

  public int getDegreeOfRackOSA() {
    return degreeOfRackOSA;
  }

  public int getMembershipRatio() {
    return membershipRatio;
  }

  public int getNumOfRacksPerCluster() {
    return numOfRacksPerCluster;
  }

  public int getOutDegreePerCluster() {
    return outDegreePerCluster;
  }

  public void setNumOfRacks(int numOfRacks) {
    this.numOfRacks = numOfRacks;
  }

  public void setNumOfWavelengths(int numOfWavelengths) {
    this.numOfWavelengths = numOfWavelengths;
  }

  public void setWavelengthCapacity(int wavelengthCapacity) {
    this.wavelengthCapacity = wavelengthCapacity;
  }

  public void setNumOfTransceiversPerRack(int numOfTransceiversPerRack) {
    this.numOfTransceiversPerRack = numOfTransceiversPerRack;
  }

  public void setMembershipRatio(int membershipRatio) {
    this.membershipRatio = membershipRatio;
  }

  public void setNumOfRacksPerCluster(int numOfRacksPerCluster) {
    this.numOfRacksPerCluster = numOfRacksPerCluster;
  }

  public void setOutDegreePerCluster(int outDegreePerCluster) {
    this.outDegreePerCluster = outDegreePerCluster;
  }

  public int getNumOfTransceiversPerRackIntra() {
    return numOfTransceiversPerRackIntra;
  }

  public void setNumOfTransceiversPerRackIntra(int numOfTransceiversPerRackIntra) {
    this.numOfTransceiversPerRackIntra = numOfTransceiversPerRackIntra;
  }
}
