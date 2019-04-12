package common.utils;

import common.utils.crypto.ECKey;
import common.utils.crypto.Hash;
import common.utils.crypto.Sha256Hash;
import common.utils.utils.Base58;
import java.math.BigInteger;
import java.util.Arrays;

public class AddressUtils {

  public String calcBase58CheckAddressFromPrivateKey(String priKeyHex) {
    ECKey eCkey = null;
    //priKeyHex = "cba92a516ea09f620a16ff7ee95ce0df1d56550a8babe9964981a7144c8a784a";
    try {
      BigInteger priK = new BigInteger(priKeyHex, 16);
      eCkey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
      return "";
    }
    byte[] pubKey = eCkey.getPubKey();
    byte[] hash = Hash.sha3(Arrays.copyOfRange(pubKey, 1, pubKey.length));
    byte[] hash_ = Hash.sha3(pubKey);
    byte[] address = eCkey.getAddress();
    address[0] = (byte)65;
    String base58Address = encode58Check(address);
    System.out.println(base58Address);
    return base58Address;
  }

  public static String encode58Check(byte[] input) {
    byte[] hash0 = Sha256Hash.hash(input);
    byte[] hash1 = Sha256Hash.hash(hash0);
    byte[] inputCheck = new byte[input.length + 4];
    System.arraycopy(input, 0, inputCheck, 0, input.length);
    System.arraycopy(hash1, 0, inputCheck, input.length, 4);
    return Base58.encode(inputCheck);
  }

  public static void main(String[] args) {
    System.out.println(new AddressUtils().calcBase58CheckAddressFromPrivateKey("cba92a516ea09f620a16ff7ee95ce0df1d56550a8babe9964981a7144c8a784a"));
  }
}