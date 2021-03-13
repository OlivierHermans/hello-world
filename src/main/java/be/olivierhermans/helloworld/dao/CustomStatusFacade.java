package be.olivierhermans.helloworld.dao;

import be.olivierhermans.helloworld.dao.mapper.CustomStatusDaoMapper;
import be.olivierhermans.helloworld.model.CustomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomStatusFacade {

    private final CustomStatusRepository repository;
    private final CustomStatusDaoMapper mapper;

    public CustomStatus createCustomStatus(CustomStatus customStatus) {
        return mapper.daoToModel(repository.save(mapper.modelToDao(customStatus)));
    }
}
