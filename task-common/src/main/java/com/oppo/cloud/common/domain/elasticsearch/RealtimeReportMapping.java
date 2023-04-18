package com.oppo.cloud.common.domain.elasticsearch;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RealtimeReportMapping extends Mapping{

    public static Map<String, Object> build(boolean sourceEnabled) {
        return Stream.of(
                new AbstractMap.SimpleEntry<>("properties", build()),
                new AbstractMap.SimpleEntry<>("_source", enabled(sourceEnabled))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Object> build() {
        return Stream.of(
                /* doc_id */
                new AbstractMap.SimpleEntry<>("doc_id", text()),
                /* report */
                new AbstractMap.SimpleEntry<>("report", text())
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
