package com.ead.payments.orders;

import com.ead.payments.BaseCommand;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderCommand  extends BaseCommand {

    private UUID id;
    private String currency;
    private Long amount;
}
