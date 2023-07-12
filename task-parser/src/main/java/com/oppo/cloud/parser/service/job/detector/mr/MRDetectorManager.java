package com.oppo.cloud.parser.service.job.detector.mr;

import com.oppo.cloud.common.domain.eventlog.DetectorResult;
import com.oppo.cloud.common.domain.eventlog.DetectorStorage;
import com.oppo.cloud.common.domain.eventlog.config.DetectorConfig;
import com.oppo.cloud.parser.domain.job.MRDetectorParam;
import com.oppo.cloud.parser.service.job.detector.IDetector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MRDetectorManager {

    private final MRDetectorParam param;

    private final DetectorConfig config;

    public MRDetectorManager(MRDetectorParam param) {
        this.param = param;
        this.config = param.getConfig();
    }

    private List<IDetector> createDetectors() {
        List<IDetector> detectors = new ArrayList<>();
        detectors.add(new MRLargeTableScanDetector(param));
        detectors.add(new MRMemoryWasteDetector(param));
        detectors.add(new MRDataSkewDetector(param));
        return detectors;
    }

    public DetectorStorage run() {
        List<IDetector> detectors = createDetectors();
        DetectorStorage detectorStorage = new DetectorStorage(
                this.param.getFlowName(), this.param.getProjectName(),
                this.param.getTaskName(), this.param.getExecutionTime(),
                this.param.getTryNumber(), this.param.getAppId(),
                this.param.getLogPath(), this.config);

        for (IDetector detector : detectors) {
            DetectorResult result;
            try {
                result = detector.detect();
            } catch (Exception e) {
                log.error("Exception:", e);
                continue;
            }
            if (result == null) {
                continue;
            }
            if (result.getAbnormal()) {
                detectorStorage.setAbnormal(true);
                log.info("DetectorResult:{},{}", this.param.getAppId(), result.getAppCategory());
            }
            detectorStorage.addDetectorResult(result);
        }

        return detectorStorage;
    }
}
