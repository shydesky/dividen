package program;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import common.utils.ByteArray;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;


public class HttpUtil {

  public static HttpClient httpClient;
  public static HttpPost httppost;
  public static HttpGet httpGet;

  public static HttpResponse response;
  static Integer connectionTimeout = 10000;
  static Integer soTimeout = 20000;
  static String transactionString;
  static String transactionSignString;
  static JSONObject responseContent;
  static JSONArray responseContentArray;

  static JSONObject signResponseContent;

  static {
    PoolingClientConnectionManager pccm = new PoolingClientConnectionManager();
    pccm.setDefaultMaxPerRoute(20);
    pccm.setMaxTotal(100);

    httpClient = new DefaultHttpClient(pccm);
  }

  public static HttpResponse createConnect(String url) {
    return createConnect(url, null);
  }

  public static HttpResponse createConnect(String url, JsonObject requestBody) {
    try {
      httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
          connectionTimeout);
      httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
      httppost = new HttpPost(url);
      httppost.setHeader("Content-type", "application/json; charset=utf-8");
      httppost.setHeader("Connection", "Close");
      if (requestBody != null) {
        StringEntity entity = new StringEntity(requestBody.toString(), Charset.forName("UTF-8"));
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httppost.setEntity(entity);
      }
      response = httpClient.execute(httppost);
    } catch (Exception e) {
      e.printStackTrace();
      httppost.releaseConnection();
      return null;
    }
    return response;
  }

  public static JSONArray parseResponseContent(HttpResponse response) {
    try {
      System.out.println(response.getStatusLine().toString());
      String result = EntityUtils.toString(response.getEntity());
      StringEntity entity = new StringEntity(result, Charset.forName("UTF-8"));
      response.setEntity(entity);
      JSONArray obj = JSONArray.parseArray(result);
      return obj;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  //handle http request

  //list witness
  public static HttpResponse listWitnesses(String httpNode) {
    try {
      String requestUrl = "http://" + httpNode + "/wallet/listwitnesses";
      response = createConnect(requestUrl);
    } catch (Exception e) {
      e.printStackTrace();
      httppost.releaseConnection();
      return null;
    }
    return response;
  }

  //easy transfer coin
  public static HttpResponse easyTransfer(String httpNode, String value, byte[] toAddress,
      Long amount) {
    try {
      final String requestUrl = "http://" + httpNode + "/wallet/easytransfer";
      JsonObject userBaseObj2 = new JsonObject();
      userBaseObj2.addProperty("toAddress", ByteArray.toHexString(toAddress));
      userBaseObj2.addProperty("passPhrase", str2hex(value));
      userBaseObj2.addProperty("amount", amount);
      response = createConnect(requestUrl, userBaseObj2);
      //logger.info(userBaseObj2.toString());
      transactionString = EntityUtils.toString(response.getEntity());
      //logger.info(transactionString);
    } catch (Exception e) {
      e.printStackTrace();
      httppost.releaseConnection();
      return null;
    }
    return response;
  }

  //easy transfer coin with private key
  public static HttpResponse easyTransferByPrivate(String httpNode, String privateKey,
      byte[] toAddress, Long amount) {
    try {
      final String requestUrl = "http://" + httpNode + "/wallet/easytransferbyprivate";
      JsonObject userBaseObj2 = new JsonObject();
      userBaseObj2.addProperty("privateKey", privateKey);
      userBaseObj2.addProperty("toAddress", ByteArray.toHexString(toAddress));
      userBaseObj2.addProperty("amount", amount);
      response = createConnect(requestUrl, userBaseObj2);
      //logger.info(userBaseObj2.toString());
      transactionString = EntityUtils.toString(response.getEntity());
      //logger.info(transactionString);
    } catch (Exception e) {
      e.printStackTrace();
      httppost.releaseConnection();
      return null;
    }
    return response;
  }

  //list voters of a witness
  /*public static HashMap<String, Long> listVoterOfWitness(String httpNode, String address) {
    HashMap<String, Long> res = new HashMap<>();
    HttpResponse response = listVoterOfWitness(httpNode, address, 0, 1);
    responseContent = HttpUtil.parseResponseContent(response);

    Integer totalVoter = Integer.valueOf(responseContent.getString("total"));
    Integer totalVotes = Integer.valueOf(responseContent.getString("totalVotes"));
    int step = 40;
    JSONArray array;
    for (int i = 0; i < totalVoter; i = i + step) {
      response = listVoterOfWitness(httpNode, address, i, step);
      responseContent = HttpUtil.parseResponseContent(response);
      array = responseContent.getJSONArray("data");
      for (Object obj : array) {
        JSONObject json = (JSONObject) obj;
        res.put(json.getString("voterAddress"), Long.valueOf(json.getString("votes")));
      }
    }
    System.out.println("total voters:" + totalVoter);
    System.out.println("total votes:" + totalVotes);

    return res;
  }*/

  public static Map<String, Long> getVoterOfWitness(String httpNode, String address, String timestamp) {
    HttpResponse response = listVoterOfWitness(httpNode, address, timestamp);
    responseContentArray = HttpUtil.parseResponseContent(response);

/*    Integer totalVoter = Integer.valueOf(responseContent.getString("total"));
    Integer totalVotes = Integer.valueOf(responseContent.getString("totalVotes"));
    Map<String, Integer> res = new HashMap<>();
    res.put("totalVoter", totalVoter);
    res.put("totalVotes", totalVotes);*/

    Map<String, Long> res = new HashMap<>();
    Iterator<Object> iter = responseContentArray.iterator();
    JSONObject obj = null;
    while (iter.hasNext()){
      obj = (JSONObject)iter.next();
      Set<String> set = obj.keySet();
      for(String addr: set){
        res.put(addr, Long.valueOf(String.valueOf(obj.get(addr))));
      }
    }
    return res;
  }


  public static HttpResponse listVoterOfWitness(String httpNode, String address, String timestamp) {
    try {
      //http://47.89.180.128:8090/wallet/getwitnessvote?address=4119bec4e96e417f936d40bd4fe3876d1a516a064d&round=8294420

      URI uri = new URIBuilder()
          .setScheme("http")
          .setHost("47.89.180.128")
          .setPort(8090)
          .setPath("/wallet/getwitnessvote")
          .setParameter("address", address)
          .setParameter("round", "8294420")
          .build();
      System.out.println(uri.toString());

      httpGet = new HttpGet(uri);
      response = httpClient.execute(httpGet);
    } catch (Exception e) {
      e.printStackTrace();
      httppost.releaseConnection();
      return null;
    }
    return response;
  }

  public static HttpResponse getTransactionByIdFromSolidity(String httpSolidityNode, String txid) {
    try {
      String requestUrl = "http://" + httpSolidityNode + "/walletsolidity/gettransactionbyid";
      JsonObject userBaseObj2 = new JsonObject();
      userBaseObj2.addProperty("value", txid);
      response = createConnect(requestUrl, userBaseObj2);
    } catch (Exception e) {
      e.printStackTrace();
      httppost.releaseConnection();
      return null;
    }
    return response;
  }

  public static String str2hex(String str) {
    char[] chars = "0123456789ABCDEF".toCharArray();
    StringBuilder sb = new StringBuilder("");
    byte[] bs = str.getBytes();
    int bit;
    for (int i = 0; i < bs.length; i++) {
      bit = (bs[i] & 0x0f0) >> 4;
      sb.append(chars[bit]);
      bit = bs[i] & 0x0f;
      sb.append(chars[bit]);
      // sb.append(' ');
    }
    return sb.toString().trim();
  }

}