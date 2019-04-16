package common.utils.config;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Preconditions;

/**
 * @program: dividen
 * @description: FullNode参数校验
 * @author: shydesky@gmail.com
 * @create: 2019-04-12
 **/

public class FullNodeParamCheck implements IParameterValidator {

  @Override
  public void validate(String name, String input) throws ParameterException {
    try {
      Preconditions.checkArgument(IPCheck(input));
    } catch (IllegalArgumentException e) {
      throw new ParameterException("invalid FullNode ip: " + input);
    }
  }

  public static boolean IPCheck(String str) {
    if (str == null || str.isEmpty()) {
      return false;
    }

    String regex = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    try {
      String ip;
      if (str.indexOf(":") > 0) {
        ip = str.substring(0, str.indexOf(":"));

        Preconditions
            .checkArgument(Integer.valueOf(str.substring(str.indexOf(":") + 1, str.length())) > 0);
      }else{
        ip = str;
      }
      return ip.matches(regex);
    } catch (Exception e) {
      return false;
    }
  }
}