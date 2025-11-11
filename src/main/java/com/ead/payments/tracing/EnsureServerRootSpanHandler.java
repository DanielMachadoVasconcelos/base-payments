package com.ead.payments.tracing;

import brave.Span.Kind;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * Grafana's built-in Zipkin datasource assumes that every trace contains at least one span without
 * a parent. When a request enters the service with client-side propagation headers (for example
 * when MockMvc or another instrumented client issues the call), Zipkin stores the server span with
 * the propagated parent id but the original client span might live in another service (or not be
 * exported at all). In those cases the datasource crashes when trying to dereference the missing
 * root span.
 *
 * To keep the traces explorable we normalise server spans so that they become roots whenever the
 * referenced parent id does not belong to any span that we are exporting in the current process.
 * The original parent id is still attached as a tag so cross-service correlation is not lost.
 */
@Component
public class EnsureServerRootSpanHandler extends SpanHandler {

    private final ConcurrentMap<String, Set<String>> spansPerTrace = new ConcurrentHashMap<>();

    @Override
    public boolean begin(TraceContext context, MutableSpan span, TraceContext parent) {
        spansPerTrace
                .computeIfAbsent(context.traceIdString(), newTraceSpanSet())
                .add(span.id());
        return true;
    }

    @Override
    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
        if (span.kind() == Kind.SERVER) {
            String parentId = span.parentId();
            if (parentId != null) {
                Set<String> knownSpanIds = spansPerTrace.get(context.traceIdString());
                boolean parentKnown = knownSpanIds != null && knownSpanIds.contains(parentId);
                if (!parentKnown) {
                    span.tag("remote.parent.id", parentId);
                    span.parentId(null);
                }
            }
        }

        Set<String> knownSpanIds = spansPerTrace.get(context.traceIdString());
        if (knownSpanIds != null) {
            knownSpanIds.remove(span.id());
            if (knownSpanIds.isEmpty()) {
                spansPerTrace.remove(context.traceIdString());
            }
        }

        return true;
    }

    private Function<String, Set<String>> newTraceSpanSet() {
        return ignored -> ConcurrentHashMap.newKeySet();
    }
}
