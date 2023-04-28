package com.oppo.cloud.portal.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.github.pagehelper.PageHelper;
import com.oppo.cloud.common.api.CommonPage;
import com.oppo.cloud.common.domain.cluster.yarn.YarnApp;
import com.oppo.cloud.common.domain.flink.enums.EDiagnosisRuleHasAdvice;
import com.oppo.cloud.common.domain.flink.enums.ERealtimeTaskAppState;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReport;
import com.oppo.cloud.common.domain.flink.report.DiagnosisRuleReportDoc;
import com.oppo.cloud.mapper.RealtimeTaskDiagnosisMapper;
import com.oppo.cloud.mapper.RealtimeTaskDiagnosisRuleAdviceMapper;
import com.oppo.cloud.model.*;
import com.oppo.cloud.portal.dao.RealtimeTaskDiagnosisExtendMapper;
import com.oppo.cloud.portal.domain.realtime.*;
import com.oppo.cloud.portal.service.ElasticSearchService;
import com.oppo.cloud.portal.service.RealtimeTaskDiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.*;


/**
 * 实时任务查询接口
 */
@Service
@Slf4j
public class RealtimeTaskDiagnosisServiceImpl implements RealtimeTaskDiagnosisService {

    @Autowired
    RealtimeTaskDiagnosisExtendMapper realtimeTaskDiagnosisExtendMapper;
    @Autowired
    RealtimeTaskDiagnosisMapper realtimeTaskDiagnosisMapper;
    @Autowired
    RealtimeTaskDiagnosisRuleAdviceMapper realtimeTaskDiagnosisRuleAdviceMapper;
    @Autowired
    ElasticSearchService elasticSearchService;

    @Override
    public CommonPage<RealtimeTaskDiagnosis> pageJobs(DiagnosisAdviceListReq req) {
        PageHelper.startPage(req.getPage(), req.getPageSize());
        List<RealtimeTaskDiagnosis> realtimeTaskDiagnoses = realtimeTaskDiagnosisExtendMapper.page(req);
        return CommonPage.restPage(realtimeTaskDiagnoses);
    }

    @Override
    public DiagnosisGeneralViewNumberResp getGeneralViewNumber(DiagnosisGeneralViewReq request) {
        GeneralViewNumberDto generalViewNumber = realtimeTaskDiagnosisExtendMapper.getGeneralViewNumber(request);
        DiagnosisGeneralViewReq request1DayBefore = new DiagnosisGeneralViewReq();
        request1DayBefore.setStartTs(request.getStartTs().minusDays(1));
        request1DayBefore.setEndTs(request.getEndTs().minusDays(1));
        GeneralViewNumberDto generalViewNumberDay1Before = realtimeTaskDiagnosisExtendMapper
                .getGeneralViewNumber(request1DayBefore);
        DiagnosisGeneralViewReq request7DayBefore = new DiagnosisGeneralViewReq();
        request7DayBefore.setStartTs(request.getStartTs().minusDays(7));
        request7DayBefore.setEndTs(request.getEndTs().minusDays(7));
        GeneralViewNumberDto generalViewNumberDay7Before = realtimeTaskDiagnosisExtendMapper
                .getGeneralViewNumber(request7DayBefore);
        DiagnosisGeneralViewNumberResp diagnosisGeneralViewNumberResp = new DiagnosisGeneralViewNumberResp();
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDto(generalViewNumber);
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDtoDay1Before(generalViewNumberDay1Before);
        diagnosisGeneralViewNumberResp.setGeneralViewNumberDtoDay7Before(generalViewNumberDay7Before);
        return diagnosisGeneralViewNumberResp;
    }

    @Override
    public DiagnosisGeneralViewTrendResp getGeneralViewTrend(DiagnosisGeneralViewReq request) {
        List<GeneralViewNumberDto> generalViewTrend = realtimeTaskDiagnosisExtendMapper.getGeneralViewTrend(request);
        DiagnosisGeneralViewTrendResp diagnosisGeneralViewTrendResp = new DiagnosisGeneralViewTrendResp();
        diagnosisGeneralViewTrendResp.setTrend(generalViewTrend);
        return diagnosisGeneralViewTrendResp;
    }

