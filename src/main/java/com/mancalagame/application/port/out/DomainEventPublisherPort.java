package com.mancalagame.application.port.out;

import com.mancalagame.domain.event.DomainEvent;

public interface DomainEventPublisherPort {
    void publish(DomainEvent event);
}