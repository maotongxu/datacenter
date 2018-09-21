package RHODA.architectures.api;

import RHODA.architectures.common.Configuration;

public class Wavelength {
  private final int wavelengthId;
  private final double wavelengthCapacity;
  private final int src;
  private final int dst;

  private double bandwidthUsed;

  public Wavelength(final int wavelengthId,
                    final int src,
                    final int dst) {
    this.wavelengthId = wavelengthId;
    this.wavelengthCapacity = Configuration.getInstance().getWavelengthCapacity();
    this.src = src;
    this.dst = dst;
  }

  public int getWavelengthId() {
    return wavelengthId;
  }

  public double getWavelengthCapacity() {
    return wavelengthCapacity;
  }

  public int getSrc() {
    return src;
  }

  public int getDst() {
    return dst;
  }

  public double getBandwidthUsed() {
    return bandwidthUsed;
  }

  public void setBandwidthUsed(double bandwidthUsed) {
    this.bandwidthUsed = bandwidthUsed;
  }

  @Override
  public String toString() {
    return "Wavelength{" +
        "wavelengthId=" + wavelengthId +
        ", src=" + src +
        ", dst=" + dst +
        ", bandwidthUsed=" + bandwidthUsed +
        '}';
  }
}
