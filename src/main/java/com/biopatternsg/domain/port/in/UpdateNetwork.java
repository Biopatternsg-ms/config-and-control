package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.infrastructure.dtos.UpdateNetworkRequest;

public interface UpdateNetwork {

    NetworkConfig execute(UpdateNetworkRequest updateNetwork);
}
