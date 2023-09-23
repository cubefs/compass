/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.common.domain.gc;

import com.oppo.cloud.common.util.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("GC日志分析")
public class GCReport {

    /**
     * gc日志对应的executor id
     */
    @ApiModelProperty(value = "gc日志对应的executor id")
    private Integer executorId;
    /**
     * 最大分配内存
     */
    @ApiModelProperty(value = "最大分配内存")
    private Integer maxHeapAllocatedSize;
    /**
     * 最大内存使用
     */
    @ApiModelProperty(value = "最大内存使用")
    private Integer maxHeapUsedSize;
    /**
     * 运行时间
     */
    @ApiModelProperty(value = "运行时间")
    private String totalTime;
    /**
     * young gc 次数
     */
    @ApiModelProperty(value = "young gc 次数")
    private Integer youngGCCount;
    /**
     * young gc 时间
     */
    @ApiModelProperty(value = "young gc 时间")
    private Double youngGCTime;
    /**
     * full gc 次数
     */
    @ApiModelProperty(value = "full gc 次数")
    private Integer fullGCCount;
    /**
     * full gc 时间
     */
    @ApiModelProperty(value = "full gc 时间")
    private Double fullGCTime;
    /**
     * gc 总次数
     */
    @ApiModelProperty(value = "gc 总次数")
    private Integer totalGCCount;
    /**
     * gc 总时间
     */
    @ApiModelProperty(value = "gc 总时间")
    private Double totalGCTime;
    /**
     * heap使用趋势图
     */
    @ApiModelProperty(value = "heap使用趋势图")
    private List<HeapUsed> heapUsed;
    /**
     * tenured使用趋势图
     */
    @ApiModelProperty(value = "tenured使用趋势图")
    private List<TenuredUsed> tenuredUsed;
    /**
     * young使用趋势图
     */
    @ApiModelProperty(value = "young使用趋势图")
    private List<YoungUsed> youngUsed;
    /**
     * appId
     */
    @ApiModelProperty(value = "appId")
    private String applicationId;

    /**
     * 日志类型 driver executor
     */
    @ApiModelProperty(value = "日志类型 driver executor")
    private String logType;
    /**
     * hdfs path
     */
    @ApiModelProperty(value = "hdfs path")
    private String logPath;

    /**
     * executor的内存使用
     */
    @ApiModelProperty(value = "executor的内存使用")
    private List<ExecutorPeakMemory> executorPeakMemory;

    public Map<String, Object> genDoc() throws Exception {
        Map<String, Object> res = new HashMap<>();
        Field[] fileds = this.getClass().getDeclaredFields();
        for (Field field : fileds) {
            String key = field.getName();
            String method = key.substring(0, 1).toUpperCase() + key.substring(1);
            Method getMethod = this.getClass().getMethod("get" + method);
            res.put(key, getMethod.invoke(this));
        }
        return res;
    }

}
