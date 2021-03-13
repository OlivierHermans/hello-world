package be.olivierhermans.helloworld.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomStatusRepository extends JpaRepository<CustomStatusDao, Integer> {
}
