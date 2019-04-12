package common.utils.config;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Preconditions;

/**
 * @program: dividen
 * @description: percent参数校验
 * @author: shydesky@gmail.com
 * @create: 2019-04-12
 **/

public class PercentParamCheck implements IParameterValidator {

  private final int MAX_VALUE = 70;
  private final int MIN_VALUE = 1;

  @Override
  public void validate(String name, String value) throws ParameterException {
    try {
      Preconditions.checkArgument(Integer.valueOf(value) > MIN_VALUE);
      Preconditions.checkArgument(Integer.valueOf(value) <= MAX_VALUE);
    }catch (IllegalArgumentException e){
      throw new ParameterException(e.getMessage());
    }
  }
}