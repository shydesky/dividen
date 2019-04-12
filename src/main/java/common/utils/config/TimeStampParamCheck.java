package common.utils.config;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Preconditions;

/**
 * @program: dividen
 * @description: TimeStamp参数检查
 * @author: shydesky@gmail.com
 * @create: 2019-04-12
 **/

public class TimeStampParamCheck implements IParameterValidator {

  private final int MIN_VALUE = 1529906397;

  @Override
  public void validate(String name, String input) throws ParameterException {
    Long timestamp;
    try {
      timestamp = Long.valueOf(input);
    } catch (IllegalArgumentException e) {
      throw new ParameterException("Invalid timestamp: " + input);
    }

    try {
      Long now = System.currentTimeMillis() / 1000;
      Preconditions.checkArgument(timestamp >= MIN_VALUE);
      Preconditions.checkArgument(timestamp <= now);
    } catch (IllegalArgumentException e) {
      throw new ParameterException("Invalid timestamp: " + input);
    }
  }
}