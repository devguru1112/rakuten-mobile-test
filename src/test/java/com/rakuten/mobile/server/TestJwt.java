package com.rakuten.mobile.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;
import java.util.List;

public final class TestJwt {
    private TestJwt() {}

    public static String hmacToken(String secret, String issuer, String subject, String tenant, List<String> roles) {
        Algorithm alg = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(subject)
                .withClaim("tenant", tenant)
                .withArrayClaim("roles", roles.toArray(String[]::new))
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600_000))
                .sign(alg);
    }
}
