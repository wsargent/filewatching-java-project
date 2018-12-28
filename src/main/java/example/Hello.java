package example;
import com.tersesystems.securitybuilder.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.util.Collections;

public class Hello {
    public static void main(String[] args) throws Exception {
        KeyPairCreator.FinalStage<RSAKeyPair> keyPairCreator = KeyPairCreator.creator().withRSA().withKeySize(2048);
        RSAKeyPair rootKeyPair = keyPairCreator.create();
        RSAKeyPair intermediateKeyPair = keyPairCreator.create();
        RSAKeyPair eePair = keyPairCreator.create();

        X509CertificateCreator.IssuerStage<RSAPrivateKey> creator =
                X509CertificateCreator.creator().withSHA256withRSA().withDuration(Duration.ofDays(365));

        String issuer = "CN=letsencrypt.derp,O=Root CA";
        X509Certificate[] chain =
                creator
                        .withRootCA(issuer, rootKeyPair, 2)
                        .chain(
                                rootKeyPair.getPrivate(),
                                rootCreator ->
                                        rootCreator
                                                .withPublicKey(intermediateKeyPair.getPublic())
                                                .withSubject("OU=intermediate CA")
                                                .withCertificateAuthorityExtensions(0)
                                                .chain(
                                                        intermediateKeyPair.getPrivate(),
                                                        intCreator ->
                                                                intCreator
                                                                        .withPublicKey(eePair.getPublic())
                                                                        .withSubject("CN=tersesystems.com")
                                                                        .withEndEntityExtensions()
                                                                        .chain()))
                        .create();

        PrivateKeyStore privateKeyStore =
                PrivateKeyStore.create("tersesystems.com", eePair.getPrivate(), chain);
        TrustStore trustStore = TrustStore.create(Collections.singletonList(chain[2]), cert -> "letsencrypt.derp");

        X509ExtendedKeyManager keyManager = KeyManagerBuilder.builder()
                .withSunX509()
                .withPrivateKeyStore(privateKeyStore)
                .build();

        X509ExtendedTrustManager trustManager = TrustManagerBuilder.builder()
                .withDefaultAlgorithm()
                .withTrustStore(trustStore)
                .build();

        System.out.println("keyManager = " + keyManager);
        System.out.println("trustManager = " + trustManager);

        SSLContext sslContext =
                SSLContextBuilder.builder()
                        .withTLS()
                        .withKeyManager(keyManager)
                        .withTrustManager(trustManager)
                        .build();

        System.out.println("sslContext = sslContext");
    }
}