package com.oppo.cloud.parser.domain.mr;

import com.oppo.cloud.parser.domain.reader.ReaderObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobHistoryFileInfo {
    private ReaderObject jobHistoryReader;
    private ReaderObject confReader;
}
