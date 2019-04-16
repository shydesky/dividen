package core.service;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import common.utils.config.Configuration;
import exceptions.ApiRequestException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
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
import org.apache.log4j.Logger;


public class HttpUtil {

  private static Logger log = Logger.getLogger(HttpUtil.class.getClass());

  public static HttpClient httpClient;

  public static HttpResponse response;
  static Integer connectionTimeout = 2000;
  static Integer soTimeout = 2000;
  static String transactionString;
  static JSONObject responseContent;

  static {
    PoolingClientConnectionManager pccm = new PoolingClientConnectionManager();
    pccm.setDefaultMaxPerRoute(20);
    pccm.setMaxTotal(100);

    httpClient = new DefaultHttpClient(pccm);
  }


  public static HttpResponse createConnect(String url, JsonObject requestBody) {
    HttpResponse response = null;
    HttpPost httppost = null;
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
      return response;
    } catch (Exception e) {
      if (response != null) {
        try {
          EntityUtils.consume(response.getEntity());
        } catch (IOException e1) {

        }
      }
      if (httppost != null) {
        httppost.releaseConnection();
        return null;
      }
      return response;
    }
  }

  public static JSONObject parseResponseContent(HttpResponse response) {
    try {
      String result = EntityUtils.toString(response.getEntity());
      StringEntity entity = new StringEntity(result, Charset.forName("UTF-8"));
      response.setEntity(entity);
      JSONObject obj = JSONObject.parseObject(result);
      return obj;
    } catch (Exception e) {
      return null;
    }
  }

  //handle http request

  //easy transfer coin with private key
  public static HttpResponse easyTransferByPrivate(String httpNode, String privateKey,
      String toAddressHex, Long amount) {
    try {
      final String requestUrl = "http://" + httpNode + "/wallet/easytransferbyprivate";
      JsonObject userBaseObj = new JsonObject();
      userBaseObj.addProperty("privateKey", privateKey);
      userBaseObj.addProperty("toAddress", toAddressHex);
      userBaseObj.addProperty("amount", amount);
      response = createConnect(requestUrl, userBaseObj);
      return response;
    } catch (Exception e) {
      return null;
    }
  }


  public static HttpResponse listVoterOfWitness(String httpNode, String address, String timestamp) {
    HttpGet httpGet = null;
    HttpResponse response = null;
    try {
      URI uri = new URIBuilder()
          .setScheme("http")
          .setHost(httpNode)
          .setPath("/wallet/getwitnessvote")
          .setParameter("address", address)
          .setParameter("round", timestamp)
          .build();
      if (Configuration.getInstance().debug) {
        log.info(uri.toString());
      }
      httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
          connectionTimeout);
      httpGet = new HttpGet(uri);
      response = httpClient.execute(httpGet);
      return response;
    } catch (ApiRequestException e) {
      httpGet.releaseConnection();
      throw e;
    } catch (Exception e) {
      httpGet.releaseConnection();
      return null;
    }
  }

  public static HttpResponse getTransactionByIdFromSolidity(String httpSolidityNode, String txid) {
    try {
      String requestUrl = "http://" + httpSolidityNode + "/walletsolidity/gettransactionbyid";
      JsonObject userBaseObj = new JsonObject();
      userBaseObj.addProperty("value", txid);
      HttpResponse response = createConnect(requestUrl, userBaseObj);
      return response;
    } catch (Exception e) {
      return null;
    }
  }

  public static boolean withdrawBalance(String httpNode, String witnessAddress_hex,
      String fromKey) {
    HttpResponse response;
    try {
      final String requestUrl = "http://" + httpNode + "/wallet/withdrawbalance";
      JsonObject userBaseObj = new JsonObject();
      userBaseObj.addProperty("owner_address", witnessAddress_hex);
      response = createConnect(requestUrl, userBaseObj);

      String transactionString = EntityUtils.toString(response.getEntity());
      if (transactionString != null) {
        if (transactionString.contains("less than 24") || transactionString.contains("Guard")) {
          return false;
        }
      }

      String transactionSignString = gettransactionsign(httpNode, transactionString, fromKey);
      if (transactionSignString == null) {
        return false;
      }

      response = broadcastTransaction(httpNode, transactionSignString);
      JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
      return Boolean.valueOf(jsonObject.getBoolean("result"));
    } catch (Exception e) {
      return false;
    }
  }

  public static String gettransactionsign(String httpNode, String transactionString,
      String privateKey) {
    String transactionSignString;
    try {
      String requestUrl = "http://" + httpNode + "/wallet/gettransactionsign";
      JsonObject userBaseObj = new JsonObject();
      userBaseObj.addProperty("transaction", transactionString);
      userBaseObj.addProperty("privateKey", privateKey);
      response = createConnect(requestUrl, userBaseObj);
      transactionSignString = EntityUtils.toString(response.getEntity());
    } catch (Exception e) {
      return null;
    }
    return transactionSignString;
  }

  public static HttpResponse broadcastTransaction(String httpNode, String transactionSignString) {
    HttpPost httppost;
    try {
      String requestUrl = "http://" + httpNode + "/wallet/broadcasttransaction";
      httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
          connectionTimeout);
      httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);
      httppost = new HttpPost(requestUrl);
      httppost.setHeader("Content-type", "application/json; charset=utf-8");
      httppost.setHeader("Connection", "Close");
      if (transactionSignString != null) {
        StringEntity entity = new StringEntity(transactionSignString, Charset.forName("UTF-8"));
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httppost.setEntity(entity);
      }
      response = httpClient.execute(httppost);
    } catch (Exception e) {
      return null;
    }

    responseContent = HttpUtil.parseResponseContent(response);
    Integer times = 0;

    while (times++ <= 10 && responseContent.getString("code") != null && responseContent
        .getString("code").equalsIgnoreCase("SERVER_BUSY")) {
      try {
        response = httpClient.execute(httppost);
      } catch (Exception e) {
        return null;
      }
      responseContent = HttpUtil.parseResponseContent(response);
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return response;
  }

  public static Long getBalance(String httpNode, String queryAddress_hex) {
    Long balance;
    HttpResponse response;
    try {
      String requestUrl = "http://" + httpNode + "/wallet/getaccount";
      JsonObject userBaseObj = new JsonObject();
      userBaseObj.addProperty("address", queryAddress_hex);
      response = createConnect(requestUrl, userBaseObj);
      responseContent = HttpUtil.parseResponseContent(response);
      balance = Long.parseLong(responseContent.get("balance").toString());
      return balance;
    } catch (Exception e) {
      return null;
    }
  }
}