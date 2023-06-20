package com.oppo.cloud.portal.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.PageHelper;
import com.oppo.cloud.common.api.CommonPage;
import com.oppo.cloud.common.api.CommonStatus;
import com.oppo.cloud.common.domain.flink.enums.DiagnosisRuleHasAdvice;
import com.oppo.cloud.common.domain.flink.enums.FlinkRule;
import com.oppo.cloud.mapper.FlinkTaskDiagnosisMapper;
import com.oppo.cloud.mapper.FlinkTaskDiagnosisRuleAdviceMapper;
import com.oppo.cloud.model.*;
import com.oppo.cloud.portal.dao.FlinkTaskDiagnosisExtendMapper;
import com.oppo.cloud.portal.domain.realtime.*;
import com.oppo.cloud.portal.domain.report.*;
import com.oppo.cloud.portal.domain.task.IndicatorData;
import com.oppo.cloud.portal.service.ElasticSearchService;
import com.oppo.cloud.portal.service.FlinkTaskDiagnosisService;
import com.oppo.cloud.portal.util.HttpUtil;
import com.oppo.cloud.portal.util.UnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 实时任务查询接口
 */
@Service
@Slf4j
public class FlinkTaskDiagnosisServiceImpl implements FlinkTaskDiagnosisService {
    @Value("${custom.flink.diagnosisHost}")
    private String diagnosisHost;
    @Autowired
    FlinkTaskDiagnosisExtendMapper flinkTaskDiagnosisExtendMapper;
    @Autowired
    FlinkTaskDiagnosisMapper flinkTaskDiagnosisMapper;
    @Autowired
    FlinkTaskDiagnosisRuleAdviceMapper flinkTaskDiagnosisRuleAdviceMapper;
    @Autowired
    ElasticSearchService elasticSearchService;

    @Autowired
    HttpUtil httpUtil;

