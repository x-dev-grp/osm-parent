package com.xdev.xdevbase.repos;

import com.xdev.xdevbase.entities.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository <E extends BaseEntity> extends JpaRepository<E, UUID>, RevisionRepository<E,UUID,Integer>, JpaSpecificationExecutor<E> {
}
