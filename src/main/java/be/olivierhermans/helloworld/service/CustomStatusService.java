package be.olivierhermans.helloworld.service;

import be.olivierhermans.helloworld.dao.CustomStatusFacade;
import be.olivierhermans.helloworld.model.CustomStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomStatusService {

    private final CustomStatusFacade facade;

    public void createCustomStatus(String status) {
        facade.createCustomStatus(CustomStatus.builder().status(status).build());
    }
}
