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

  public boolean isSuccess() {
    return isSuccess;
  }

  private final String address;
  private final byte[] bAddress;
  private final Long amount;

  private String transaction = "";
  private boolean isSuccess = false;

  public ImmutableDividen(String address, Long amount) {
    this.address = address;
    this.bAddress = address.getBytes();
    this.amount = amount;
  }

  public void setTransaction(String transaction){
    if(!isSuccess){
      this.transaction = transaction;
    }
  }

  public String getTransaction(){
    return this.transaction;
  }
}