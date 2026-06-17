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
package com.biopatternsg.domain.enums;

import lombok.Getter;

@Getter
public enum PipelineSteps {

    CONFIG("configuration", PipelineStage.INIT),
    LAUNCH("launched", PipelineStage.INIT),
    TRANSCRIPTION_FACTOR("transcription_factor", PipelineStage.BIOLOGICAL_OBJECT),
    EXPERT_OBJECTS("expert_objects", PipelineStage.BIOLOGICAL_OBJECT),
    SEARCH_LEVELS("search_levels", PipelineStage.BIOLOGICAL_OBJECT),
    COMBINATIONS("combinations", PipelineStage.PUBMED_INTEGRATION),
    SEARCH_PUBMED_IDS("search_pubmed_ids", PipelineStage.PUBMED_INTEGRATION),
    SEARCH_PUBTATOR("search_pubtator", PipelineStage.PUBMED_INTEGRATION);

    private final String value;
    private final PipelineStage stage;

    PipelineSteps(String value, PipelineStage stage) {
        this.value = value;
        this.stage = stage;
    }

}
