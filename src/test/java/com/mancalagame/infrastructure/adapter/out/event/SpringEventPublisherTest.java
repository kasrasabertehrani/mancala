package com.mancalagame.infrastructure.adapter.out.event;

import com.mancalagame.domain.event.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class SpringEventPublisherTest {

    @Mock
    private ApplicationEventPublisher springPublisher;

    @Mock
    private DomainEvent event;

    @InjectMocks
    private SpringEventPublisherAdapter adapter;

    @Test
    void publish_shouldForwardSameEventInstance() {
        adapter.publish(event);

        verify(springPublisher).publishEvent(same(event));
        verifyNoMoreInteractions(springPublisher);
    }
}
