package com.xdev.xdevbase.repos;

import com.xdev.xdevbase.entities.BaseEntity;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity> extends JpaRepository<E, UUID>, RevisionRepository<E, UUID, Integer>, JpaSpecificationExecutor<E> {
  List<E> findByTenantId(UUID tenantId);
  Optional<E> findByIdAndTenantIdAndIsDeletedFalse(UUID id,UUID tenantId);
  List<E> findAllByTenantIdAndIsDeletedFalse(UUID tenantId);
  Page<E> findAllByTenantIdAndIsDeletedFalse(UUID tenantId, Pageable pageable);

}
