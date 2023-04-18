package com.oppo.cloud.diagnosis.domain.dto;

import com.oppo.cloud.model.TaskInstance;
import lombok.Data;

@Data
public class RealtimeTaskInstance {
    TaskInstance taskInstance;
    String applicationId;
}
