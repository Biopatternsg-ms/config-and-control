package com.biopatternsg.infrastructure.clients;

import com.biopatternsg.infrastructure.dtos.BuildPairsRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "pubmed-integration-api")
public interface PubmedRestClient {

    @POST
    @Path("/pubmed/build-pairs")
    void buildPairs(BuildPairsRequest request);
}
