package core.service;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * @program: dividen
 * @description:
 * @author: shydesky@gmail.com
 * @create: 2019-04-10
 **/


public class Configuration {

  private static final Configuration INSTANCE = new Configuration();

  public static Configuration getInstance() {
    return INSTANCE;
  }

  @Parameter(names = {"-p", "--private-key"}, description = "private key of SR")
  public String privateKey;

  @Parameter(names = {"-a", "---address"}, description = "address of SR")
  public String address;

  @Parameter(names = {"-f", "--fullnode"}, description = "fullnode uri")
  public String fullnode;

  @Parameter(names = {"-s", "--soliditynode"}, description = "soliditynode uri")
  public String soliditynode;

  @Parameter(names = {"-t", "--timestamp"}, description = "maintenance timestamp")
  public String timestamp;

  @Parameter(names = {"-d", "--percent"}, description = "divide percent")
  public String percent;


  public static void setParam(final String[] args) {
    JCommander.newBuilder().addObject(INSTANCE).build().parse(args);
    if (!checkAddressPrivateKeyMatch()) {
      throw new IllegalArgumentException("privateKey wrong or address wrong");
    }
    ;
    if (!checkTimeStamp()) {
      throw new IllegalArgumentException("timestamp wrong");
    }
    ;
    if (!checkFullnode()) {
      throw new IllegalArgumentException("fullnode wrong");
    }
    ;
    if (!checkSoliditynode()) {
      throw new IllegalArgumentException("soliditynode wrong");
    }
    ;
    if (!checkPercent()) {
      throw new IllegalArgumentException("soliditynode wrong");
    }
  }

  public static boolean checkAddressPrivateKeyMatch() {
    return true;
  }

  public static boolean checkTimeStamp() {
    return true;
  }

  public static boolean checkFullnode() {
    return !("".equals(INSTANCE.fullnode));
  }

  public static boolean checkSoliditynode() {
    return !("".equals(INSTANCE.soliditynode));
  }

  public static boolean checkPercent() {
    return Double.valueOf(INSTANCE.percent) >= 0 && Double.valueOf(INSTANCE.percent) <= 80;
  }
}