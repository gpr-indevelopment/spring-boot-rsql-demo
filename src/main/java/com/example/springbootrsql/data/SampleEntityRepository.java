package com.example.springbootrsql.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SampleEntityRepository extends JpaRepository<SampleEntity, Long>, JpaSpecificationExecutor<SampleEntity> {
}
