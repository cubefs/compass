package com.oppo.cloud.gpt.config;

import com.oppo.cloud.gpt.drain.MaskRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DrainConfigTest {

    @Autowired
    private DrainConfig drainConfig;

    @Test
    public void testMaskRules() {
        for (MaskRule rule : drainConfig.getMaskRules()) {
            System.out.println(rule);
        }
        Assertions.assertFalse(drainConfig.getMaskRules().isEmpty());
        Assertions.assertNotNull(drainConfig.getMaskPrefix());
        Assertions.assertNotNull(drainConfig.getMaskSuffix());
    }
}
