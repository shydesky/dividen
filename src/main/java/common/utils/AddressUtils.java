package common.utils;

import common.utils.crypto.ECKey;
import common.utils.crypto.Sha256Hash;
import common.utils.utils.Base58;
import java.math.BigInteger;

public class AddressUtils {

  public static byte[] calcByteAddressFromPrivateKey(String priKeyHex) {
    ECKey eCkey = null;
    try {
      BigInteger priK = new BigInteger(priKeyHex, 16);
      eCkey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      return null;
    }
    byte[] address = eCkey.getAddress();
    address[0] = 65; //0x41
    return address;
  }

  public static String calcHexAddressFromPrivateKey(String priKeyHex) {
    byte[] byteAddress = calcByteAddressFromPrivateKey(priKeyHex);
    return ByteArray.toHexString(byteAddress);
  }

  public static String calcBase58CheckAddressFromPrivateKey(String priKeyHex) {
    byte[] byteAddress = calcByteAddressFromPrivateKey(priKeyHex);
    String base58Address = encode58Check(byteAddress);
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
}