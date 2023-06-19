package com.oppo.cloud.diagnosis;

import com.oppo.cloud.diagnosis.service.impl.FlinkDiagnosisMetricsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestFlinkDiagnosisMetricsServiceImpl {

    @Test
    public void testRegex() {
        FlinkDiagnosisMetricsServiceImpl x = new FlinkDiagnosisMetricsServiceImpl();
        String s = x.addLabel("abc{}", "key1", "value1");
        s = x.addLabel(s, "key2", "value2");
        log.info(s);
    }
}
