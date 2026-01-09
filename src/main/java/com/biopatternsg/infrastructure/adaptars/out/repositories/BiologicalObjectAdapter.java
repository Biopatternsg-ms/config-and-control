package com.biopatternsg.infrastructure.adaptars.out.repositories;

import com.biopatternsg.domain.port.out.repositories.BiologicalObjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class BiologicalObjectAdapter implements BiologicalObjectRepository {

    @Override
    public void launch() {

    }
}
