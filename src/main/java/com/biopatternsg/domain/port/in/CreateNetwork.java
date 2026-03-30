package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.infrastructure.dtos.CreateNetworkRequest;

public interface CreateNetwork {

    NetworkConfig execute(CreateNetworkRequest createNetwork);
}
