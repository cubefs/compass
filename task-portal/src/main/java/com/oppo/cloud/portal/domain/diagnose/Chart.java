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

package com.oppo.cloud.portal.domain.diagnose;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("图表类")
public class Chart<T> {

    @ApiModelProperty(value = "横坐标")
    private String x;

    @ApiModelProperty(value = "纵坐标")
    private String y;

    @ApiModelProperty(value = "纵坐标单位")
    private String unit;

    @ApiModelProperty(value = "图表数据")
    private List<T> dataList = new ArrayList<>();

    @ApiModelProperty(value = "数据分类说明(不同数据展示不同颜色)")
    private Map<String, ChartInfo> dataCategory;

    @ApiModelProperty(value = "图表说明")
    private String des;

    @Data
    public static class ChartInfo {

        private String name;
        private String color;

        public ChartInfo(String name, String color) {
            this.color = color;
            this.name = name;
        }
    }
}
