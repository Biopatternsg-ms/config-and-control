package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.NetworkConfig;

public interface CreateNetwork {

    NetworkConfig execute(NetworkConfig networkConfig);
}
