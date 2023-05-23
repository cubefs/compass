package com.oppo.cloud.portal.domain.realtime;

import com.oppo.cloud.portal.domain.report.ReportGraph;
import com.oppo.cloud.portal.domain.report.TrendGraph;
import lombok.Data;

import java.util.List;

@Data
public class DiagnosisGeneralViewTrendResp {
    List<GeneralViewNumberDto> trend;
    TrendGraph cpuTrend;
    TrendGraph memoryTrend;
    TrendGraph jobNumberTrend;
}
