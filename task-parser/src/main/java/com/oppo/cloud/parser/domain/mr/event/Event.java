package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class Event {
    EventType type;
    String event;
}
