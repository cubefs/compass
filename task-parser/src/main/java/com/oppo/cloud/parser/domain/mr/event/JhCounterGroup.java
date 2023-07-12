package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

import java.util.List;

@Data
public class JhCounterGroup {
    private String name;
    private String displayName;
    private List<JhCounter> counts;
}
