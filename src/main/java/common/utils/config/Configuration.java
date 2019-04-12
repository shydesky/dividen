package common.utils.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Preconditions;
import common.utils.AddressUtils;

/**
 * @program: dividen
 * @description: 命令行参数解析
 * @author: shydesky@gmail.com
 * @create: 2019-04-10
 **/


public class Configuration {

  private static final Configuration INSTANCE = new Configuration();

  public static Configuration getInstance() {
    return INSTANCE;
  }

  @Parameter(names = {"-p", "--private-key"}, description = "private key of SR", required = true)
  public String privateKey;

  @Parameter(names = {"-a", "---address"}, description = "address of SR", required = true)
  public String address;

  @Parameter(names = {"-f",
      "--fullnode"}, description = "fullnode ip", validateWith = FullNodeParamCheck.class)
  public String fullNodeIP = "47.89.180.128:8090";

  @Parameter(names = {"-s",
      "--soliditynode"}, description = "soliditynode ip", validateWith = SolidityNodeParamCheck.class)
  public String solidityNodeIP = "47.89.180.128:8091";

  @Parameter(names = {"-t",
      "--timestamp"}, description = "maintenance timestamp", validateWith = TimeStampParamCheck.class)
  public String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

  @Parameter(names = {"-d",
      "--percent"}, description = "divide percent", validateWith = PercentParamCheck.class)
  public Integer percent = 70;



  @Parameter(names = "--ignore-allowance", description = "ignore withdraw allowance check", hidden = true)
  public boolean ignore_allowance = false;

  @Parameter(names = "-debug", description = "Debug mode", hidden = true)
  private boolean debug = false;

  @Parameter(names = "--help", help = true)
  private boolean help;

  public static void setParam(final String[] args) {
    JCommander jct = JCommander.newBuilder().addObject(INSTANCE).build();
    jct.parse(args);
    jct.setProgramName("SR分红");  //设置程序名称

    if (INSTANCE.help) {
      jct.usage();
      System.exit(0);
    }

    checkAddressPrivateKey();
  }

  public static boolean checkAddressPrivateKey() {
    String addressHexFromPri = AddressUtils
        .calcHexAddressFromPrivateKey(INSTANCE.privateKey); // 通过私钥计算地址
    Preconditions.checkArgument(addressHexFromPri.equalsIgnoreCase(INSTANCE.address),
        "private key does not match address.");
    return true;
  }

  public static void main(String[] args) {

  }
}