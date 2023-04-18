package com.oppo.cloud.diagnosis;

import com.oppo.cloud.diagnosis.service.impl.RealtimeDiagnosisMetricsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class TestRealtimeDiagnosisMetricsServiceImpl {

    @Autowired
    RealtimeDiagnosisMetricsServiceImpl realtimeDiagnosisMetricsServiceImpl;
    @Test
    public void testRegex(){
        String s = realtimeDiagnosisMetricsServiceImpl.addLabel("abc{} by abc{\"a\":\"b\"}", "key1", "value1");
        log.info(s);
    }
}
