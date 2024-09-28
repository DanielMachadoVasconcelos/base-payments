package com.ead.payments.orders;


import com.ead.payments.BaseEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.modulith.events.Externalized;

@Data
@ToString
@Externalized
@NoArgsConstructor
@JsonTypeName("order_placed")
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class OrderPlacedEvent extends BaseEvent {

    private UUID id;
    private String currency;
    private Long amount;

}
