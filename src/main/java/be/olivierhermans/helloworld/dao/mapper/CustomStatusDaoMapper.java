package be.olivierhermans.helloworld.dao.mapper;

import be.olivierhermans.helloworld.dao.CustomStatusDao;
import be.olivierhermans.helloworld.model.CustomStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomStatusDaoMapper {

    CustomStatusDao modelToDao(CustomStatus customStatus);

    CustomStatus daoToModel(CustomStatusDao customStatusDao);
}
