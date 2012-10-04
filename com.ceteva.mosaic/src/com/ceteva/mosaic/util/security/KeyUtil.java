/*
 * @(#)KeyUtil.java  1.00
 *
 * Copyright WEBsina (c)2001  All Rights Reserved.
 *
 */
package com.ceteva.mosaic.util.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * This is a utility class encode and decode keys.
 *
 * @author Samuel Chen
 * @version $Revision: 1.1 $
 * created:  2-10-2001
 * @since 1.0 
 */
public class KeyUtil {

  private static KeyPairGenerator kpg;
  private static KeyFactory kf;
  static {
    try {
      kpg = KeyPairGenerator.getInstance("DSA");
      SecureRandom sr = new SecureRandom();
      kpg.initialize(1024, new SecureRandom(sr.generateSeed(8)));
      kf = KeyFactory.getInstance("DSA"); 
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * The method gets the public key from the encoded byte.
   * The bytes can be recovered from a Hex string saved in a file etc.
   * @param encodedKey the encoded public key in bytes.
   */
  public static PublicKey getPublic(byte[] encodedKey) 
    throws InvalidKeySpecException {
    return kf.generatePublic(new X509EncodedKeySpec(encodedKey));
  }

  /**
   * This method gets the private key from the encoded byte.
   * The bytes can be recovered from a Hex string saved in a file etc.
   * @param encodedKey the encoded private key in bytes.
   */
  public static PrivateKey getPrivate(byte[] encodedKey) 
    throws InvalidKeySpecException {
    return kf.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
  }

  /**
   * The method gets the key pair.
   * @return a pair of keys
   */
  public static KeyPair getKeyPair() {
    return kpg.genKeyPair();
  }

}