    private void fillRealtimeTaskDiagnosis(FlinkTaskDiagnosis x) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.ofEpochSecond(x.getStartTime().getTime() / 1000, 0, ZoneOffset.ofHours(8));
        Duration between = Duration.between(start, now);
        String timeCosts = UnitUtil.transferTimeUnit(between.getSeconds() * 1000);
        x.setTimeCost(timeCosts);
        String vcoreCosts = UnitUtil.transferVcoreS(between.getSeconds() * ((long) x.getTmCore() * x.getTmNum() + 1));
        x.setResourceCost(vcoreCosts);
        String memCosts = UnitUtil.transferMemGbS(between.getSeconds() * ((long) (float) x.getTmMem() / 1024 * x.getTmNum() + 1));
        x.setMemCost(memCosts);
        String resourceDesc = getResourceAdvice(x);
        x.setResourceAdvice(resourceDesc);
        String dt = x.getDiagnosisTypes();
        JSONArray objects = JSON.parseArray(dt);
        List<String> typesList = new ArrayList<>();
        x.setRuleNames(typesList);
        if (objects != null) {
            List<Integer> dtList = objects.toList(Integer.class);
            for (Integer code : dtList) {
                for (FlinkRule fr : FlinkRule.values()) {
                    if (fr.getCode() == code) {
                        typesList.add(fr.getName());
                    }
                }
            }
        }

    }

    /**
     * 分页查询作业
     *
     * @param req
     * @return
     */
    @Override
    public CommonPage<FlinkTaskDiagnosis> pageJobs(DiagnosisAdviceListReq req) {
        PageHelper.startPage(req.getPage(), req.getPageSize());
        List<FlinkTaskDiagnosis> realtimeTaskDiagnoses = flinkTaskDiagnosisExtendMapper.page(req);
        for (FlinkTaskDiagnosis r : realtimeTaskDiagnoses) {
            fillRealtimeTaskDiagnosis(r);
        }
        return CommonPage.restPage(realtimeTaskDiagnoses);
    }

    private DiagnosisGeneralViewQuery getDiagnosisTime(LocalDateTime startTs, LocalDateTime endTs) {
        DiagnosisGeneralViewQuery query = new DiagnosisGeneralViewQuery();
        query.setStartTs(startTs);
        query.setEndTs(endTs);
        LocalDateTime diagnosisTime = flinkTaskDiagnosisExtendMapper.getDiagnosisTime(query);
        DiagnosisGeneralViewQuery realReq = new DiagnosisGeneralViewQuery();
        realReq.setStartTs(diagnosisTime);
        realReq.setEndTs(diagnosisTime);
        return realReq;
    }

    /**
     * 获取概览数值
     *
     * @param request
     * @return
     */
    @Override
    public DiagnosisGeneralViewNumberResp getGeneralViewNumber(DiagnosisGeneralViewReq request) {
        LocalDateTime startDt = LocalDateTime.ofEpochSecond(request.getStartTs(), 0, ZoneOffset.ofHours(8));
        LocalDateTime endDt = LocalDateTime.ofEpochSecond(request.getEndTs(), 0, ZoneOffset.ofHours(8));
        GeneralViewNumberDto generalViewNumber = flinkTaskDiagnosisExtendMapper
                .getGeneralViewNumber(getDiagnosisTime(startDt, endDt));
        GeneralViewNumberDto generalViewNumberDay1Before = flinkTaskDiagnosisExtendMapper
                .getGeneralViewNumber(getDiagnosisTime(startDt.minusDays(1), endDt.minusDays(1)));
        GeneralViewNumberDto generalViewNumberDay7Before = flinkTaskDiagnosisExtendMapper
                .getGeneralViewNumber(getDiagnosisTime(startDt.minusDays(7), endDt.minusDays(7)));
        DiagnosisGeneralViewNumberResp diagnosisGeneralViewNumberResp = new DiagnosisGeneralViewNumberResp();
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDto(generalViewNumber);
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDtoDay1Before(generalViewNumberDay1Before);
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDtoDay7Before(generalViewNumberDay7Before);
        if (generalViewNumber.getBaseTaskCntSum() == 0) {
            diagnosisGeneralViewNumberResp.setAbnormalJobNumRatio(0f);
        } else {
            diagnosisGeneralViewNumberResp.setAbnormalJobNumRatio(
                    (float) generalViewNumber.getExceptionTaskCntSum() /
                            generalViewNumber.getBaseTaskCntSum());
        }

        if (generalViewNumberDay7Before.getExceptionTaskCntSum() == 0) {
            diagnosisGeneralViewNumberResp.setAbnormalJobNumChainRatio(0f);
        } else {
            diagnosisGeneralViewNumberResp.setAbnormalJobNumChainRatio(
                    (float) (generalViewNumber.getExceptionTaskCntSum() - generalViewNumberDay7Before.getExceptionTaskCntSum())
                            / generalViewNumberDay7Before.getExceptionTaskCntSum());
        }
        if (generalViewNumberDay1Before.getExceptionTaskCntSum() == 0) {
            diagnosisGeneralViewNumberResp.setAbnormalJobNumDayOnDay(0f);
        } else {
            diagnosisGeneralViewNumberResp.setAbnormalJobNumDayOnDay(
                    (float) (generalViewNumber.getExceptionTaskCntSum() - generalViewNumberDay1Before.getExceptionTaskCntSum())
                            / generalViewNumberDay1Before.getExceptionTaskCntSum());
        }
        if (generalViewNumber.getBaseTaskCntSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceJobNumRatio(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceJobNumRatio(
                    (float) (generalViewNumber.getResourceTaskCntSum())
                            / generalViewNumber.getBaseTaskCntSum());
        }
        if (generalViewNumberDay7Before.getResourceTaskCntSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceJobNumChainRatio(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceJobNumChainRatio(
                    (float) (generalViewNumber.getResourceTaskCntSum() - generalViewNumberDay7Before.getResourceTaskCntSum())
                            / generalViewNumberDay7Before.getResourceTaskCntSum());
        }
        if (generalViewNumberDay1Before.getResourceTaskCntSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceJobNumDayOnDay(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceJobNumDayOnDay(
                    (float) (generalViewNumber.getResourceTaskCntSum() - generalViewNumberDay1Before.getResourceTaskCntSum())
                            / generalViewNumberDay1Before.getResourceTaskCntSum());
        }
        if (generalViewNumber.getTotalCoreNumSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceCpuNumRatio(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceCpuNumRatio((float) generalViewNumber.getCutCoreNumSum() /
                    generalViewNumber.getTotalCoreNumSum());
        }
        if (generalViewNumberDay7Before.getCutCoreNumSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceCpuNumChainRatio(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceCpuNumChainRatio(
                    (float) (generalViewNumber.getCutCoreNumSum() - generalViewNumberDay7Before.getCutCoreNumSum()) /
                            generalViewNumberDay7Before.getCutCoreNumSum());
        }
        if (generalViewNumberDay1Before.getCutCoreNumSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceCpuNumDayOnDay(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceCpuNumDayOnDay(
                    (float) (generalViewNumber.getCutCoreNumSum() - generalViewNumberDay1Before.getCutCoreNumSum()) /
                            generalViewNumberDay1Before.getCutCoreNumSum());
        }
        if (generalViewNumber.getTotalMemNumSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceMemoryNumRatio(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceMemoryNumRatio(
                    (float) generalViewNumber.getCutMemNumSum() /
                            generalViewNumber.getTotalMemNumSum());
        }
        if (generalViewNumberDay7Before.getCutMemNumSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceMemoryNumChainRatio(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceMemoryNumChainRatio(
                    (float) (generalViewNumber.getCutMemNumSum() - generalViewNumberDay7Before.getCutMemNumSum()) /
                            generalViewNumberDay7Before.getCutMemNumSum());
        }
        if (generalViewNumberDay1Before.getCutMemNumSum() == 0) {
            diagnosisGeneralViewNumberResp.setResourceMemoryNumDayOnDay(0f);
        } else {
            diagnosisGeneralViewNumberResp.setResourceMemoryNumDayOnDay(
                    (float) (generalViewNumber.getCutMemNumSum() - generalViewNumberDay1Before.getCutMemNumSum()) /
                            generalViewNumberDay1Before.getCutMemNumSum());
        }
        if (diagnosisGeneralViewNumberResp.getGeneralViewNumberDto().getCutMemNumSum() / 1024 >= 10
                && diagnosisGeneralViewNumberResp.getGeneralViewNumberDto().getTotalMemNumSum() / 1024 >= 10) {
            diagnosisGeneralViewNumberResp.getGeneralViewNumberDto().setCutMemNumSum(
                    diagnosisGeneralViewNumberResp.getGeneralViewNumberDto().getCutMemNumSum() / 1024
            );
            diagnosisGeneralViewNumberResp.getGeneralViewNumberDto().setTotalMemNumSum(
                    diagnosisGeneralViewNumberResp.getGeneralViewNumberDto().getTotalMemNumSum() / 1024
            );
            diagnosisGeneralViewNumberResp.setMemoryUnit("GB");
        }
        return diagnosisGeneralViewNumberResp;
    }

    public List<LocalDateTime> getDiagnosisEndTimes(DiagnosisGeneralViewQuery req) {
        List<LocalDateTime> diagnosisDates = flinkTaskDiagnosisExtendMapper.getDiagnosisDates(req);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, LocalDateTime> dtMap = new HashMap<>();
        diagnosisDates.forEach(x -> {
            String dt = x.format(dateTimeFormatter);
            if (dtMap.containsKey(dt)) {
                LocalDateTime old = dtMap.get(dt);
                if (old.isAfter(x)) {

                } else {
                    dtMap.put(dt, x);
                }
            } else {
                dtMap.put(dt, x);
            }
        });
        return dtMap.values().stream().collect(Collectors.toList());
    }

    /**
     * 获取概览趋势
     *
     * @param request
     * @return
     */
    @Override
    public DiagnosisGeneralViewTrendResp getGeneralViewTrend(DiagnosisGeneralViewReq request) {
        DiagnosisGeneralViewQuery query = new DiagnosisGeneralViewQuery();
        Long startTs, endTs;
        if (request.getStartTs() == null || request.getEndTs() == null) {
            endTs = System.currentTimeMillis() / 1000;
            startTs = LocalDateTime.now().minus(30, ChronoUnit.DAYS).toEpochSecond(ZoneOffset.ofHours(8));
        } else {
            startTs = request.getStartTs();
            endTs = request.getEndTs();
        }
        query.setStartTs(LocalDateTime.ofEpochSecond(startTs, 0, ZoneOffset.ofHours(8)));
        query.setEndTs(LocalDateTime.ofEpochSecond(endTs, 0, ZoneOffset.ofHours(8)));
        List<LocalDateTime> diagnosisEndTimes = getDiagnosisEndTimes(query);
        DiagnosisGeneralViewTrendResp diagnosisGeneralViewTrendResp = new DiagnosisGeneralViewTrendResp();
        if (diagnosisEndTimes == null || diagnosisEndTimes.size() == 0) {
            return null;
        }
        List<GeneralViewNumberDto> generalViewTrend = flinkTaskDiagnosisExtendMapper.getGeneralViewTrend(diagnosisEndTimes);
        diagnosisGeneralViewTrendResp.setTrend(generalViewTrend);
        TrendGraph cpuTrend = new TrendGraph();
        cpuTrend.setName("CPU消耗趋势");
        cpuTrend.setUnit("core");
        LineGraph cpuDecrLine = new LineGraph();
        cpuDecrLine.setName("可优化CPU数");
        List<IndicatorData> cpuDecrList = generalViewTrend.stream().map(x -> {
            IndicatorData indicatorData = new IndicatorData();
            indicatorData.setDate(x.getDate());
            indicatorData.setCount(x.getCutCoreNumSum());
            return indicatorData;
        }).collect(Collectors.toList());
        cpuDecrLine.setData(cpuDecrList);
        cpuTrend.setJobUsage(cpuDecrLine);
        LineGraph cpuTotalLine = new LineGraph();
        cpuTotalLine.setName("总CPU消耗数");
        List<IndicatorData> cpuTotalList = generalViewTrend.stream().map(x -> {
            IndicatorData indicatorData = new IndicatorData();
            indicatorData.setDate(x.getDate());
            indicatorData.setCount(x.getTotalCoreNumSum());
            return indicatorData;
        }).collect(Collectors.toList());
        cpuTotalLine.setData(cpuTotalList);
        cpuTrend.setTotalUsage(cpuTotalLine);

        TrendGraph memTrend = new TrendGraph();
        memTrend.setName("内存消耗趋势");
        memTrend.setUnit("GB");
        LineGraph memoryDecrLine = new LineGraph();
        memoryDecrLine.setName("可优化内存数");
        List<IndicatorData> memoryDecrList = generalViewTrend.stream().map(x -> {
            IndicatorData indicatorData = new IndicatorData();
            indicatorData.setDate(x.getDate());
            indicatorData.setCount(x.getCutMemNumSum() / 1024f);
            return indicatorData;
        }).collect(Collectors.toList());
        memoryDecrLine.setData(memoryDecrList);
        memTrend.setJobUsage(memoryDecrLine);
        LineGraph memoryTotalLine = new LineGraph();
        memoryTotalLine.setName("总内存消耗数");
        List<IndicatorData> memoryTotalList = generalViewTrend.stream().map(x -> {
            IndicatorData indicatorData = new IndicatorData();
            indicatorData.setDate(x.getDate());
            indicatorData.setCount(x.getTotalMemNumSum() / 1024f);
            return indicatorData;
        }).collect(Collectors.toList());
        memoryTotalLine.setData(memoryTotalList);
        memTrend.setTotalUsage(memoryTotalLine);

        TrendGraph jobNumTrend = new TrendGraph();
        jobNumTrend.setName("异常任务数趋势");
        jobNumTrend.setUnit("个");
        LineGraph exceptionNumberLine = new LineGraph();
        exceptionNumberLine.setName("异常任务数");
        List<IndicatorData> exceptionNumberList = generalViewTrend.stream().map(x -> {
            IndicatorData indicatorData = new IndicatorData();
            indicatorData.setDate(x.getDate());
            indicatorData.setCount(x.getExceptionTaskCntSum());
            return indicatorData;
        }).collect(Collectors.toList());
        exceptionNumberLine.setData(exceptionNumberList);
        jobNumTrend.setJobUsage(exceptionNumberLine);
        LineGraph jobNumberTotalLine = new LineGraph();
        jobNumberTotalLine.setName("总任务数");
        List<IndicatorData> jobNumberTotalList = generalViewTrend.stream().map(x -> {
            IndicatorData indicatorData = new IndicatorData();
            indicatorData.setDate(x.getDate());
            indicatorData.setCount(x.getBaseTaskCntSum());
            return indicatorData;
        }).collect(Collectors.toList());
        jobNumberTotalLine.setData(jobNumberTotalList);
        jobNumTrend.setTotalUsage(jobNumberTotalLine);

        diagnosisGeneralViewTrendResp.setCpuTrend(cpuTrend);
        diagnosisGeneralViewTrendResp.setMemoryTrend(memTrend);
        diagnosisGeneralViewTrendResp.setJobNumberTrend(jobNumTrend);
        return diagnosisGeneralViewTrendResp;
    }

    /**
     * 获取概览分布
     *
     * @param request
     * @return
     */
    @Override
    public DiagnosisGeneralVIewDistributeResp getGeneralViewDistribute(DiagnosisGeneralViewReq request) {
        DiagnosisGeneralViewQuery query = new DiagnosisGeneralViewQuery();
        query.setStartTs(LocalDateTime.ofEpochSecond(request.getStartTs(), 0, ZoneOffset.ofHours(8)));
        query.setEndTs(LocalDateTime.ofEpochSecond(request.getEndTs(), 0, ZoneOffset.ofHours(8)));
        List<Date> diagnosisEndTimes = getDiagnosisEndTimes(query).stream()
                .map(x -> new Date(x.toEpochSecond(ZoneOffset.ofHours(8)) * 1000)).collect(Collectors.toList());
        FlinkTaskDiagnosisExample realtimeTaskAppExample = new FlinkTaskDiagnosisExample();
        realtimeTaskAppExample.createCriteria()
                .andDiagnosisEndTimeIn(diagnosisEndTimes)
        ;
        long count = flinkTaskDiagnosisExtendMapper.countByExample(realtimeTaskAppExample);
        int pageSize = 100;
        int pageNum = (int) Math.ceil((double) count / pageSize);
        Map<String, Long> memDistribute = new HashMap<>();
        Map<String, Long> cpuDistribute = new HashMap<>();
        Map<String, Long> numDistribute = new HashMap<>();
        for (int i = 1; i <= pageNum; i++) {
            PageHelper.startPage(i, pageSize);
            try {
                List<FlinkTaskDiagnosis> realtimeTaskApps = flinkTaskDiagnosisExtendMapper.selectByExample(realtimeTaskAppExample);
                for (FlinkTaskDiagnosis flinkTaskDiagnosis : realtimeTaskApps) {
                    int costCores = flinkTaskDiagnosis.getTmCore() * flinkTaskDiagnosis.getTmNum();
                    int costMem = flinkTaskDiagnosis.getTmMem() * flinkTaskDiagnosis.getTmNum();
                    String diagnosisTypes = flinkTaskDiagnosis.getDiagnosisTypes();
                    List<Integer> diagnosisTypesList = JSON.parseArray(diagnosisTypes, Integer.class);
                    for (Integer t : diagnosisTypesList) {
                        FlinkRule flinkRule = FlinkRule.valueOf(t);
                        String key = flinkRule.getName();
                        numDistribute.put(key, numDistribute.getOrDefault(key, 0L) + 1);
                        memDistribute.put(key, memDistribute.getOrDefault(key, 0L) + costMem);
                        cpuDistribute.put(key, cpuDistribute.getOrDefault(key, 0L) + costCores);
                    }
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
        DiagnosisGeneralVIewDistributeResp diagnosisGeneralVIewDistributeResp = new DiagnosisGeneralVIewDistributeResp();
        diagnosisGeneralVIewDistributeResp.setCpuDistribute(cpuDistribute);
        DistributionGraph cpuDistributionGraph = new DistributionGraph();
        cpuDistributionGraph.setName("CPU资源消耗分布");
        List<DistributionData> cpuData = new ArrayList<>();
        cpuDistribute.forEach((key, value) -> {
            DistributionData d = new DistributionData();
            d.setName(key);
            d.setValue((double) value);
            cpuData.add(d);
        });
        cpuDistributionGraph.setData(cpuData);
        diagnosisGeneralVIewDistributeResp.setCpu(cpuDistributionGraph);

        diagnosisGeneralVIewDistributeResp.setMemDistribute(memDistribute);
        DistributionGraph memDistributionGraph = new DistributionGraph();
        memDistributionGraph.setName("内存资源消耗分布");
        List<DistributionData> memData = new ArrayList<>();
        memDistribute.forEach((key, value) -> {
            DistributionData d = new DistributionData();
            d.setName(key);
            d.setValue((double) value);
            memData.add(d);
        });
        memDistributionGraph.setData(memData);
        diagnosisGeneralVIewDistributeResp.setMem(memDistributionGraph);
        diagnosisGeneralVIewDistributeResp.setTaskNumDistribute(numDistribute);
        DistributionGraph numDistributionGraph = new DistributionGraph();
        numDistributionGraph.setName("任务数分布");
        List<DistributionData> numData = new ArrayList<>();
        numDistribute.forEach((key, value) -> {
            DistributionData d = new DistributionData();
            d.setName(key);
            d.setValue((double) value);
            numData.add(d);
        });
        numDistributionGraph.setData(numData);
        diagnosisGeneralVIewDistributeResp.setNum(numDistributionGraph);
        return diagnosisGeneralVIewDistributeResp;
    }

    public String getResourceAdvice(FlinkTaskDiagnosis request) {
        DiagnosisReportResp diagnosisReportResp = new DiagnosisReportResp();
        List<String> reports = new ArrayList<>();
        diagnosisReportResp.setReports(reports);
        FlinkTaskDiagnosis flinkTaskDiagnosis = flinkTaskDiagnosisMapper.selectByPrimaryKey(request.getId());
        diagnosisReportResp.setFlinkTaskDiagnosis(flinkTaskDiagnosis);
        FlinkTaskDiagnosisRuleAdviceExample example = new FlinkTaskDiagnosisRuleAdviceExample();
        example.createCriteria().andFlinkTaskDiagnosisIdEqualTo(request.getId());
        List<FlinkTaskDiagnosisRuleAdvice> flinkTaskDiagnosisRuleAdvices = flinkTaskDiagnosisRuleAdviceMapper.selectByExample(example);
        // 查询诊断建议
        List<String> descs = new ArrayList<>();
        for (FlinkTaskDiagnosisRuleAdvice advice : flinkTaskDiagnosisRuleAdvices) {
            try {
                if (advice.getHasAdvice() == DiagnosisRuleHasAdvice.HAS_ADVICE.getCode()) {
                    descs.add(advice.getDescription());
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }

        StringBuilder sb = new StringBuilder();
        if (!Objects.equals(flinkTaskDiagnosis.getDiagnosisParallel(), flinkTaskDiagnosis.getParallel())) {
            sb.append("作业并行度:").append(flinkTaskDiagnosis.getParallel()).append("->").append(flinkTaskDiagnosis.getDiagnosisParallel()).append(";");
        }
        if (!Objects.equals(flinkTaskDiagnosis.getDiagnosisTmSlotNum(), flinkTaskDiagnosis.getTmSlot())) {
            sb.append("作业TM的Slot数:").append(flinkTaskDiagnosis.getTmSlot()).append("->").append(flinkTaskDiagnosis.getDiagnosisTmSlotNum()).append(";");
        }
        if (!Objects.equals(flinkTaskDiagnosis.getDiagnosisTmCoreNum(), flinkTaskDiagnosis.getTmCore())) {
            sb.append("作业TM的Core数:").append(flinkTaskDiagnosis.getTmCore()).append("->").append(flinkTaskDiagnosis.getDiagnosisTmCoreNum()).append(";");
        }
        if (!Objects.equals(flinkTaskDiagnosis.getDiagnosisTmMemSize(), flinkTaskDiagnosis.getTmMem())) {
            sb.append("作业TM的内存:").append(flinkTaskDiagnosis.getTmMem() + "MB").append("->").append(flinkTaskDiagnosis.getDiagnosisTmMemSize() + "MB").append(";");
        }
        if (!Objects.equals(flinkTaskDiagnosis.getDiagnosisJmMemSize(), flinkTaskDiagnosis.getJmMem())) {
            sb.append("作业JM的内存:").append(flinkTaskDiagnosis.getJmMem() + "MB").append("->").append(flinkTaskDiagnosis.getDiagnosisJmMemSize() + "MB").append(";");
        }
        descs.add(sb.toString());
        return String.join("\n", descs);
    }

    /**
     * 获取诊断报告
     *
     * @param request
     * @return
     */
    @Override
    public DiagnosisReportResp getReport(FlinkTaskDiagnosis request) {
        DiagnosisReportResp diagnosisReportResp = new DiagnosisReportResp();
        List<String> reports = new ArrayList<>();
        diagnosisReportResp.setReports(reports);
        FlinkTaskDiagnosis flinkTaskDiagnosis = flinkTaskDiagnosisMapper.selectByPrimaryKey(request.getId());
        fillRealtimeTaskDiagnosis(flinkTaskDiagnosis);
        diagnosisReportResp.setFlinkTaskDiagnosis(flinkTaskDiagnosis);
        FlinkTaskDiagnosisRuleAdviceExample example = new FlinkTaskDiagnosisRuleAdviceExample();
        example.createCriteria().andFlinkTaskDiagnosisIdEqualTo(request.getId());
        List<FlinkTaskDiagnosisRuleAdvice> flinkTaskDiagnosisRuleAdvices = flinkTaskDiagnosisRuleAdviceMapper.selectByExample(example);
        String esIndex = Constant.REALTIME_DIAGNOSIS_REPORT_ES_INDEX_PREFIX;
        // 查询诊断建议
        for (FlinkTaskDiagnosisRuleAdvice advice : flinkTaskDiagnosisRuleAdvices) {
            try {
                if (advice.getHasAdvice() == DiagnosisRuleHasAdvice.HAS_ADVICE.getCode()) {
                    HashMap<String, Object> termQuery = new HashMap<>();
                    termQuery.put("doc_id", advice.getId());
                    Map<String, SortOrder> sort = null;
                    Map<String, Object[]> rangeConditions = null;
                    SearchSourceBuilder builder = elasticSearchService.genSearchBuilder(termQuery, rangeConditions, sort, null);
                    SearchHits searchHits = elasticSearchService.find(builder, esIndex);
                    if (searchHits.getHits().length == 0) {
                        log.error("Can't find report {}", advice);
                    }
                    for (SearchHit hit : searchHits) {
                        log.info(hit.toString());
                        String report = hit.getSourceAsMap().get("report").toString();
                        reports.add(report);
                    }

                }

            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
        log.debug("报告 {}", reports.toString());
        log.debug("整体 {}", diagnosisReportResp);
        return diagnosisReportResp;
    }

    public CommonStatus<OneClickDiagnosisResponse> diagnosis(OneClickDiagnosisRequest req) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("appId", req.getAppId());
            body.put("end", req.getEnd());
            body.put("start", req.getStart());
            log.debug("请求地址 {}/ostream/diagnosis {}", diagnosisHost, body);
            String res = httpUtil.post(diagnosisHost + "/ostream/diagnosis", body);
            JSONObject jsonObject = JSON.parseObject(res);
            if (jsonObject.get("status") != null && jsonObject.getInteger("status").equals(200)) {
                OneClickDiagnosisResponse result = new OneClickDiagnosisResponse();
                result.setStatus("succeed");
                result.setFlinkTaskDiagnosis(jsonObject.getObject("data", FlinkTaskDiagnosis.class));
                return CommonStatus.success(result);
            } else {
                return CommonStatus.failed(jsonObject.getString("msg"));
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return CommonStatus.failed("未知错误");
        }

    }

    public CommonStatus<FlinkTaskDiagnosis> updateStatus(FlinkTaskDiagnosis flinkTaskDiagnosis) {
        if (flinkTaskDiagnosis.getId() == null) {
            return CommonStatus.failed("id为空");
        }
        FlinkTaskDiagnosis flinkTaskDiagnosisSelect = flinkTaskDiagnosisMapper.selectByPrimaryKey(flinkTaskDiagnosis.getId());
        if (flinkTaskDiagnosisSelect == null) {
            return CommonStatus.failed("查不到该数据");
        }
        flinkTaskDiagnosisSelect.setProcessState(flinkTaskDiagnosis.getProcessState());
        flinkTaskDiagnosisMapper.updateByPrimaryKey(flinkTaskDiagnosisSelect);
        return CommonStatus.success(flinkTaskDiagnosisSelect);
    }
}
