package com.oppo.cloud.parser.utils;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.parser.domain.mr.MRAppInfo;
import com.oppo.cloud.parser.domain.mr.MRTaskAttemptInfo;
import com.oppo.cloud.parser.domain.mr.SpeculationInfo;
import com.oppo.cloud.parser.domain.mr.event.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.hadoop.fs.FSDataInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ReplayMREventLogs {
    private MRAppInfo mrAppInfo;

    private final FSDataInputStream in;

    private static final String VERSION_JSON = "Avro-Json";
    private static final String VERSION_BINARY = "Avro-Binary";

    private static final String SPECULATION = "Speculation";

    public ReplayMREventLogs(FSDataInputStream in) {
        this.in = in;
        this.mrAppInfo = new MRAppInfo();
    }


    public void parse() throws Exception {
        String version = this.in.readLine();
        String eventSchema = this.in.readLine();
        Schema schema = new Schema.Parser().parse(eventSchema);
        DatumReader reader = new SpecificDatumReader(schema);
        Decoder decoder;

        switch (version) {
            case VERSION_BINARY:
                decoder = DecoderFactory.get().binaryDecoder(this.in, null);
                break;
            case VERSION_JSON:
                decoder = DecoderFactory.get().jsonDecoder(schema, this.in);
                break;
            default:
                throw new Exception("incompatible event log version: " + version);
        }

        while (true) {
            String eventStr;
            try {
                eventStr = reader.read(null, decoder).toString();
            } catch (Exception e) {
                break;
            }
            try {
                parseEvent(eventStr);
            } catch (Exception e) {
                log.error("parseEventErr: ", e);
            }
        }

        correlate();

    }

    public MRAppInfo getMRAppInfo() {
        return this.mrAppInfo;
    }

    private void parseEvent(String eventStr) throws Exception {
        Event event = JSON.parseObject(eventStr, Event.class);
        switch (event.getType()) {
            case JOB_SUBMITTED:
                JobSubmitted jobSubmitted = JSON.parseObject(event.getEvent(), JobSubmitted.class);
                handleJobSubmitted(jobSubmitted);
                break;
            case JOB_STATUS_CHANGED:
                break;
            case JOB_INFO_CHANGED:
                JobInfoChange jobInfoChange = JSON.parseObject(event.getEvent(), JobInfoChange.class);
                handleJobInfoChange(jobInfoChange);
                break;
            case JOB_INITED:
                JobInit jobInit = JSON.parseObject(event.getEvent(), JobInit.class);
                handleJobInit(jobInit);
                break;
            case JOB_PRIORITY_CHANGED:
                break;
            case JOB_QUEUE_CHANGED:
                JobQueueChange jobQueueChange = JSON.parseObject(event.getEvent(), JobQueueChange.class);
                handleJobQueueChange(jobQueueChange);
                break;
            case JOB_FAILED:
            case JOB_KILLED:
            case JOB_ERROR:
                JobUnsuccessfulCompletion jobUnsuccessfulCompletion = JSON.parseObject(event.getEvent(), JobUnsuccessfulCompletion.class);
                handleJobFailedEvent(jobUnsuccessfulCompletion);
                break;
            case JOB_FINISHED:
                JobFinished jobFinished = JSON.parseObject(event.getEvent(), JobFinished.class);
                handleJobFinished(jobFinished);
                break;
            case TASK_STARTED:
                TaskStarted taskStarted = JSON.parseObject(event.getEvent(), TaskStarted.class);
                handleTaskStarted(taskStarted);
                break;
            case TASK_FAILED:
                TaskFailed taskFailed = JSON.parseObject(event.getEvent(), TaskFailed.class);
                handleTaskFailed(taskFailed);
                break;
            case TASK_UPDATED:
                TaskUpdated taskUpdated = JSON.parseObject(event.getEvent(), TaskUpdated.class);
                handleTaskUpdated(taskUpdated);
                break;
            case TASK_FINISHED:
                TaskFinished taskFinished = JSON.parseObject(event.getEvent(), TaskFinished.class);
                handleTaskFinished(taskFinished);
                break;
            case MAP_ATTEMPT_STARTED:
            case CLEANUP_ATTEMPT_STARTED:
            case REDUCE_ATTEMPT_STARTED:
            case SETUP_ATTEMPT_STARTED:
                TaskAttemptStarted taskAttemptStarted = JSON.parseObject(event.getEvent(), TaskAttemptStarted.class);
                handleTaskAttemptStarted(taskAttemptStarted);
                break;
            case MAP_ATTEMPT_FAILED:
            case CLEANUP_ATTEMPT_FAILED:
            case REDUCE_ATTEMPT_FAILED:
            case SETUP_ATTEMPT_FAILED:
            case MAP_ATTEMPT_KILLED:
            case CLEANUP_ATTEMPT_KILLED:
            case REDUCE_ATTEMPT_KILLED:
            case SETUP_ATTEMPT_KILLED:
                TaskAttemptUnsuccessfulCompletion taskAttemptUnsuccessfulCompletionEvent = JSON.parseObject(event.getEvent(),
                        TaskAttemptUnsuccessfulCompletion.class);

                handleTaskAttemptFailed(taskAttemptUnsuccessfulCompletionEvent);
                break;
            case MAP_ATTEMPT_FINISHED:
                MapAttemptFinished mapAttemptFinishedEvent = JSON.parseObject(event.getEvent(), MapAttemptFinished.class);
                handleMapAttemptFinished(mapAttemptFinishedEvent);
                break;
            case REDUCE_ATTEMPT_FINISHED:
                ReduceAttemptFinished reduceAttemptFinished = JSON.parseObject(event.getEvent(), ReduceAttemptFinished.class);
                handleReduceAttemptFinished(reduceAttemptFinished);
                break;
            case SETUP_ATTEMPT_FINISHED:
            case CLEANUP_ATTEMPT_FINISHED:
                TaskAttemptFinished taskAttemptFinished = JSON.parseObject(event.getEvent(), TaskAttemptFinished.class);
                handleTaskAttemptFinished(taskAttemptFinished);
                break;
            case AM_STARTED:
                break;
            default:
                log.warn("unexpected event type: {}", event.getType());
        }
    }

    private void handleJobSubmitted(JobSubmitted event) {
        this.mrAppInfo.setJobId(event.getJobid());
        this.mrAppInfo.setJobName(event.getJobName());
        this.mrAppInfo.setUsername(event.getUserName());
        this.mrAppInfo.setSubmitTime(event.getSubmitTime());
    }

    private void handleJobInfoChange(JobInfoChange event) {
        this.mrAppInfo.setSubmitTime(event.getSubmitTime());
        this.mrAppInfo.setLaunchTime(event.getLaunchTime());
    }

    private void handleJobInit(JobInit event) {
        this.mrAppInfo.setLaunchTime(event.getLaunchTime());
        this.mrAppInfo.setTotalMaps(event.getTotalMaps());
        this.mrAppInfo.setTotalReduces(event.getTotalReduces());
    }

    private void handleJobQueueChange(JobQueueChange event) {
        this.mrAppInfo.setJobQueueName(event.getJobQueueName());
    }

    private void handleJobFailedEvent(JobUnsuccessfulCompletion event) {
        this.mrAppInfo.setFinishTime(event.getFinishTime());
        this.mrAppInfo.setSucceededMaps(event.getFinishedMaps());
        this.mrAppInfo.setSucceededReduces(event.getFinishedReduces());
        this.mrAppInfo.setFailedMaps(event.getFailedMaps());
        this.mrAppInfo.setFailedReduces(event.getFailedReduces());
        this.mrAppInfo.setKilledMaps(event.getKilledMaps());
        this.mrAppInfo.setKilledReduces(event.getKilledReduces());
        this.mrAppInfo.setJobStatus(event.getJobStatus());
        this.mrAppInfo.setErrorInfo(event.getDiagnostics());
    }

    private void handleJobFinished(JobFinished event) {
        this.mrAppInfo.setFinishTime(event.getFinishTime());
        this.mrAppInfo.setSucceededMaps(event.getFinishedMaps());
        this.mrAppInfo.setSucceededReduces(event.getFinishedReduces());
        this.mrAppInfo.setFailedMaps(event.getFailedMaps());
        this.mrAppInfo.setFailedReduces(event.getFailedReduces());
        this.mrAppInfo.setKilledMaps(event.getKilledMaps());
        this.mrAppInfo.setKilledReduces(event.getKilledReduces());
        this.mrAppInfo.setTotalCounters(handleCounters(event.getTotalCounters()));
        this.mrAppInfo.setJobStatus(JobStatus.SUCCEEDED.toString());
    }

    private void handleTaskStarted(TaskStarted event) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(event.getTaskid());
        taskInfo.setStartTime(event.getStartTime());
        taskInfo.setTaskType(event.getTaskType());
        taskInfo.setSplitLocations(event.getSplitLocations());
        Map<String, TaskInfo> tasksMap = this.mrAppInfo.getTasksMap();
        tasksMap.put(event.getTaskid(), taskInfo);
        this.mrAppInfo.setTasksMap(tasksMap);
    }

    private void handleTaskFailed(TaskFailed event) {
        TaskInfo taskInfo = this.mrAppInfo.getTasksMap().get(event.getTaskid());
        if (taskInfo == null) {
            log.error("TaskInfo is null:{}", event.getTaskid());
            return;
        }
        taskInfo.setStatus(JobStatus.FAILED.name());
        taskInfo.setFinishTime(event.getFinishTime());
        taskInfo.setError(event.getError());
        taskInfo.setFailedDueToAttemptId(event.getFailedDueToAttempt());
        taskInfo.setCounters(event.getCounters());
    }

    private void handleTaskUpdated(TaskUpdated event) {
        TaskInfo taskInfo = this.mrAppInfo.getTasksMap().get(event.getTaskid());
        if (taskInfo == null) {
            log.error("TaskInfo is null:{}", event.getTaskid());
            return;
        }
        taskInfo.setFinishTime(event.getFinishTime());
    }

    private void handleTaskFinished(TaskFinished event) {
        TaskInfo taskInfo = this.mrAppInfo.getTasksMap().get(event.getTaskid());
        if (taskInfo == null) {
            log.error("TaskInfo is null:{}", event.getTaskid());
            return;
        }
        taskInfo.setCounters(event.getCounters());
        taskInfo.setFinishTime(event.getFinishTime());
        taskInfo.setStatus(JobStatus.SUCCEEDED.toString());
        taskInfo.setSuccessfulAttemptId(event.getSuccessfulAttemptId());
    }

    private void handleTaskAttemptStarted(TaskAttemptStarted event) {
        String attemptId = event.getAttemptId();
        TaskInfo taskInfo = this.mrAppInfo.getTasksMap().get(event.getTaskid());

        TaskAttemptInfo attemptInfo = new TaskAttemptInfo();
        attemptInfo.setStartTime(event.getStartTime());
        attemptInfo.setAttemptId(event.getAttemptId());
        attemptInfo.setHttpPort(event.getHttpPort());
        attemptInfo.setTrackerName(event.getTrackerName());
        attemptInfo.setTaskType(event.getTaskType());
        attemptInfo.setShufflePort(event.getShufflePort());
        attemptInfo.setContainerId(event.getContainerId());
        Map<String, TaskAttemptInfo> attemptsMap = new HashMap<>();
        attemptsMap.put(attemptId, attemptInfo);
        taskInfo.setAttemptsMap(attemptsMap);
    }

    private void handleTaskAttemptFailed(TaskAttemptUnsuccessfulCompletion event) {
        TaskInfo taskInfo = this.mrAppInfo.getTasksMap().get(event.getTaskid());
        if (taskInfo == null) {
            log.error("TaskInfo is null:{}", event.getTaskid());
            return;
        }
        TaskAttemptInfo attemptInfo =
                taskInfo.getAttemptsMap().get(event.getAttemptId());
        if (attemptInfo == null) {
            log.error("TaskAttemptInfo is null:{}", event.getAttemptId());
            return;
        }
        attemptInfo.setFinishTime(event.getFinishTime());
        attemptInfo.setError(event.getError());
        attemptInfo.setStatus(event.getStatus());
        attemptInfo.setHostname(event.getHostname());
        attemptInfo.setPort(event.getPort());
        attemptInfo.setRackname(event.getRackname());
        attemptInfo.setShuffleFinishTime(event.getFinishTime());
        attemptInfo.setSortFinishTime(event.getFinishTime());
        attemptInfo.setMapFinishTime(event.getFinishTime());
        attemptInfo.setCounters(event.getCounters());
        if (JobStatus.SUCCEEDED.toString().equals(taskInfo.getStatus())) {
            if (attemptInfo.getAttemptId().equals(taskInfo.getSuccessfulAttemptId())) {
                taskInfo.setCounters(null);
                taskInfo.setFinishTime(-1);
                taskInfo.setStatus(null);
                taskInfo.setSuccessfulAttemptId(null);
            }
        }
    }

    private void handleMapAttemptFinished(MapAttemptFinished event) {
        TaskInfo taskInfo = this.mrAppInfo.getTasksMap().get(event.getTaskid());
        if (taskInfo == null) {
            log.error("TaskInfo is null:{}", event.getTaskid());
            return;
        }
        TaskAttemptInfo attemptInfo =
                taskInfo.getAttemptsMap().get(event.getAttemptId());
        if (attemptInfo == null) {
            log.error("TaskAttemptInfo is null:{}", event.getAttemptId());
            return;
        }
        attemptInfo.setFinishTime(event.getFinishTime());
        attemptInfo.setStatus(event.getTaskStatus());
        attemptInfo.setState(event.getState());
        attemptInfo.setMapFinishTime(event.getMapFinishTime());
        attemptInfo.setCounters(event.getCounters());
        attemptInfo.setHostname(event.getHostname());
        attemptInfo.setPort(event.getPort());
        attemptInfo.setRackname(event.getRackname());
    }

    private void handleReduceAttemptFinished(ReduceAttemptFinished event) {
        TaskInfo taskInfo = this.mrAppInfo.getTasksMap().get(event.getTaskid());
        if (taskInfo == null) {
            log.error("TaskInfo is null:{}", event.getTaskid());
            return;
        }
        TaskAttemptInfo attemptInfo =
                taskInfo.getAttemptsMap().get(event.getAttemptId());
        if (attemptInfo == null) {
            log.error("TaskAttemptInfo is null:{}", event.getAttemptId());
            return;
        }
        attemptInfo.setFinishTime(event.getFinishTime());
        attemptInfo.setStatus(event.getTaskStatus());
        attemptInfo.setState(event.getState());
        attemptInfo.setShuffleFinishTime(event.getShuffleFinishTime());
        attemptInfo.setSortFinishTime(event.getSortFinishTime());
        attemptInfo.setCounters(event.getCounters());
        attemptInfo.setHostname(event.getHostname());
        attemptInfo.setPort(event.getPort());
        attemptInfo.setRackname(event.getRackname());
    }

    private void handleTaskAttemptFinished(TaskAttemptFinished event) {
        TaskInfo taskInfo = this.mrAppInfo.getTasksMap().get(event.getTaskid());
        if (taskInfo == null) {
            log.error("TaskInfo is null:{}", event.getTaskid());
            return;
        }
        TaskAttemptInfo attemptInfo =
                taskInfo.getAttemptsMap().get(event.getAttemptId());
        if (attemptInfo == null) {
            log.error("TaskAttemptInfo is null:{}", event.getAttemptId());
            return;
        }
        attemptInfo.setFinishTime(event.getFinishTime());
        attemptInfo.setStatus(event.getTaskStatus());
        attemptInfo.setState(event.getState());
        attemptInfo.setCounters(event.getJhCounters());
        attemptInfo.setHostname(event.getHostname());
    }

    private void correlate() {
        List<MRTaskAttemptInfo> mapList = new ArrayList<>();
        List<MRTaskAttemptInfo> reduceList = new ArrayList<>();
        SpeculationInfo speculationInfo = new SpeculationInfo();
        for (Map.Entry<String, TaskInfo> map : this.mrAppInfo.getTasksMap().entrySet()) {
            TaskInfo taskInfo = map.getValue();
            if (TaskType.MAP.toString().equals(taskInfo.getTaskType())) {
                setMRTaskAttemptInfoList(taskInfo, mapList, speculationInfo);
            } else if (TaskType.REDUCE.toString().equals(taskInfo.getTaskType())) {
                setMRTaskAttemptInfoList(taskInfo, reduceList, speculationInfo);
            }
        }
        this.mrAppInfo.setMapList(mapList);
        this.mrAppInfo.setReduceList(reduceList);
        this.mrAppInfo.setSpeculationInfo(speculationInfo);
    }

    private void setMRTaskAttemptInfoList(TaskInfo taskInfo, List<MRTaskAttemptInfo> list, SpeculationInfo speculationInfo) {

        for (Map.Entry<String, TaskAttemptInfo> taskAttemptInfoMap : taskInfo.getAttemptsMap().entrySet()) {
            TaskAttemptInfo taskAttemptInfo = taskAttemptInfoMap.getValue();
            long finishTime = taskAttemptInfo.getFinishTime() == null ? 0 : taskAttemptInfo.getFinishTime();
            long startTime = taskAttemptInfo.getStartTime() == null ? 0 : taskAttemptInfo.getStartTime();
            long elapsedTime = finishTime - startTime;

            if (taskAttemptInfo.getError() != null && taskAttemptInfo.getError().contains(SPECULATION)) {
                speculationInfo.setSpeculationCount(speculationInfo.getSpeculationCount() + 1);
                speculationInfo.setSpeculationElapsedTime(speculationInfo.getSpeculationElapsedTime() + elapsedTime);
                List<String> taskAttemptIds = speculationInfo.getTaskAttemptIds();
                taskAttemptIds.add(taskAttemptInfo.getAttemptId());
                speculationInfo.setTaskAttemptIds(taskAttemptIds);
            }

            Map<String, Map<String, Long>> mapCounters = handleCounters(taskInfo.getCounters());
            MRTaskAttemptInfo mrTaskAttemptInfo = new MRTaskAttemptInfo();
            mrTaskAttemptInfo.setTaskId(getTaskId(taskAttemptInfo.getAttemptId()));
            mrTaskAttemptInfo.setAttemptId(taskAttemptInfo.getAttemptId());
            mrTaskAttemptInfo.setTaskStatus(taskAttemptInfo.getStatus());
            mrTaskAttemptInfo.setStartTime(startTime);
            mrTaskAttemptInfo.setFinishTime(finishTime);
            mrTaskAttemptInfo.setShuffleFishTime(taskAttemptInfo.getShuffleFinishTime() == null ? 0 : taskAttemptInfo.getShuffleFinishTime());
            mrTaskAttemptInfo.setSortFinishTime(taskAttemptInfo.getSortFinishTime() == null ? 0 : taskAttemptInfo.getSortFinishTime());
            mrTaskAttemptInfo.setElapsedTime(elapsedTime);
            mrTaskAttemptInfo.setError(taskAttemptInfo.getError());
            mrTaskAttemptInfo.setCounters(mapCounters);
            list.add(mrTaskAttemptInfo);

        }

    }

    private Map<String, Map<String, Long>> handleCounters(JhCounters counters) {
        Map<String, Map<String, Long>> result = new HashMap<>();
        if (counters.getGroups() == null) {
            return result;
        }
        for (JhCounterGroup group : counters.getGroups()) {
            String groupName = group.getName();
            Map<String, Long> counterMap = result.computeIfAbsent(groupName, k -> new HashMap<>());
            if (group.getCounts() == null) {
                continue;
            }
            for (JhCounter counter : group.getCounts()) {
                counterMap.put(counter.getName(), counter.getValue());
            }
        }
        return result;
    }

    private int getTaskId(String id) {
        String[] sp = id.split("_");
        if (sp.length < 2) {
            return 0;
        }
        return Integer.parseInt(sp[sp.length - 2]);
    }

}
