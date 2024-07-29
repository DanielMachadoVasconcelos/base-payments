package com.ead.payments.orders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Date;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@Table(name = "orders", schema = "orders")
public class OrderEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Version
    @Column(name = "version")
    private Long version;

    @Lob
    @Column(name = "payload")
    private byte[] payload;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    private Date createdAt;

    @LastModifiedDate
    @Column(name = "modified_at")
    private Date modifiedAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "modified_by")
    private String modifiedBy;
}