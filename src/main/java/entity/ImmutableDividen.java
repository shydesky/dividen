package entity;

public class ImmutableDividen {

  public String getAddress() {
    return address;
  }

  public byte[] getbAddress() {
    return bAddress;
  }

  public Long getAmount() {
    return amount;
  }

  public boolean isTxConfirmed() {
    return txConfirmed;
  }

  public void setTxConfirmed(boolean flag){
    this.txConfirmed = flag;
  }

  public String getTxID(){
    return txID;
  }

  private final String address;
  private final byte[] bAddress;
  private final Long amount;

  private final String txID;
  private boolean txConfirmed = false;

  public ImmutableDividen(String address, Long amount, String txID) {
    this.address = address;
    this.bAddress = address.getBytes();
    this.amount = amount;
    this.txID = txID;
  }
}