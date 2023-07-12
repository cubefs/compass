package com.oppo.cloud.parser.domain.mr.event;

import lombok.Data;

@Data
public class JhCounter {
    private String name;
    private String displayName;
    private Long value;
}
