package exceptions;

/**
 * @program: dividen
 * @description:
 * @author: shydesky@gmail.com
 * @create: 2019-04-15
 **/

public class WithdrawAllowanceException extends IllegalStateException{

  public WithdrawAllowanceException(String msg){
    super(msg);
  }
}