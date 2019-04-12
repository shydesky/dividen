package exceptions;

/**
 * @program: dividen
 * @description:
 * @author: shydesky@gmail.com
 * @create: 2019-04-12
 **/

public class ApiRequestException extends RuntimeException {

  public ApiRequestException(String mesage) {
    super(mesage);
  }
}