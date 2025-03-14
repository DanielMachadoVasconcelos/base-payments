package com.ead.payments.tracer;

import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class TracerConfiguration {

    @Bean
    public Tracer tracer() {
        // Configura o contexto de trace local
        CurrentTraceContext braveCurrentTraceContext = ThreadLocalCurrentTraceContext.newBuilder().build();

        // Inicializa o Brave Tracing
        Tracing braveTracing = Tracing.newBuilder()
                .currentTraceContext(braveCurrentTraceContext)
                .sampler(Sampler.ALWAYS_SAMPLE) // Sempre coletar traces (ideal para testes)
                .build();

        // Criar o Bridge do Brave para Micrometer
        BraveCurrentTraceContext braveTraceContext = new BraveCurrentTraceContext(braveCurrentTraceContext);
        BraveBaggageManager braveBaggageManager = new BraveBaggageManager();

        return new BraveTracer(
                braveTracing.tracer(),
                braveTraceContext,
                braveBaggageManager
        );
    }
}