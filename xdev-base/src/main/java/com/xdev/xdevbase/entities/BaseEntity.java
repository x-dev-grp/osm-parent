package com.xdev.xdevbase.entities;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;


@Audited
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "DELETED", updatable = false)
    private Boolean isDeleted=false;

    @CreatedBy
    @Column(name = "CREATED_BY", updatable = false)
    private String createdBy;

    @Column(name = "CREATE_DATE_TIME", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "MODIFIED_BY")
    private String lastModifiedBy;

    @Column(name = "MODIFIED_DATE_TIME")
    private LocalDateTime lastModifiedDate;


    public String getCreatedBy() {
        return createdBy;
    }


    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @PrePersist
    protected void onCreate() {
        setCreatedDate(LocalDateTime.now());
        setLastModifiedDate(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setLastModifiedDate(LocalDateTime.now());
    }

}
