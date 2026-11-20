/*
 * Copyright © 2026 biopatternsg (biopatternsg@gmail.com)
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
package com.biopatternsg.application.services;

import com.biopatternsg.domain.models.ExperimentExecutionResponse;
import com.biopatternsg.domain.models.PipelineConfig;

public interface PipelineExecutionService {

    /**
     * Constructs a consolidated execution report for the given pipeline,
     * aggregating execution durations, timeline steps, step metrics,
     * and UI icon/description mappings.
     *
     * @param pipeline the pipeline configuration aggregate
     * @return the complete execution response
     */
    ExperimentExecutionResponse buildExecutionReport(PipelineConfig pipeline);
}
