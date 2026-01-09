package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.NetworkConfig;

public interface UpdateNetwork {

    NetworkConfig execute(NetworkConfig networkConfig);
}