    @Override
    public DiagnosisGeneralVIewDistributeResp getGeneralViewDistribute(DiagnosisGeneralViewReq request) {
        RealtimeTaskDiagnosisExample realtimeTaskAppExample = new RealtimeTaskDiagnosisExample();
        realtimeTaskAppExample.createCriteria()
                .andDiagnosisEndTimeLessThanOrEqualTo(new Date(request.getEndTs().toEpochSecond(ZoneOffset.ofHours(8))))
        ;
        long count = realtimeTaskDiagnosisExtendMapper.countByExample(realtimeTaskAppExample);
        int pageSize = 100;
        int pageNum = (int) Math.ceil((double) count / pageSize);
        Map<Integer, Long> memDistribute = new HashMap<>();
        Map<Integer, Long> cpuDistribute = new HashMap<>();
        Map<Integer, Long> numDistribute = new HashMap<>();
        for (int i = 1; i <= pageNum; i++) {
            PageHelper.startPage(i, pageSize);
            try {
                List<RealtimeTaskDiagnosis> realtimeTaskApps = realtimeTaskDiagnosisExtendMapper.selectByExample(realtimeTaskAppExample);
                for (RealtimeTaskDiagnosis realtimeTaskDiagnosis : realtimeTaskApps) {
                    int costCores = realtimeTaskDiagnosis.getTmCore() * realtimeTaskDiagnosis.getTmNum();
                    int costMem = realtimeTaskDiagnosis.getTmMem() * realtimeTaskDiagnosis.getTmNum();
                    String diagnosisTypes = realtimeTaskDiagnosis.getDiagnosisTypes();
                    List<Integer> diagnosisTypesList = JSON.parseArray(diagnosisTypes, Integer.class);
                    for (Integer t : diagnosisTypesList) {
                        numDistribute.put(t, numDistribute.getOrDefault(t, 0L) + 1);
                        memDistribute.put(t, memDistribute.getOrDefault(t, 0L) + costMem);
                        cpuDistribute.put(t, cpuDistribute.getOrDefault(t, 0L) + costCores);
                    }
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
        DiagnosisGeneralVIewDistributeResp diagnosisGeneralVIewDistributeResp = new DiagnosisGeneralVIewDistributeResp();
        diagnosisGeneralVIewDistributeResp.setCpuDistribute(cpuDistribute);
        diagnosisGeneralVIewDistributeResp.setMemDistribute(memDistribute);
        diagnosisGeneralVIewDistributeResp.setTaskNumDistribute(numDistribute);
        return diagnosisGeneralVIewDistributeResp;
    }

    @Override
    public DiagnosisReportResp getReport(RealtimeTaskDiagnosis request) {
        DiagnosisReportResp diagnosisReportResp = new DiagnosisReportResp();
        List<String> reports = new ArrayList<>();
        diagnosisReportResp.setReports(reports);
        RealtimeTaskDiagnosis realtimeTaskDiagnosis = realtimeTaskDiagnosisMapper.selectByPrimaryKey(request.getId());
        diagnosisReportResp.setRealtimeTaskDiagnosis(realtimeTaskDiagnosis);
        RealtimeTaskDiagnosisRuleAdviceExample example = new RealtimeTaskDiagnosisRuleAdviceExample();
        example.createCriteria().andRealtimeTaskDiagnosisIdEqualTo(request.getId());
        List<RealtimeTaskDiagnosisRuleAdvice> realtimeTaskDiagnosisRuleAdvices = realtimeTaskDiagnosisRuleAdviceMapper.selectByExample(example);
        String esIndex = Constant.REALTIME_DIAGNOSIS_REPORT_ES_INDEX_PREFIX;
        for (RealtimeTaskDiagnosisRuleAdvice advice : realtimeTaskDiagnosisRuleAdvices) {
            try {
                if (advice.getHasAdvice() == EDiagnosisRuleHasAdvice.HAS_ADVICE.getCode()) {
                    DiagnosisRuleReport diagnosisRuleReportDoc = null;
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
                        log.info(hit.getSourceAsString());
                        log.info(hit.toString());
                        String report = hit.getSourceAsMap().get("report").toString();
                        reports.add(report);
                    }

                }

            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
        log.info("报告 {}", reports.toString());
        log.info("整体 {}", diagnosisReportResp);
        return diagnosisReportResp;
    }


}
