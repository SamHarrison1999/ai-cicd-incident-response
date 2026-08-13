package com.samharrison.incidentresponse.ingestion;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class WebhookSignatureService {

  private static final String PREFIX = "CICD-WEBHOOK-V1\n";
  private static final String SIGNATURE_PREFIX = "sha256=";

  public String calculateSignature(
      String deliveryId,
      String eventType,
      String deliveryTimestamp,
      byte[] payload,
      byte[] secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return SIGNATURE_PREFIX
          + HexFormat.of()
              .formatHex(
                  mac.doFinal(signingInput(deliveryId, eventType, deliveryTimestamp, payload)));
    } catch (Exception exception) {
      throw new IllegalStateException("HMAC-SHA-256 is unavailable.", exception);
    }
  }

  public boolean verify(
      String deliveryId,
      String eventType,
      String deliveryTimestamp,
      byte[] payload,
      byte[] secret,
      String suppliedSignature) {
    byte[] expected =
        decodeSignature(
            calculateSignature(deliveryId, eventType, deliveryTimestamp, payload, secret));
    byte[] supplied = decodeSignature(suppliedSignature);
    return MessageDigest.isEqual(expected, supplied);
  }

  public boolean hasValidFormat(String signature) {
    return signature != null && signature.matches("sha256=[0-9a-f]{64}");
  }

  public String sha256Hex(byte[] payload) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(payload));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable.", exception);
    }
  }

  private byte[] signingInput(
      String deliveryId, String eventType, String deliveryTimestamp, byte[] payload) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    output.writeBytes(PREFIX.getBytes(StandardCharsets.UTF_8));
    output.writeBytes(deliveryId.getBytes(StandardCharsets.UTF_8));
    output.write('\n');
    output.writeBytes(eventType.getBytes(StandardCharsets.UTF_8));
    output.write('\n');
    output.writeBytes(deliveryTimestamp.getBytes(StandardCharsets.UTF_8));
    output.write('\n');
    output.writeBytes(payload);
    return output.toByteArray();
  }

  private byte[] decodeSignature(String signature) {
    return HexFormat.of().parseHex(signature.substring(SIGNATURE_PREFIX.length()));
  }
}
