package com.ead.payments.orders.place.response;

import com.ead.payments.orders.Order.OrderStatus;
import com.ead.payments.orders.response.LineItemResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponseV2 {

    private UUID id;
    private @NotNull Long version;
    private @NotNull OrderStatus status;
    private @NotBlank Currency currency;
    private @NotNull Long amount;
    private @NotNull List<LineItemResponse> lineItems;

}
