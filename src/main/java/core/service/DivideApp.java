package core.service;

import static com.google.common.base.Predicates.not;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import common.utils.AddressUtils;
import common.utils.config.Configuration;
import entity.ImmutableDividen;
import entity.ImmutableSRVoter;
import exceptions.ApiRequestException;
import exceptions.FetchSrBalanceException;
import exceptions.WithdrawAllowanceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class DivideApp {

  private static Logger log = Logger.getLogger(DivideApp.class.getClass());

  public class SRInfo {

    private final String address_hex;
    private final String address_b58check;
    private final String privateKey;


    SRInfo(String address_hex, String privateKey) {
      this.privateKey = privateKey;
      this.address_hex = address_hex.toLowerCase();
      this.address_b58check = makeAddressB58check();
    }

    private String makeAddressB58check() {
      return AddressUtils.calcBase58CheckAddressFromPrivateKey(this.privateKey);
    }
  }


  public class ApiNodeInfo {

    private String fullNode;
    private String solidityNode;

    ApiNodeInfo(String fullNode, String solidityNode) {
      this.fullNode = fullNode;
      this.solidityNode = solidityNode;
    }
  }

  public class DivideInfo {

    private final String[] timestamp;
    private final int percent;

    DivideInfo(String timestamp, Integer percent) {
      this.timestamp = makeTimestamp(timestamp);
      this.percent = percent;
    }

    private String[] makeTimestamp(final String timestamp) {
      String[] fourTimestamp = new String[4];
      Long time = Long.valueOf(timestamp);
      Long timeStamp0 = time - (time - BASETIMESTAMP) % SECOND_PER_MAINTENCE;
      Long timeStamp1 = timeStamp0 - SECOND_PER_MAINTENCE;
      Long timeStamp2 = timeStamp1 - SECOND_PER_MAINTENCE;
      Long timeStamp3 = timeStamp2 - SECOND_PER_MAINTENCE;

      fourTimestamp[3] = String.valueOf(timeStamp0);
      fourTimestamp[2] = String.valueOf(timeStamp1);
      fourTimestamp[1] = String.valueOf(timeStamp2);
      fourTimestamp[0] = String.valueOf(timeStamp3);

      return fourTimestamp;
    }
  }

  private final int SUN = 1_000_000;
  private final long BASETIMESTAMP = 1529906397; // the first maintance
  private final long SECOND_PER_MAINTENCE = 10 * 60; // the first maintance

  private final SRInfo srInfo;
  private final ApiNodeInfo apiNodeInfo;
  private final DivideInfo divideInfo;

  private ImmutableSRVoter[] srVoter = new ImmutableSRVoter[4];
  private Map<Integer, ArrayList<ImmutableDividen>> divideListMap = new HashMap();

  private int trxPerShare = 0;
  private long produceReward;

  public DivideApp(String sr_address_hex, String privateKey, String fullNode, String solidityNode,
      String timestamp, Integer percent) {

    this.srInfo = new SRInfo(sr_address_hex, privateKey);
    this.apiNodeInfo = new ApiNodeInfo(fullNode, solidityNode);
    this.divideInfo = new DivideInfo(timestamp, percent);
  }

  //divide service main logic
  public void start() {
    log.info("*******执行分红开始*******");
    logExecuteInfo();

    //seven steps: withdraw allowance -- calc sr reward -- check balance enough -- count sr voter's votes -- calc reward of one vote -- divide reward -- check reward tx
    if (!Configuration.getInstance().ignore_allowance) {
      withdrawAllowance();
    }

    this.produceReward = calcProduceReward();

    Preconditions.checkState(checkSrBalance() > this.produceReward, "SR's balance is not enough");

    for (int index = 0; index < 4; index++) {
      this.srVoter[index] = getSRVoter(index);
      this.trxPerShare = calcTrxPerShare(this.produceReward, this.srVoter[index].getTotalVotes());
      this.divideListMap.put(index, divide(trxPerShare, this.srVoter[index].getVoteMap()));
      try {
        Thread.sleep(81 * 1000);
      } catch (InterruptedException e) {

      }
      checkTxWithRetry(5, index);
    }

    log.info("*******执行分红结果*******");
    for (int index = 0; index < 4; index++) {
      ArrayList<ImmutableDividen> alist = this.divideListMap.get(index);
      List<ImmutableDividen> success = alist.stream().filter(ImmutableDividen::isTxConfirmed)
          .collect(Collectors
              .toList());

      // 交易成功上链
      for (ImmutableDividen divide : success) {
        log.info(String.format("address:%s txID:%s amount:%d is confirmed", divide.getAddress(),
            divide.getTxID(), divide.getAmount()));
      }

      // 交易未成功上链
      List<ImmutableDividen> failure = alist.stream().filter(not(ImmutableDividen::isTxConfirmed))
          .collect(Collectors.toList());
      for (ImmutableDividen divide : failure) {
        log.info(String.format("address:%s txID:%s amount:%d is not confirmed", divide.getAddress(),
            divide.getTxID(), divide.getAmount()));
      }
    }

    log.info("*******执行分红结束*******");
  }

  private void withdrawAllowance() throws WithdrawAllowanceException {
    int retry = 5;
    while (retry-- > 0) {
      boolean success = HttpUtil.withdrawBalance(this.apiNodeInfo.fullNode, this.srInfo.address_hex,
          this.srInfo.privateKey);
      if (success) {
        return;
      }
    }
    throw new WithdrawAllowanceException("SR withdraw balance failure.");
  }

  public void logExecuteInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append("Execute Dividen Information").append("\n");
    sb.append("SR address(HEX): ").append(this.srInfo.address_hex).append("\n");
    sb.append("SR address(b58c): ").append(this.srInfo.address_b58check).append("\n");
    sb.append("API Node FullNode: ").append(this.apiNodeInfo.fullNode).append("\n");
    sb.append("API Node SolidityNode: ").append(this.apiNodeInfo.solidityNode).append("\n");
    for (int i = 0; i < 4; i++) {
      sb.append("timestamp ").append(i).append(":").append(this.divideInfo.timestamp[i])
          .append("\n");
    }
    sb.append("Divide percent: ").append(this.divideInfo.percent).append("%").append("\n");
    log.info(String.format("分红执行配置：%s", sb.toString()));
  }

  //calc sr produce reward.
  private long calcProduceReward() {
    BigDecimal b = new BigDecimal(86400 / 3 / 4);
    BigDecimal sr = new BigDecimal(27);
    long produceReward = (long) (b.divide(sr, 2).floatValue() * 32 * this.divideInfo.percent
        / 100.0);
    log.info("SR获得的区块奖励:" + produceReward);
    return produceReward;
  }


  //fetch sr's voters.
  private ImmutableSRVoter getSRVoter(int index) {
    ImmutableSRVoter srVoter = null;
    Map<String, Long> voteTotalMap = null;
    int retry = 5;
    while (retry-- > 0) {
      voteTotalMap = getVoterOfWitness(this.apiNodeInfo.fullNode,
          this.srInfo.address_hex, this.divideInfo.timestamp[index]);
      if (voteTotalMap.size() > 0) {
        break;
      }
    }
    srVoter = new ImmutableSRVoter(this.srInfo.address_hex, voteTotalMap,
        this.divideInfo.timestamp[index]);
    log.info(srVoter);
    return srVoter;
  }

  //calc trx per share
  private int calcTrxPerShare(long produceReward, Long totalVotes) {
    double tmp = (double) produceReward / totalVotes;
    log.info(String.format("每张选票分得的收益：%d Sun", (int) (tmp * SUN)));
    return (int) (tmp * SUN);
  }

  //divide the reward
  private ArrayList<ImmutableDividen> divide(int trxPerShare, Map<String, Long> voteMap) {
    String address, txID;
    Long voteCount;
    Long reward;
    ArrayList<ImmutableDividen> list = new ArrayList<>();

    for (Map.Entry<String, Long> entry : voteMap.entrySet()) {
      address = entry.getKey();
      voteCount = entry.getValue();
      reward = voteCount * trxPerShare;
      txID = sendCoinToVoter(this.apiNodeInfo.fullNode, this.srInfo.privateKey, address, reward);
      list.add(new ImmutableDividen(address, reward, txID));
    }
    return list;
  }

  public void checkTxWithRetry(int retry, int index) {

    ArrayList<ImmutableDividen> divideList = this.divideListMap.get(index);
    long confirmed = divideList.stream().filter(ImmutableDividen::isTxConfirmed).count();
    Preconditions.checkState(confirmed == 0);

    int retry_times = 0;
    while (retry_times++ < retry) {
      checkDivideTx(index);
      confirmed = divideList.stream().filter(ImmutableDividen::isTxConfirmed).count();
      log.info(String.format("第%d次检测交易完成, 已确认的交易数量: %d", retry_times, confirmed));
      if (divideList.size() == confirmed) {
        break;
      }
    }
  }

  public void checkDivideTx(int index) {

    ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 50, 200, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>());

    ArrayList<ImmutableDividen> divideList = this.divideListMap.get(index);
    for (ImmutableDividen dividen : divideList) {
      if (dividen.isTxConfirmed()) {
        continue;
      }
      executor.submit(() -> {
        HttpResponse response = HttpUtil
            .getTransactionByIdFromSolidity(this.apiNodeInfo.solidityNode, dividen.getTxID());
        if (response != null) {
          try {
            String transactionString = null;
            transactionString = EntityUtils.toString(response.getEntity());
            JSONObject result = JSONObject.parseObject(transactionString);
            if (result.keySet().contains("ret")) {
              if ("SUCCESS".equals(result.getJSONArray("ret").getJSONObject(0)
                  .getString("contractRet"))) {
                if (!dividen.isTxConfirmed()) {
                  dividen.setTxConfirmed(true);
                }
              }
            }
          } catch (Exception e) {
          }
        } else {
        }
      });
    }

    executor.shutdown();
    while (!executor.isTerminated()) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {

      }
    }
  }

  //send trx to every voter
  private String sendCoinToVoter(String httpnode, String privateKey, String addressHex,
      Long amount) {
    String transactionString = null;
    amount = 1000000L; //to do
    HttpResponse response = HttpUtil
        .easyTransferByPrivate(httpnode, privateKey, addressHex, amount);
    try {
      transactionString = EntityUtils.toString(response.getEntity());
      JSONObject result = JSONObject.parseObject(transactionString);
      if (result.keySet().contains("transaction")) {
        JSONObject transaction = JSONObject.parseObject(transactionString)
            .getJSONObject("transaction");
        transactionString = transaction.getString("txID");
      }
    } catch (Exception e) {
    }

    return transactionString;
  }

  public static Map<String, Long> getVoterOfWitness(String httpNode, String addressHex,
      String timestamp) {

    Map<String, Long> res = new HashMap<>();

    try {
      HttpResponse response = HttpUtil.listVoterOfWitness(httpNode, addressHex, timestamp);
      JSONObject responseContent = HttpUtil.parseResponseContent(response);
      Preconditions.checkState(responseContent != null && responseContent.keySet().size() > 0);
      Set<String> set = responseContent.keySet();
      for (String voterAddress : set) {
        res.put(voterAddress, Long.valueOf(String.valueOf(responseContent.get(voterAddress))));
      }
    } catch (Exception e) {
    }

    return res;
  }

  public Long checkSrBalance() throws FetchSrBalanceException {
    Long balance;
    int retry = 5;
    while (retry-- > 0) {
      balance = HttpUtil.getBalance(this.apiNodeInfo.fullNode, this.srInfo.address_hex);
      if (balance != null) {
        return balance;
      }
    }
    throw new FetchSrBalanceException("get balance failure");
  }

  public static void main(String[] args) {
    Configuration.setParam(args);

    try {
      DivideApp app = new DivideApp(
          Configuration.getInstance().address,
          Configuration.getInstance().privateKey,
          Configuration.getInstance().fullNodeIP,
          Configuration.getInstance().solidityNodeIP,
          Configuration.getInstance().timestamp,
          Configuration.getInstance().percent);
      app.start();
    } catch (WithdrawAllowanceException e) {
      log.error("发生API数据请求错误，本次分红没有执行成功。原因：" + e.getMessage());
      System.exit(0);
    } catch (ApiRequestException e) {
      log.error("发生API数据请求错误，本次分红没有执行成功。原因：" + e.getMessage());
      System.exit(0);
    } catch (FetchSrBalanceException e) {
      log.error("发生API数据请求错误，本次分红没有执行成功。原因：" + e.getMessage());
      System.exit(0);
    } catch (IllegalStateException e) {
      log.error("SR选民数据错误，本次分红没有执行成功。原因：" + e.getMessage());
      System.exit(0);
    }
  }
}