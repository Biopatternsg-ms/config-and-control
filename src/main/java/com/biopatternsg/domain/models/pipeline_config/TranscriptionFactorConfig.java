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
package com.biopatternsg.domain.models.pipeline_config;

import com.biopatternsg.domain.enums.Genome;
import com.biopatternsg.domain.enums.Strand;
import com.biopatternsg.domain.enums.TranscriptionFactorSource;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class TranscriptionFactorConfig {

    private String promoterRegion;
    private int reliability;
    private BigDecimal start;
    private BigDecimal end;
    private Genome genome;
    private String track;
    private Float identity;
    private String chromosome;
    private Strand strand;
    private List<TranscriptionFactorSource> sources;
}
