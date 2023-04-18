package com.oppo.cloud.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RealtimeTaskDiagnosisExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RealtimeTaskDiagnosisExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdIsNull() {
            addCriterion("realtime_task_app_id is null");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdIsNotNull() {
            addCriterion("realtime_task_app_id is not null");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdEqualTo(Integer value) {
            addCriterion("realtime_task_app_id =", value, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdNotEqualTo(Integer value) {
            addCriterion("realtime_task_app_id <>", value, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdGreaterThan(Integer value) {
            addCriterion("realtime_task_app_id >", value, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("realtime_task_app_id >=", value, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdLessThan(Integer value) {
            addCriterion("realtime_task_app_id <", value, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdLessThanOrEqualTo(Integer value) {
            addCriterion("realtime_task_app_id <=", value, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdIn(List<Integer> values) {
            addCriterion("realtime_task_app_id in", values, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdNotIn(List<Integer> values) {
            addCriterion("realtime_task_app_id not in", values, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdBetween(Integer value1, Integer value2) {
            addCriterion("realtime_task_app_id between", value1, value2, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andRealtimeTaskAppIdNotBetween(Integer value1, Integer value2) {
            addCriterion("realtime_task_app_id not between", value1, value2, "realtimeTaskAppId");
            return (Criteria) this;
        }

        public Criteria andUsernameIsNull() {
            addCriterion("username is null");
            return (Criteria) this;
        }

        public Criteria andUsernameIsNotNull() {
            addCriterion("username is not null");
            return (Criteria) this;
        }

        public Criteria andUsernameEqualTo(String value) {
            addCriterion("username =", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotEqualTo(String value) {
            addCriterion("username <>", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameGreaterThan(String value) {
            addCriterion("username >", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("username >=", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLessThan(String value) {
            addCriterion("username <", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLessThanOrEqualTo(String value) {
            addCriterion("username <=", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameLike(String value) {
            addCriterion("username like", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotLike(String value) {
            addCriterion("username not like", value, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameIn(List<String> values) {
            addCriterion("username in", values, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotIn(List<String> values) {
            addCriterion("username not in", values, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameBetween(String value1, String value2) {
            addCriterion("username between", value1, value2, "username");
            return (Criteria) this;
        }

        public Criteria andUsernameNotBetween(String value1, String value2) {
            addCriterion("username not between", value1, value2, "username");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(Integer value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(Integer value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(Integer value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(Integer value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Integer value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<Integer> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<Integer> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(Integer value1, Integer value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(Integer value1, Integer value2) {
            addCriterion("user_id not between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andProjectNameIsNull() {
            addCriterion("project_name is null");
            return (Criteria) this;
        }

        public Criteria andProjectNameIsNotNull() {
            addCriterion("project_name is not null");
            return (Criteria) this;
        }

        public Criteria andProjectNameEqualTo(String value) {
            addCriterion("project_name =", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameNotEqualTo(String value) {
            addCriterion("project_name <>", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameGreaterThan(String value) {
            addCriterion("project_name >", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameGreaterThanOrEqualTo(String value) {
            addCriterion("project_name >=", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameLessThan(String value) {
            addCriterion("project_name <", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameLessThanOrEqualTo(String value) {
            addCriterion("project_name <=", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameLike(String value) {
            addCriterion("project_name like", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameNotLike(String value) {
            addCriterion("project_name not like", value, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameIn(List<String> values) {
            addCriterion("project_name in", values, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameNotIn(List<String> values) {
            addCriterion("project_name not in", values, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameBetween(String value1, String value2) {
            addCriterion("project_name between", value1, value2, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectNameNotBetween(String value1, String value2) {
            addCriterion("project_name not between", value1, value2, "projectName");
            return (Criteria) this;
        }

        public Criteria andProjectIdIsNull() {
            addCriterion("project_id is null");
            return (Criteria) this;
        }

        public Criteria andProjectIdIsNotNull() {
            addCriterion("project_id is not null");
            return (Criteria) this;
        }

        public Criteria andProjectIdEqualTo(Integer value) {
            addCriterion("project_id =", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotEqualTo(Integer value) {
            addCriterion("project_id <>", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdGreaterThan(Integer value) {
            addCriterion("project_id >", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("project_id >=", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLessThan(Integer value) {
            addCriterion("project_id <", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdLessThanOrEqualTo(Integer value) {
            addCriterion("project_id <=", value, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdIn(List<Integer> values) {
            addCriterion("project_id in", values, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotIn(List<Integer> values) {
            addCriterion("project_id not in", values, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdBetween(Integer value1, Integer value2) {
            addCriterion("project_id between", value1, value2, "projectId");
            return (Criteria) this;
        }

        public Criteria andProjectIdNotBetween(Integer value1, Integer value2) {
            addCriterion("project_id not between", value1, value2, "projectId");
            return (Criteria) this;
        }

        public Criteria andFlowNameIsNull() {
            addCriterion("flow_name is null");
            return (Criteria) this;
        }

        public Criteria andFlowNameIsNotNull() {
            addCriterion("flow_name is not null");
            return (Criteria) this;
        }

        public Criteria andFlowNameEqualTo(String value) {
            addCriterion("flow_name =", value, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameNotEqualTo(String value) {
            addCriterion("flow_name <>", value, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameGreaterThan(String value) {
            addCriterion("flow_name >", value, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameGreaterThanOrEqualTo(String value) {
            addCriterion("flow_name >=", value, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameLessThan(String value) {
            addCriterion("flow_name <", value, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameLessThanOrEqualTo(String value) {
            addCriterion("flow_name <=", value, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameLike(String value) {
            addCriterion("flow_name like", value, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameNotLike(String value) {
            addCriterion("flow_name not like", value, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameIn(List<String> values) {
            addCriterion("flow_name in", values, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameNotIn(List<String> values) {
            addCriterion("flow_name not in", values, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameBetween(String value1, String value2) {
            addCriterion("flow_name between", value1, value2, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowNameNotBetween(String value1, String value2) {
            addCriterion("flow_name not between", value1, value2, "flowName");
            return (Criteria) this;
        }

        public Criteria andFlowIdIsNull() {
            addCriterion("flow_id is null");
            return (Criteria) this;
        }

        public Criteria andFlowIdIsNotNull() {
            addCriterion("flow_id is not null");
            return (Criteria) this;
        }

        public Criteria andFlowIdEqualTo(Integer value) {
            addCriterion("flow_id =", value, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdNotEqualTo(Integer value) {
            addCriterion("flow_id <>", value, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdGreaterThan(Integer value) {
            addCriterion("flow_id >", value, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("flow_id >=", value, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdLessThan(Integer value) {
            addCriterion("flow_id <", value, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdLessThanOrEqualTo(Integer value) {
            addCriterion("flow_id <=", value, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdIn(List<Integer> values) {
            addCriterion("flow_id in", values, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdNotIn(List<Integer> values) {
            addCriterion("flow_id not in", values, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdBetween(Integer value1, Integer value2) {
            addCriterion("flow_id between", value1, value2, "flowId");
            return (Criteria) this;
        }

        public Criteria andFlowIdNotBetween(Integer value1, Integer value2) {
            addCriterion("flow_id not between", value1, value2, "flowId");
            return (Criteria) this;
        }

        public Criteria andTaskNameIsNull() {
            addCriterion("task_name is null");
            return (Criteria) this;
        }

        public Criteria andTaskNameIsNotNull() {
            addCriterion("task_name is not null");
            return (Criteria) this;
        }

        public Criteria andTaskNameEqualTo(String value) {
            addCriterion("task_name =", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotEqualTo(String value) {
            addCriterion("task_name <>", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameGreaterThan(String value) {
            addCriterion("task_name >", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameGreaterThanOrEqualTo(String value) {
            addCriterion("task_name >=", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLessThan(String value) {
            addCriterion("task_name <", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLessThanOrEqualTo(String value) {
            addCriterion("task_name <=", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLike(String value) {
            addCriterion("task_name like", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotLike(String value) {
            addCriterion("task_name not like", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameIn(List<String> values) {
            addCriterion("task_name in", values, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotIn(List<String> values) {
            addCriterion("task_name not in", values, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameBetween(String value1, String value2) {
            addCriterion("task_name between", value1, value2, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotBetween(String value1, String value2) {
            addCriterion("task_name not between", value1, value2, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNull() {
            addCriterion("task_id is null");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNotNull() {
            addCriterion("task_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaskIdEqualTo(Integer value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(Integer value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(Integer value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(Integer value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(Integer value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<Integer> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<Integer> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(Integer value1, Integer value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(Integer value1, Integer value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdIsNull() {
            addCriterion("application_id is null");
            return (Criteria) this;
        }

        public Criteria andApplicationIdIsNotNull() {
            addCriterion("application_id is not null");
            return (Criteria) this;
        }

        public Criteria andApplicationIdEqualTo(String value) {
            addCriterion("application_id =", value, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdNotEqualTo(String value) {
            addCriterion("application_id <>", value, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdGreaterThan(String value) {
            addCriterion("application_id >", value, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdGreaterThanOrEqualTo(String value) {
            addCriterion("application_id >=", value, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdLessThan(String value) {
            addCriterion("application_id <", value, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdLessThanOrEqualTo(String value) {
            addCriterion("application_id <=", value, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdLike(String value) {
            addCriterion("application_id like", value, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdNotLike(String value) {
            addCriterion("application_id not like", value, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdIn(List<String> values) {
            addCriterion("application_id in", values, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdNotIn(List<String> values) {
            addCriterion("application_id not in", values, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdBetween(String value1, String value2) {
            addCriterion("application_id between", value1, value2, "applicationId");
            return (Criteria) this;
        }

        public Criteria andApplicationIdNotBetween(String value1, String value2) {
            addCriterion("application_id not between", value1, value2, "applicationId");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlIsNull() {
            addCriterion("flink_track_url is null");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlIsNotNull() {
            addCriterion("flink_track_url is not null");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlEqualTo(String value) {
            addCriterion("flink_track_url =", value, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlNotEqualTo(String value) {
            addCriterion("flink_track_url <>", value, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlGreaterThan(String value) {
            addCriterion("flink_track_url >", value, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlGreaterThanOrEqualTo(String value) {
            addCriterion("flink_track_url >=", value, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlLessThan(String value) {
            addCriterion("flink_track_url <", value, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlLessThanOrEqualTo(String value) {
            addCriterion("flink_track_url <=", value, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlLike(String value) {
            addCriterion("flink_track_url like", value, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlNotLike(String value) {
            addCriterion("flink_track_url not like", value, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlIn(List<String> values) {
            addCriterion("flink_track_url in", values, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlNotIn(List<String> values) {
            addCriterion("flink_track_url not in", values, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlBetween(String value1, String value2) {
            addCriterion("flink_track_url between", value1, value2, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andFlinkTrackUrlNotBetween(String value1, String value2) {
            addCriterion("flink_track_url not between", value1, value2, "flinkTrackUrl");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbIsNull() {
            addCriterion("allocated_mb is null");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbIsNotNull() {
            addCriterion("allocated_mb is not null");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbEqualTo(Integer value) {
            addCriterion("allocated_mb =", value, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbNotEqualTo(Integer value) {
            addCriterion("allocated_mb <>", value, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbGreaterThan(Integer value) {
            addCriterion("allocated_mb >", value, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbGreaterThanOrEqualTo(Integer value) {
            addCriterion("allocated_mb >=", value, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbLessThan(Integer value) {
            addCriterion("allocated_mb <", value, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbLessThanOrEqualTo(Integer value) {
            addCriterion("allocated_mb <=", value, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbIn(List<Integer> values) {
            addCriterion("allocated_mb in", values, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbNotIn(List<Integer> values) {
            addCriterion("allocated_mb not in", values, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbBetween(Integer value1, Integer value2) {
            addCriterion("allocated_mb between", value1, value2, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedMbNotBetween(Integer value1, Integer value2) {
            addCriterion("allocated_mb not between", value1, value2, "allocatedMb");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresIsNull() {
            addCriterion("allocated_vcores is null");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresIsNotNull() {
            addCriterion("allocated_vcores is not null");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresEqualTo(Integer value) {
            addCriterion("allocated_vcores =", value, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresNotEqualTo(Integer value) {
            addCriterion("allocated_vcores <>", value, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresGreaterThan(Integer value) {
            addCriterion("allocated_vcores >", value, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresGreaterThanOrEqualTo(Integer value) {
            addCriterion("allocated_vcores >=", value, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresLessThan(Integer value) {
            addCriterion("allocated_vcores <", value, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresLessThanOrEqualTo(Integer value) {
            addCriterion("allocated_vcores <=", value, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresIn(List<Integer> values) {
            addCriterion("allocated_vcores in", values, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresNotIn(List<Integer> values) {
            addCriterion("allocated_vcores not in", values, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresBetween(Integer value1, Integer value2) {
            addCriterion("allocated_vcores between", value1, value2, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andAllocatedVcoresNotBetween(Integer value1, Integer value2) {
            addCriterion("allocated_vcores not between", value1, value2, "allocatedVcores");
            return (Criteria) this;
        }

        public Criteria andRunningContainersIsNull() {
            addCriterion("running_containers is null");
            return (Criteria) this;
        }

        public Criteria andRunningContainersIsNotNull() {
            addCriterion("running_containers is not null");
            return (Criteria) this;
        }

        public Criteria andRunningContainersEqualTo(Integer value) {
            addCriterion("running_containers =", value, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersNotEqualTo(Integer value) {
            addCriterion("running_containers <>", value, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersGreaterThan(Integer value) {
            addCriterion("running_containers >", value, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersGreaterThanOrEqualTo(Integer value) {
            addCriterion("running_containers >=", value, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersLessThan(Integer value) {
            addCriterion("running_containers <", value, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersLessThanOrEqualTo(Integer value) {
            addCriterion("running_containers <=", value, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersIn(List<Integer> values) {
            addCriterion("running_containers in", values, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersNotIn(List<Integer> values) {
            addCriterion("running_containers not in", values, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersBetween(Integer value1, Integer value2) {
            addCriterion("running_containers between", value1, value2, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andRunningContainersNotBetween(Integer value1, Integer value2) {
            addCriterion("running_containers not between", value1, value2, "runningContainers");
            return (Criteria) this;
        }

        public Criteria andEngineTypeIsNull() {
            addCriterion("engine_type is null");
            return (Criteria) this;
        }

        public Criteria andEngineTypeIsNotNull() {
            addCriterion("engine_type is not null");
            return (Criteria) this;
        }

        public Criteria andEngineTypeEqualTo(String value) {
            addCriterion("engine_type =", value, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeNotEqualTo(String value) {
            addCriterion("engine_type <>", value, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeGreaterThan(String value) {
            addCriterion("engine_type >", value, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeGreaterThanOrEqualTo(String value) {
            addCriterion("engine_type >=", value, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeLessThan(String value) {
            addCriterion("engine_type <", value, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeLessThanOrEqualTo(String value) {
            addCriterion("engine_type <=", value, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeLike(String value) {
            addCriterion("engine_type like", value, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeNotLike(String value) {
            addCriterion("engine_type not like", value, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeIn(List<String> values) {
            addCriterion("engine_type in", values, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeNotIn(List<String> values) {
            addCriterion("engine_type not in", values, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeBetween(String value1, String value2) {
            addCriterion("engine_type between", value1, value2, "engineType");
            return (Criteria) this;
        }

        public Criteria andEngineTypeNotBetween(String value1, String value2) {
            addCriterion("engine_type not between", value1, value2, "engineType");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeIsNull() {
            addCriterion("execution_time is null");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeIsNotNull() {
            addCriterion("execution_time is not null");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeEqualTo(Date value) {
            addCriterion("execution_time =", value, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeNotEqualTo(Date value) {
            addCriterion("execution_time <>", value, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeGreaterThan(Date value) {
            addCriterion("execution_time >", value, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("execution_time >=", value, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeLessThan(Date value) {
            addCriterion("execution_time <", value, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeLessThanOrEqualTo(Date value) {
            addCriterion("execution_time <=", value, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeIn(List<Date> values) {
            addCriterion("execution_time in", values, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeNotIn(List<Date> values) {
            addCriterion("execution_time not in", values, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeBetween(Date value1, Date value2) {
            addCriterion("execution_time between", value1, value2, "executionTime");
            return (Criteria) this;
        }

        public Criteria andExecutionTimeNotBetween(Date value1, Date value2) {
            addCriterion("execution_time not between", value1, value2, "executionTime");
            return (Criteria) this;
        }

        public Criteria andDurationIsNull() {
            addCriterion("duration is null");
            return (Criteria) this;
        }

        public Criteria andDurationIsNotNull() {
            addCriterion("duration is not null");
            return (Criteria) this;
        }

        public Criteria andDurationEqualTo(Double value) {
            addCriterion("duration =", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationNotEqualTo(Double value) {
            addCriterion("duration <>", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationGreaterThan(Double value) {
            addCriterion("duration >", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationGreaterThanOrEqualTo(Double value) {
            addCriterion("duration >=", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationLessThan(Double value) {
            addCriterion("duration <", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationLessThanOrEqualTo(Double value) {
            addCriterion("duration <=", value, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationIn(List<Double> values) {
            addCriterion("duration in", values, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationNotIn(List<Double> values) {
            addCriterion("duration not in", values, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationBetween(Double value1, Double value2) {
            addCriterion("duration between", value1, value2, "duration");
            return (Criteria) this;
        }

        public Criteria andDurationNotBetween(Double value1, Double value2) {
            addCriterion("duration not between", value1, value2, "duration");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimeEqualTo(Date value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(Date value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(Date value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(Date value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<Date> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<Date> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(Date value1, Date value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNull() {
            addCriterion("end_time is null");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNotNull() {
            addCriterion("end_time is not null");
            return (Criteria) this;
        }

        public Criteria andEndTimeEqualTo(Date value) {
            addCriterion("end_time =", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotEqualTo(Date value) {
            addCriterion("end_time <>", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThan(Date value) {
            addCriterion("end_time >", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("end_time >=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThan(Date value) {
            addCriterion("end_time <", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("end_time <=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIn(List<Date> values) {
            addCriterion("end_time in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotIn(List<Date> values) {
            addCriterion("end_time not in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeBetween(Date value1, Date value2) {
            addCriterion("end_time between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("end_time not between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsIsNull() {
            addCriterion("vcore_seconds is null");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsIsNotNull() {
            addCriterion("vcore_seconds is not null");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsEqualTo(Float value) {
            addCriterion("vcore_seconds =", value, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsNotEqualTo(Float value) {
            addCriterion("vcore_seconds <>", value, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsGreaterThan(Float value) {
            addCriterion("vcore_seconds >", value, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsGreaterThanOrEqualTo(Float value) {
            addCriterion("vcore_seconds >=", value, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsLessThan(Float value) {
            addCriterion("vcore_seconds <", value, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsLessThanOrEqualTo(Float value) {
            addCriterion("vcore_seconds <=", value, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsIn(List<Float> values) {
            addCriterion("vcore_seconds in", values, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsNotIn(List<Float> values) {
            addCriterion("vcore_seconds not in", values, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsBetween(Float value1, Float value2) {
            addCriterion("vcore_seconds between", value1, value2, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andVcoreSecondsNotBetween(Float value1, Float value2) {
            addCriterion("vcore_seconds not between", value1, value2, "vcoreSeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsIsNull() {
            addCriterion("memory_seconds is null");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsIsNotNull() {
            addCriterion("memory_seconds is not null");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsEqualTo(Float value) {
            addCriterion("memory_seconds =", value, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsNotEqualTo(Float value) {
            addCriterion("memory_seconds <>", value, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsGreaterThan(Float value) {
            addCriterion("memory_seconds >", value, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsGreaterThanOrEqualTo(Float value) {
            addCriterion("memory_seconds >=", value, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsLessThan(Float value) {
            addCriterion("memory_seconds <", value, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsLessThanOrEqualTo(Float value) {
            addCriterion("memory_seconds <=", value, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsIn(List<Float> values) {
            addCriterion("memory_seconds in", values, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsNotIn(List<Float> values) {
            addCriterion("memory_seconds not in", values, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsBetween(Float value1, Float value2) {
            addCriterion("memory_seconds between", value1, value2, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andMemorySecondsNotBetween(Float value1, Float value2) {
            addCriterion("memory_seconds not between", value1, value2, "memorySeconds");
            return (Criteria) this;
        }

        public Criteria andQueueIsNull() {
            addCriterion("queue is null");
            return (Criteria) this;
        }

        public Criteria andQueueIsNotNull() {
            addCriterion("queue is not null");
            return (Criteria) this;
        }

        public Criteria andQueueEqualTo(String value) {
            addCriterion("queue =", value, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueNotEqualTo(String value) {
            addCriterion("queue <>", value, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueGreaterThan(String value) {
            addCriterion("queue >", value, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueGreaterThanOrEqualTo(String value) {
            addCriterion("queue >=", value, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueLessThan(String value) {
            addCriterion("queue <", value, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueLessThanOrEqualTo(String value) {
            addCriterion("queue <=", value, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueLike(String value) {
            addCriterion("queue like", value, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueNotLike(String value) {
            addCriterion("queue not like", value, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueIn(List<String> values) {
            addCriterion("queue in", values, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueNotIn(List<String> values) {
            addCriterion("queue not in", values, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueBetween(String value1, String value2) {
            addCriterion("queue between", value1, value2, "queue");
            return (Criteria) this;
        }

        public Criteria andQueueNotBetween(String value1, String value2) {
            addCriterion("queue not between", value1, value2, "queue");
            return (Criteria) this;
        }

        public Criteria andClusterNameIsNull() {
            addCriterion("cluster_name is null");
            return (Criteria) this;
        }

        public Criteria andClusterNameIsNotNull() {
            addCriterion("cluster_name is not null");
            return (Criteria) this;
        }

        public Criteria andClusterNameEqualTo(String value) {
            addCriterion("cluster_name =", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotEqualTo(String value) {
            addCriterion("cluster_name <>", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameGreaterThan(String value) {
            addCriterion("cluster_name >", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameGreaterThanOrEqualTo(String value) {
            addCriterion("cluster_name >=", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLessThan(String value) {
            addCriterion("cluster_name <", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLessThanOrEqualTo(String value) {
            addCriterion("cluster_name <=", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameLike(String value) {
            addCriterion("cluster_name like", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotLike(String value) {
            addCriterion("cluster_name not like", value, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameIn(List<String> values) {
            addCriterion("cluster_name in", values, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotIn(List<String> values) {
            addCriterion("cluster_name not in", values, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameBetween(String value1, String value2) {
            addCriterion("cluster_name between", value1, value2, "clusterName");
            return (Criteria) this;
        }

        public Criteria andClusterNameNotBetween(String value1, String value2) {
            addCriterion("cluster_name not between", value1, value2, "clusterName");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIsNull() {
            addCriterion("retry_times is null");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIsNotNull() {
            addCriterion("retry_times is not null");
            return (Criteria) this;
        }

        public Criteria andRetryTimesEqualTo(Integer value) {
            addCriterion("retry_times =", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotEqualTo(Integer value) {
            addCriterion("retry_times <>", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesGreaterThan(Integer value) {
            addCriterion("retry_times >", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_times >=", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesLessThan(Integer value) {
            addCriterion("retry_times <", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesLessThanOrEqualTo(Integer value) {
            addCriterion("retry_times <=", value, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesIn(List<Integer> values) {
            addCriterion("retry_times in", values, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotIn(List<Integer> values) {
            addCriterion("retry_times not in", values, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesBetween(Integer value1, Integer value2) {
            addCriterion("retry_times between", value1, value2, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andRetryTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_times not between", value1, value2, "retryTimes");
            return (Criteria) this;
        }

        public Criteria andExecuteUserIsNull() {
            addCriterion("execute_user is null");
            return (Criteria) this;
        }

        public Criteria andExecuteUserIsNotNull() {
            addCriterion("execute_user is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteUserEqualTo(String value) {
            addCriterion("execute_user =", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserNotEqualTo(String value) {
            addCriterion("execute_user <>", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserGreaterThan(String value) {
            addCriterion("execute_user >", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserGreaterThanOrEqualTo(String value) {
            addCriterion("execute_user >=", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserLessThan(String value) {
            addCriterion("execute_user <", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserLessThanOrEqualTo(String value) {
            addCriterion("execute_user <=", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserLike(String value) {
            addCriterion("execute_user like", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserNotLike(String value) {
            addCriterion("execute_user not like", value, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserIn(List<String> values) {
            addCriterion("execute_user in", values, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserNotIn(List<String> values) {
            addCriterion("execute_user not in", values, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserBetween(String value1, String value2) {
            addCriterion("execute_user between", value1, value2, "executeUser");
            return (Criteria) this;
        }

        public Criteria andExecuteUserNotBetween(String value1, String value2) {
            addCriterion("execute_user not between", value1, value2, "executeUser");
            return (Criteria) this;
        }

        public Criteria andParallelIsNull() {
            addCriterion("parallel is null");
            return (Criteria) this;
        }

        public Criteria andParallelIsNotNull() {
            addCriterion("parallel is not null");
            return (Criteria) this;
        }

        public Criteria andParallelEqualTo(Integer value) {
            addCriterion("parallel =", value, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelNotEqualTo(Integer value) {
            addCriterion("parallel <>", value, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelGreaterThan(Integer value) {
            addCriterion("parallel >", value, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelGreaterThanOrEqualTo(Integer value) {
            addCriterion("parallel >=", value, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelLessThan(Integer value) {
            addCriterion("parallel <", value, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelLessThanOrEqualTo(Integer value) {
            addCriterion("parallel <=", value, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelIn(List<Integer> values) {
            addCriterion("parallel in", values, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelNotIn(List<Integer> values) {
            addCriterion("parallel not in", values, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelBetween(Integer value1, Integer value2) {
            addCriterion("parallel between", value1, value2, "parallel");
            return (Criteria) this;
        }

        public Criteria andParallelNotBetween(Integer value1, Integer value2) {
            addCriterion("parallel not between", value1, value2, "parallel");
            return (Criteria) this;
        }

        public Criteria andTmSlotIsNull() {
            addCriterion("tm_slot is null");
            return (Criteria) this;
        }

        public Criteria andTmSlotIsNotNull() {
            addCriterion("tm_slot is not null");
            return (Criteria) this;
        }

        public Criteria andTmSlotEqualTo(Integer value) {
            addCriterion("tm_slot =", value, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotNotEqualTo(Integer value) {
            addCriterion("tm_slot <>", value, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotGreaterThan(Integer value) {
            addCriterion("tm_slot >", value, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotGreaterThanOrEqualTo(Integer value) {
            addCriterion("tm_slot >=", value, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotLessThan(Integer value) {
            addCriterion("tm_slot <", value, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotLessThanOrEqualTo(Integer value) {
            addCriterion("tm_slot <=", value, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotIn(List<Integer> values) {
            addCriterion("tm_slot in", values, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotNotIn(List<Integer> values) {
            addCriterion("tm_slot not in", values, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotBetween(Integer value1, Integer value2) {
            addCriterion("tm_slot between", value1, value2, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmSlotNotBetween(Integer value1, Integer value2) {
            addCriterion("tm_slot not between", value1, value2, "tmSlot");
            return (Criteria) this;
        }

        public Criteria andTmCoreIsNull() {
            addCriterion("tm_core is null");
            return (Criteria) this;
        }

        public Criteria andTmCoreIsNotNull() {
            addCriterion("tm_core is not null");
            return (Criteria) this;
        }

        public Criteria andTmCoreEqualTo(Integer value) {
            addCriterion("tm_core =", value, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreNotEqualTo(Integer value) {
            addCriterion("tm_core <>", value, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreGreaterThan(Integer value) {
            addCriterion("tm_core >", value, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreGreaterThanOrEqualTo(Integer value) {
            addCriterion("tm_core >=", value, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreLessThan(Integer value) {
            addCriterion("tm_core <", value, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreLessThanOrEqualTo(Integer value) {
            addCriterion("tm_core <=", value, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreIn(List<Integer> values) {
            addCriterion("tm_core in", values, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreNotIn(List<Integer> values) {
            addCriterion("tm_core not in", values, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreBetween(Integer value1, Integer value2) {
            addCriterion("tm_core between", value1, value2, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmCoreNotBetween(Integer value1, Integer value2) {
            addCriterion("tm_core not between", value1, value2, "tmCore");
            return (Criteria) this;
        }

        public Criteria andTmMemIsNull() {
            addCriterion("tm_mem is null");
            return (Criteria) this;
        }

        public Criteria andTmMemIsNotNull() {
            addCriterion("tm_mem is not null");
            return (Criteria) this;
        }

        public Criteria andTmMemEqualTo(Integer value) {
            addCriterion("tm_mem =", value, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemNotEqualTo(Integer value) {
            addCriterion("tm_mem <>", value, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemGreaterThan(Integer value) {
            addCriterion("tm_mem >", value, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemGreaterThanOrEqualTo(Integer value) {
            addCriterion("tm_mem >=", value, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemLessThan(Integer value) {
            addCriterion("tm_mem <", value, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemLessThanOrEqualTo(Integer value) {
            addCriterion("tm_mem <=", value, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemIn(List<Integer> values) {
            addCriterion("tm_mem in", values, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemNotIn(List<Integer> values) {
            addCriterion("tm_mem not in", values, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemBetween(Integer value1, Integer value2) {
            addCriterion("tm_mem between", value1, value2, "tmMem");
            return (Criteria) this;
        }

        public Criteria andTmMemNotBetween(Integer value1, Integer value2) {
            addCriterion("tm_mem not between", value1, value2, "tmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemIsNull() {
            addCriterion("jm_mem is null");
            return (Criteria) this;
        }

        public Criteria andJmMemIsNotNull() {
            addCriterion("jm_mem is not null");
            return (Criteria) this;
        }

        public Criteria andJmMemEqualTo(Integer value) {
            addCriterion("jm_mem =", value, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemNotEqualTo(Integer value) {
            addCriterion("jm_mem <>", value, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemGreaterThan(Integer value) {
            addCriterion("jm_mem >", value, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemGreaterThanOrEqualTo(Integer value) {
            addCriterion("jm_mem >=", value, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemLessThan(Integer value) {
            addCriterion("jm_mem <", value, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemLessThanOrEqualTo(Integer value) {
            addCriterion("jm_mem <=", value, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemIn(List<Integer> values) {
            addCriterion("jm_mem in", values, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemNotIn(List<Integer> values) {
            addCriterion("jm_mem not in", values, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemBetween(Integer value1, Integer value2) {
            addCriterion("jm_mem between", value1, value2, "jmMem");
            return (Criteria) this;
        }

        public Criteria andJmMemNotBetween(Integer value1, Integer value2) {
            addCriterion("jm_mem not between", value1, value2, "jmMem");
            return (Criteria) this;
        }

        public Criteria andTmNumIsNull() {
            addCriterion("tm_num is null");
            return (Criteria) this;
        }

        public Criteria andTmNumIsNotNull() {
            addCriterion("tm_num is not null");
            return (Criteria) this;
        }

        public Criteria andTmNumEqualTo(Integer value) {
            addCriterion("tm_num =", value, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumNotEqualTo(Integer value) {
            addCriterion("tm_num <>", value, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumGreaterThan(Integer value) {
            addCriterion("tm_num >", value, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("tm_num >=", value, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumLessThan(Integer value) {
            addCriterion("tm_num <", value, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumLessThanOrEqualTo(Integer value) {
            addCriterion("tm_num <=", value, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumIn(List<Integer> values) {
            addCriterion("tm_num in", values, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumNotIn(List<Integer> values) {
            addCriterion("tm_num not in", values, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumBetween(Integer value1, Integer value2) {
            addCriterion("tm_num between", value1, value2, "tmNum");
            return (Criteria) this;
        }

        public Criteria andTmNumNotBetween(Integer value1, Integer value2) {
            addCriterion("tm_num not between", value1, value2, "tmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeIsNull() {
            addCriterion("diagnosis_end_time is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeIsNotNull() {
            addCriterion("diagnosis_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeEqualTo(Date value) {
            addCriterion("diagnosis_end_time =", value, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeNotEqualTo(Date value) {
            addCriterion("diagnosis_end_time <>", value, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeGreaterThan(Date value) {
            addCriterion("diagnosis_end_time >", value, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("diagnosis_end_time >=", value, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeLessThan(Date value) {
            addCriterion("diagnosis_end_time <", value, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("diagnosis_end_time <=", value, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeIn(List<Date> values) {
            addCriterion("diagnosis_end_time in", values, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeNotIn(List<Date> values) {
            addCriterion("diagnosis_end_time not in", values, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeBetween(Date value1, Date value2) {
            addCriterion("diagnosis_end_time between", value1, value2, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("diagnosis_end_time not between", value1, value2, "diagnosisEndTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeIsNull() {
            addCriterion("diagnosis_start_time is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeIsNotNull() {
            addCriterion("diagnosis_start_time is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeEqualTo(Date value) {
            addCriterion("diagnosis_start_time =", value, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeNotEqualTo(Date value) {
            addCriterion("diagnosis_start_time <>", value, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeGreaterThan(Date value) {
            addCriterion("diagnosis_start_time >", value, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("diagnosis_start_time >=", value, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeLessThan(Date value) {
            addCriterion("diagnosis_start_time <", value, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("diagnosis_start_time <=", value, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeIn(List<Date> values) {
            addCriterion("diagnosis_start_time in", values, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeNotIn(List<Date> values) {
            addCriterion("diagnosis_start_time not in", values, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeBetween(Date value1, Date value2) {
            addCriterion("diagnosis_start_time between", value1, value2, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("diagnosis_start_time not between", value1, value2, "diagnosisStartTime");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeIsNull() {
            addCriterion("diagnosis_resource_type is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeIsNotNull() {
            addCriterion("diagnosis_resource_type is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeEqualTo(String value) {
            addCriterion("diagnosis_resource_type =", value, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeNotEqualTo(String value) {
            addCriterion("diagnosis_resource_type <>", value, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeGreaterThan(String value) {
            addCriterion("diagnosis_resource_type >", value, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeGreaterThanOrEqualTo(String value) {
            addCriterion("diagnosis_resource_type >=", value, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeLessThan(String value) {
            addCriterion("diagnosis_resource_type <", value, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeLessThanOrEqualTo(String value) {
            addCriterion("diagnosis_resource_type <=", value, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeLike(String value) {
            addCriterion("diagnosis_resource_type like", value, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeNotLike(String value) {
            addCriterion("diagnosis_resource_type not like", value, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeIn(List<String> values) {
            addCriterion("diagnosis_resource_type in", values, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeNotIn(List<String> values) {
            addCriterion("diagnosis_resource_type not in", values, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeBetween(String value1, String value2) {
            addCriterion("diagnosis_resource_type between", value1, value2, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisResourceTypeNotBetween(String value1, String value2) {
            addCriterion("diagnosis_resource_type not between", value1, value2, "diagnosisResourceType");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromIsNull() {
            addCriterion("diagnosis_from is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromIsNotNull() {
            addCriterion("diagnosis_from is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromEqualTo(Integer value) {
            addCriterion("diagnosis_from =", value, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromNotEqualTo(Integer value) {
            addCriterion("diagnosis_from <>", value, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromGreaterThan(Integer value) {
            addCriterion("diagnosis_from >", value, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromGreaterThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_from >=", value, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromLessThan(Integer value) {
            addCriterion("diagnosis_from <", value, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromLessThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_from <=", value, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromIn(List<Integer> values) {
            addCriterion("diagnosis_from in", values, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromNotIn(List<Integer> values) {
            addCriterion("diagnosis_from not in", values, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_from between", value1, value2, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisFromNotBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_from not between", value1, value2, "diagnosisFrom");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelIsNull() {
            addCriterion("diagnosis_parallel is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelIsNotNull() {
            addCriterion("diagnosis_parallel is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelEqualTo(Integer value) {
            addCriterion("diagnosis_parallel =", value, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelNotEqualTo(Integer value) {
            addCriterion("diagnosis_parallel <>", value, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelGreaterThan(Integer value) {
            addCriterion("diagnosis_parallel >", value, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelGreaterThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_parallel >=", value, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelLessThan(Integer value) {
            addCriterion("diagnosis_parallel <", value, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelLessThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_parallel <=", value, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelIn(List<Integer> values) {
            addCriterion("diagnosis_parallel in", values, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelNotIn(List<Integer> values) {
            addCriterion("diagnosis_parallel not in", values, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_parallel between", value1, value2, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisParallelNotBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_parallel not between", value1, value2, "diagnosisParallel");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeIsNull() {
            addCriterion("diagnosis_jm_mem_size is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeIsNotNull() {
            addCriterion("diagnosis_jm_mem_size is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeEqualTo(Integer value) {
            addCriterion("diagnosis_jm_mem_size =", value, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeNotEqualTo(Integer value) {
            addCriterion("diagnosis_jm_mem_size <>", value, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeGreaterThan(Integer value) {
            addCriterion("diagnosis_jm_mem_size >", value, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeGreaterThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_jm_mem_size >=", value, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeLessThan(Integer value) {
            addCriterion("diagnosis_jm_mem_size <", value, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeLessThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_jm_mem_size <=", value, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeIn(List<Integer> values) {
            addCriterion("diagnosis_jm_mem_size in", values, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeNotIn(List<Integer> values) {
            addCriterion("diagnosis_jm_mem_size not in", values, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_jm_mem_size between", value1, value2, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisJmMemSizeNotBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_jm_mem_size not between", value1, value2, "diagnosisJmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeIsNull() {
            addCriterion("diagnosis_tm_mem_size is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeIsNotNull() {
            addCriterion("diagnosis_tm_mem_size is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeEqualTo(Integer value) {
            addCriterion("diagnosis_tm_mem_size =", value, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeNotEqualTo(Integer value) {
            addCriterion("diagnosis_tm_mem_size <>", value, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeGreaterThan(Integer value) {
            addCriterion("diagnosis_tm_mem_size >", value, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeGreaterThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_tm_mem_size >=", value, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeLessThan(Integer value) {
            addCriterion("diagnosis_tm_mem_size <", value, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeLessThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_tm_mem_size <=", value, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeIn(List<Integer> values) {
            addCriterion("diagnosis_tm_mem_size in", values, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeNotIn(List<Integer> values) {
            addCriterion("diagnosis_tm_mem_size not in", values, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_tm_mem_size between", value1, value2, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmMemSizeNotBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_tm_mem_size not between", value1, value2, "diagnosisTmMemSize");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumIsNull() {
            addCriterion("diagnosis_tm_slot_num is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumIsNotNull() {
            addCriterion("diagnosis_tm_slot_num is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumEqualTo(Integer value) {
            addCriterion("diagnosis_tm_slot_num =", value, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumNotEqualTo(Integer value) {
            addCriterion("diagnosis_tm_slot_num <>", value, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumGreaterThan(Integer value) {
            addCriterion("diagnosis_tm_slot_num >", value, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_tm_slot_num >=", value, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumLessThan(Integer value) {
            addCriterion("diagnosis_tm_slot_num <", value, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumLessThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_tm_slot_num <=", value, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumIn(List<Integer> values) {
            addCriterion("diagnosis_tm_slot_num in", values, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumNotIn(List<Integer> values) {
            addCriterion("diagnosis_tm_slot_num not in", values, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_tm_slot_num between", value1, value2, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmSlotNumNotBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_tm_slot_num not between", value1, value2, "diagnosisTmSlotNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumIsNull() {
            addCriterion("diagnosis_tm_core_num is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumIsNotNull() {
            addCriterion("diagnosis_tm_core_num is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumEqualTo(Integer value) {
            addCriterion("diagnosis_tm_core_num =", value, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumNotEqualTo(Integer value) {
            addCriterion("diagnosis_tm_core_num <>", value, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumGreaterThan(Integer value) {
            addCriterion("diagnosis_tm_core_num >", value, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_tm_core_num >=", value, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumLessThan(Integer value) {
            addCriterion("diagnosis_tm_core_num <", value, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumLessThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_tm_core_num <=", value, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumIn(List<Integer> values) {
            addCriterion("diagnosis_tm_core_num in", values, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumNotIn(List<Integer> values) {
            addCriterion("diagnosis_tm_core_num not in", values, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_tm_core_num between", value1, value2, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmCoreNumNotBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_tm_core_num not between", value1, value2, "diagnosisTmCoreNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumIsNull() {
            addCriterion("diagnosis_tm_num is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumIsNotNull() {
            addCriterion("diagnosis_tm_num is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumEqualTo(Integer value) {
            addCriterion("diagnosis_tm_num =", value, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumNotEqualTo(Integer value) {
            addCriterion("diagnosis_tm_num <>", value, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumGreaterThan(Integer value) {
            addCriterion("diagnosis_tm_num >", value, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_tm_num >=", value, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumLessThan(Integer value) {
            addCriterion("diagnosis_tm_num <", value, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumLessThanOrEqualTo(Integer value) {
            addCriterion("diagnosis_tm_num <=", value, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumIn(List<Integer> values) {
            addCriterion("diagnosis_tm_num in", values, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumNotIn(List<Integer> values) {
            addCriterion("diagnosis_tm_num not in", values, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_tm_num between", value1, value2, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTmNumNotBetween(Integer value1, Integer value2) {
            addCriterion("diagnosis_tm_num not between", value1, value2, "diagnosisTmNum");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesIsNull() {
            addCriterion("diagnosis_types is null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesIsNotNull() {
            addCriterion("diagnosis_types is not null");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesEqualTo(String value) {
            addCriterion("diagnosis_types =", value, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesNotEqualTo(String value) {
            addCriterion("diagnosis_types <>", value, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesGreaterThan(String value) {
            addCriterion("diagnosis_types >", value, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesGreaterThanOrEqualTo(String value) {
            addCriterion("diagnosis_types >=", value, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesLessThan(String value) {
            addCriterion("diagnosis_types <", value, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesLessThanOrEqualTo(String value) {
            addCriterion("diagnosis_types <=", value, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesLike(String value) {
            addCriterion("diagnosis_types like", value, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesNotLike(String value) {
            addCriterion("diagnosis_types not like", value, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesIn(List<String> values) {
            addCriterion("diagnosis_types in", values, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesNotIn(List<String> values) {
            addCriterion("diagnosis_types not in", values, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesBetween(String value1, String value2) {
            addCriterion("diagnosis_types between", value1, value2, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andDiagnosisTypesNotBetween(String value1, String value2) {
            addCriterion("diagnosis_types not between", value1, value2, "diagnosisTypes");
            return (Criteria) this;
        }

        public Criteria andProcessStateIsNull() {
            addCriterion("process_state is null");
            return (Criteria) this;
        }

        public Criteria andProcessStateIsNotNull() {
            addCriterion("process_state is not null");
            return (Criteria) this;
        }

        public Criteria andProcessStateEqualTo(String value) {
            addCriterion("process_state =", value, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateNotEqualTo(String value) {
            addCriterion("process_state <>", value, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateGreaterThan(String value) {
            addCriterion("process_state >", value, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateGreaterThanOrEqualTo(String value) {
            addCriterion("process_state >=", value, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateLessThan(String value) {
            addCriterion("process_state <", value, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateLessThanOrEqualTo(String value) {
            addCriterion("process_state <=", value, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateLike(String value) {
            addCriterion("process_state like", value, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateNotLike(String value) {
            addCriterion("process_state not like", value, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateIn(List<String> values) {
            addCriterion("process_state in", values, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateNotIn(List<String> values) {
            addCriterion("process_state not in", values, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateBetween(String value1, String value2) {
            addCriterion("process_state between", value1, value2, "processState");
            return (Criteria) this;
        }

        public Criteria andProcessStateNotBetween(String value1, String value2) {
            addCriterion("process_state not between", value1, value2, "processState");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}