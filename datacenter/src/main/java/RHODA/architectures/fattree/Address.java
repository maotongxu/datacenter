package RHODA.architectures.fattree;

public class Address {
  private int address1;
  private int address2;
  private int address3;
  private int address4;

  public Address() {

  }

  public Address(final int address1,
                 final int address2,
                 final int address3,
                 final int address4) {
    this.address1 = address1;
    this.address2 = address2;
    this.address3 = address3;
    this.address4 = address4;
  }

  public int getAddress1() {
    return address1;
  }

  public void setAddress1(int address1) {
    this.address1 = address1;
  }

  public int getAddress2() {
    return address2;
  }

  public void setAddress2(int address2) {
    this.address2 = address2;
  }

  public int getAddress3() {
    return address3;
  }

  public void setAddress3(int address3) {
    this.address3 = address3;
  }

  public int getAddress4() {
    return address4;
  }

  public void setAddress4(int address4) {
    this.address4 = address4;
  }

  @Override
  public String toString() {
    return "Address{" +
        "address1=" + address1 +
        ", address2=" + address2 +
        ", address3=" + address3 +
        ", address4=" + address4 +
        '}';
  }
}
