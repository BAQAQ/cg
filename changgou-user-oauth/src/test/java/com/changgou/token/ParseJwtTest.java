package com.changgou.token;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/*****
 * @Author: shenkunlin
 * @Date: 2019/7/7 13:48
 * @Description: com.changgou.token
 *  使用公钥解密令牌数据
 ****/
public class ParseJwtTest {

    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJpdGhlaW1hIiwiaWQiOiIxIn0.VjD1bq6yc2UvvZYK_igJvrVknCPcWoE2y1cPcEAOZV2MD8tbZSL3WyHyQk_suxvMBn2CkoknJk3u7VbpMsYlRdYNJMhDLD1f3wCzdCAi4ais3pvMeLmXJ1dwozYEegdXIQDPsttVqX_6XM9fWb96qODmNZVRGWG2ltvC0GG69Ukv34HinRFtUmgNoT8Cf2wxXLZXI7rBH94ycxDi6G4y9PbV62xryBD1b1ODHQzrT2dgr3MQiSr7fu_SI4UbLKBP5B9g67z1PhjAuPPhZIZ2rlxxQPq_JkdaYSnBTzLQSx6NhnsPlAF-mrYOeLg2WpGJT34mRfwHhm4FPbBvBrqujw";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkU1q1nDRJmCtNB2kKtVf76Omd+xBH4tQFrvWzM8aWfRL5BDLw/KZrLRzca2FCH+Z7Rcw3orpLaNBTM3DvsD9jtDXpegCLmPZLl6kK2DDAiKOc1KUaXMkWLC3WO4BGcLSR+2QWIjpnUTRJjVKUsq2JNnuAb5JdbA/pDeI6tTjbhnhBgtiDJXfAlcobUWz89Cx9E4BAO+dWGPEe+eBESmD0CDU9GC5+ILLtq95Lg8+6tqTry8UukBByHOqYyucYzXz0537NRM9o201AHrb37Ozc20Z8yvhqD4LaB+f6vE2XF38I/NFkFKzImYbaFmkxhugw5EFlMOW5AxnxBxhKebmYQIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
