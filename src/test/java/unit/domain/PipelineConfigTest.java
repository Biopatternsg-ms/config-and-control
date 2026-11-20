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
package unit.domain;

import com.biopatternsg.domain.enums.PipelineSteps;
import com.biopatternsg.domain.enums.Status;
import com.biopatternsg.domain.models.PipelineConfig;
import com.biopatternsg.domain.models.PipelineStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineConfigTest {

    @Test
    @DisplayName("isConfigComplete should return true only when all required config fields are present")
    void isConfigComplete_allFieldsPresent_returnsTrue() {
        PipelineConfig config = PipelineConfig.builder()
                .name("Cancer Network Pipeline")
                .description("Pipeline description")
                .levels(2)
                .retMax(100)
                .maxComplexes(10)
                .build();

        assertTrue(config.isConfigComplete());

        config.applyConfigCompletionIfEligible();

        assertNotNull(config.getStatuses());
        PipelineStatus status = config.getStatuses().stream()
                .filter(ps -> ps.getStep() == PipelineSteps.CONFIG && ps.getStatus() == Status.COMPLETED)
                .findFirst()
                .orElse(null);

        assertNotNull(status, "CONFIG status COMPLETED must be added");
    }

    @Test
    @DisplayName("isConfigComplete should return false when any mandatory field is missing")
    void isConfigComplete_missingFields_returnsFalse() {
        PipelineConfig config = PipelineConfig.builder()
                .name("   ")
                .description("Desc")
                .levels(2)
                .retMax(100)
                .maxComplexes(10)
                .statuses(new ArrayList<>())
                .build();

        assertFalse(config.isConfigComplete());

        config.applyConfigCompletionIfEligible();
        assertTrue(config.getStatuses().isEmpty(), "No COMPLETED status should be added");
    }

    @Test
    @DisplayName("applyAlignedObjectsConfirmation should set step, aligned objects and generate metrics")
    void applyAlignedObjectsConfirmation_validObjects_generatesMetricsAndStatus() {
        PipelineConfig config = PipelineConfig.builder()
                .id("pipe-1")
                .statuses(new ArrayList<>())
                .build();

        List<String> symbols = List.of("TP53", "BRCA1", "EGFR");
        config.applyAlignedObjectsConfirmation(symbols);

        assertEquals(symbols, config.getAlignedExpertObjects());
        assertEquals(PipelineSteps.UPDATE_ALIGNED_OBJECTS, config.getStep());

        PipelineStatus status = config.getStatuses().stream()
                .filter(ps -> ps.getStep() == PipelineSteps.UPDATE_ALIGNED_OBJECTS && ps.getStatus() == Status.COMPLETED)
                .findFirst()
                .orElse(null);

        assertNotNull(status);
        assertNotNull(status.getMetrics());
        assertEquals("3", status.getMetrics().get("totalAlignedObjects"));
        assertEquals("TP53, BRCA1, EGFR", status.getMetrics().get("alignedSymbols"));
        assertEquals("Manual alignment confirmed by expert", status.getMetrics().get("statusMessage"));
    }
}
