package io.github.iamnicknack.pi4j.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class PayloadTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void test() {
        var bytes = new byte[] { 1, 2, 3 };
        var str = objectMapper.convertValue(bytes, String.class);
        System.out.println(str);
    }

//    @Test
//    void canCreateRegisterTransfer() {
//        var buffer = new byte[3];
//        var transfer = I2CPayload.forRegister(
//                new byte[] { 1 },
//                buffer,
//                0,
//                3
//        );
//
//        assertEquals(1, transfer.data()[0]);
//        assertEquals(4, transfer.length());
//
//        var noRegisterTransfer = I2CPayload.fromRegister(
//                transfer, 3
//        );
//
//        assertEquals(3, noRegisterTransfer.length());
//    }
}