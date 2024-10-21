package com.food.ordering.system.order.service.domain.valueobject;

import com.food.ordering.system.domain.valueobject.BaseId;

import java.util.UUID;

public class TackingId extends BaseId<UUID> {
    public TackingId(UUID value) {
        super(value);
    }
}
