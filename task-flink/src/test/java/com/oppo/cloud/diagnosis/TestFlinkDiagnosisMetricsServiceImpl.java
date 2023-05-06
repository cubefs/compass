package com.oppo.cloud.diagnosis;

import com.oppo.cloud.diagnosis.service.impl.FlinkDiagnosisMetricsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class TestFlinkDiagnosisMetricsServiceImpl {

    @Autowired
    FlinkDiagnosisMetricsServiceImpl flinkDiagnosisMetricsServiceImpl;
    @Test
    public void testRegex(){
        String s = flinkDiagnosisMetricsServiceImpl.addLabel("abc{} by abc{\"a\":\"b\"}", "key1", "value1");
        log.info(s);
    }
}
