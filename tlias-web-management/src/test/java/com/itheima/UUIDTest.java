package com.itheima;
import org.junit.jupiter.api.Test;
import java.util.UUID;

public class UUIDTest {
    @Test
    public void testUuid(){
        for (int i = 0; i < 100; i++) {
            System.out.println(UUID.randomUUID());
        }//生成UUID 每个都不同
    }
}
