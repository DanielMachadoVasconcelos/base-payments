package com.ead.payments.orders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

@With
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@Getter(onMethod = @__(@JsonProperty))
public class OrderPayload {

    private String currency;
    private Long amount;


}
