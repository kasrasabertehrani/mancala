package com.mancalagame.infrastructure.adapter.out.event;

import com.mancalagame.application.port.out.DomainEventPublisherPort;
import com.mancalagame.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisherAdapter implements DomainEventPublisherPort {

    private final ApplicationEventPublisher springPublisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        // We translate the pure Java call into a Spring Framework call
        springPublisher.publishEvent(event);
    }
}
