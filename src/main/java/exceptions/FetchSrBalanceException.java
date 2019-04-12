package exceptions;

/**
 * @program: dividen
 * @description:
 * @author: shydesky@gmail.com
 * @create: 2019-04-16
 **/

public class FetchSrBalanceException extends IllegalStateException {

  public FetchSrBalanceException(String msg) {
    super(msg);
  }
}
