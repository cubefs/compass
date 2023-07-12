package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

import java.util.List;

@Data
public class JhCounters {
    private String name;
    private List<JhCounterGroup> groups;
}
