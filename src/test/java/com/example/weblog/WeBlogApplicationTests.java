package com.example.weblog;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class WeBlogApplicationTests {

    @Test
    void testMD5() {
        String salt = "1a2b3c4d";
        String inputPassword = "123456";
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPassword + salt.charAt(5) + salt.charAt(4);
        System.out.println(DigestUtils.md5Hex(str));
    }

}
