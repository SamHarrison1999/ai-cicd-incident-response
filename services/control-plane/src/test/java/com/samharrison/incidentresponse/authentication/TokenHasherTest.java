package com.samharrison.incidentresponse.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenHasherTest {

  private final TokenHasher tokenHasher = new TokenHasher();

  @Test
  void producesStableLowercaseSha256Hex() {
    assertThat(tokenHasher.hash("refresh-token"))
        .isEqualTo("0eb17643d4e9261163783a420859c92c7d212fa9624106a12b510afbec266120");
  }

  @Test
  void doesNotReturnTheOriginalToken() {
    assertThat(tokenHasher.hash("refresh-token")).doesNotContain("refresh-token");
  }
}
