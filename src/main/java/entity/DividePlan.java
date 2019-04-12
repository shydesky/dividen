package entity;

/**
 * @program: dividen
 * @description:
 * @author: shydesky@gmail.com
 * @create: 2019-04-10
 **/

public class DividePlan {


  public boolean[] status = new boolean[5];

  public void setStatus(int index, boolean isfinished){
    this.status[index] = isfinished;
  }
}