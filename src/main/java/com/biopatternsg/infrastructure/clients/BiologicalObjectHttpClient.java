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
package com.biopatternsg.infrastructure.clients;

import com.biopatternsg.infrastructure.dtos.LaunchPipelineInternalRequest;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "biological-object-api")
public interface BiologicalObjectHttpClient {

    @POST
    @Path("/biological-object/launch-pipeline")
    String launch(@RequestBody LaunchPipelineInternalRequest pipelineRequest, @HeaderParam("x-user-id") String userId);

    @POST
    @Path("/biological-object/update-synonyms/{pipelineId}")
    void updateSynonyms(@PathParam("pipelineId") String pipelineId, @HeaderParam("x-user-id") String userId);
}
