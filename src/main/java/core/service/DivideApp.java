package core.service;

import entity.DividePlan;
import entity.DividenContainer;
import entity.ImmutableDividen;
import entity.ImmutableSRVoter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import program.HttpUtil;

public class DivideApp {

  private final int SUN = 1_000_000;
  private final long BASETIMESTAMP = 1529906397; // the first maintance
  private final long SECOND_PER_MAINTENCE = 6 * 60 * 60; // the first maintance
  private final int percent;

  DividenContainer container = new DividenContainer();
  DividePlan plan = new DividePlan();
  private final String sr_address;
  private final String privateKey;
  private final String fullnode;
  private final String soliditynode;
  private final String timestamp;


  public DivideApp(String sr_address, String privateKey, String fullnode, String soliditynode,
      String timestamp, String percent) {
    /*this.sr_address = sr_address;
    this.privateKey = privateKey;
    this.fullnode = fullnode;
    this.soliditynode = soliditynode;
    this.timestamp = makeTimestamp(timestamp);*/
    this.sr_address = "414a193c92cd631c1911b99ca964da8fd342f4cddd"; //"TGj1Ej1qRzL9feLTLhjwgxXF4Ct6GTWg2U";
    this.privateKey = "";
    this.fullnode = "localhost:8090"; //    http://47.89.180.128:8090/wallet/getwitnessvote?address=4119bec4e96e417f936d40bd4fe3876d1a516a064d&round=8294420
    this.soliditynode = "47.89.180.128:8090";
    this.timestamp = makeTimestamp("1529906397");
    this.percent = makePercent(percent);
  }

  public String makeTimestamp(final String timestamp) {
    long time;
    if ("".equals(timestamp)) {
      time = System.currentTimeMillis() / 1000;
    } else {
      time = Long.valueOf(timestamp);
    }

    return String.valueOf(time - (time - BASETIMESTAMP) % SECOND_PER_MAINTENCE);
  }

  public int makePercent(final String percent) {
    int temp = Integer.valueOf(percent);
    if (temp > 80) {
      temp = 80;
    }
    return temp;
  }


  //calc trx per share
  public int calcTrxPerShare(long produceReward, Long totalVotes) {
    double tmp = (double) produceReward / totalVotes;
    return (int) (tmp * SUN);
  }

  public void start() {
    long produceReward = calcProduceReward();
    ImmutableSRVoter srVoter = getSRVoter();
    int trxPerShare = calcTrxPerShare(produceReward, srVoter.getTotalVotes());
    divide(srVoter, trxPerShare);
    //checkTxSuccess();
  }

  public void divide(ImmutableSRVoter srVoter, int trxPerShare) {
    String address;
    Long voteCount;
    Long reward;
    List<ImmutableDividen> list = new ArrayList<>();
    for (Map.Entry<String, Long> entry : srVoter.getVoteMap().entrySet()) {
      address = entry.getKey();
      voteCount = entry.getValue();
      reward = voteCount * trxPerShare;
      list.add(new ImmutableDividen(address, reward));
    }
    for (int i = 0; i < list.size(); i++) {
      System.out.println(String
          .format("sendcoin by easyTransferByPrivate: privateKey:%s bAddress:%s amount:%s",
              privateKey, list.get(i).getAddress(), list.get(i).getAmount()));
    }
    //container.add(srVoter.getKey(), list);
  }


  public boolean checkTxSuccess() {
    List<ImmutableDividen> list = container.get("");
    List<String> txidList = new ArrayList<>();

    for (ImmutableDividen divide : list) {
      txidList.add(divide.getTransaction());
    }

    HttpResponse response;
    for (String txid : txidList) {
      response = HttpUtil.getTransactionByIdFromSolidity(this.soliditynode, txid);
    }
    return true;
  }

  //fetch sr's voters.
  public ImmutableSRVoter getSRVoter() {
    Map<String, Long> voteTotalMap = HttpUtil.getVoterOfWitness(fullnode, sr_address, timestamp);
    return new ImmutableSRVoter(sr_address, voteTotalMap);
    //Map<String, Long> voteMap = HttpUtil.listVoterOfWitness(fullnode, sr_address);
    /*return new ImmutableSRVoter(voteTotalMap.get("totalVoter"), voteTotalMap.get("totalVotes"),
        sr_address, voteMap);*/
  }

  //calc sr produce reward.
  public long calcProduceReward() {
    return (long) 86400 * 32 * this.percent / 3 / 300;
  }

  //send trx to every voter
  public void sendCoinToVoter(String address, Long amount) {
    byte[] bAddress = address.getBytes();
    HttpUtil.easyTransferByPrivate(fullnode, privateKey, bAddress, amount);
  }

  public static void main(String[] args) {
    Configuration.setParam(args);
    new DivideApp(Configuration.getInstance().address, Configuration.getInstance().privateKey,
        Configuration.getInstance().fullnode, Configuration.getInstance().soliditynode,
        Configuration.getInstance().timestamp, Configuration.getInstance().percent)
        .start();
  }
}