package be.olivierhermans.helloworld.dao;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "custom_status")
@Getter
@Builder
public class CustomStatusDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private int id;

    @Column(name = "status")
    private String status;
}
