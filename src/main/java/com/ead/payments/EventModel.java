package com.ead.payments;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.With;
import org.checkerframework.checker.units.qual.C;

@With
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "event_store", schema = "orders")
@Entity(name = "event_store")
public class EventModel {

    @Id
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = "created_at")
    private Date createdAt;

    @NotBlank
    @Column(name = "aggregated_identifier")
    private String aggregatedIdentifier;

    @NotBlank
    @Column(name = "aggregate_type")
    private String aggregateType;

    @Min(0)
    @Version
    @Column(name = "version")
    private int version;

    @NotBlank
    @Column(name = "event_type")
    private String eventType;

    @NotNull
    @Column(name = "event_data")
    @Convert(converter = BaseEventConverter.class)
    private BaseEvent eventData;

}
