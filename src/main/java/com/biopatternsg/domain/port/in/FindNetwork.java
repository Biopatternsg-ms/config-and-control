package com.biopatternsg.domain.port.in;

import com.biopatternsg.domain.models.NetworkConfig;
import com.biopatternsg.infrastructure.dtos.FindNetworkRequest;

import java.util.List;

public interface FindNetwork {

    NetworkConfig byId(String id);
    NetworkConfig byName(String name);
    List<NetworkConfig> byFilters(FindNetworkRequest findNetwork);
}
