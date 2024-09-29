package com.ead.payments.orders;

import com.ead.payments.BaseCommand;
import com.ead.payments.CommandDispatcher;
import com.ead.payments.CommandHandlerMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

@Service
@AllArgsConstructor
public class OrderCommandDispatcher implements CommandDispatcher {

    private final Map<Class<? extends BaseCommand>, List<CommandHandlerMethod>> routes = new HashMap<>();

    @Override
    public <T extends BaseCommand> void registerHandler(Class<T> type, CommandHandlerMethod<T> handler) {
        var handlers = routes.computeIfAbsent(type, c -> new LinkedList<>());
        handlers.add(handler);
    }

    @Override
    public void send(BaseCommand command) {
        var handlers = routes.get(command.getClass());
        checkArgument(handlers != null,"No command handler is registered!");
        checkState(!handlers.isEmpty(), "No command handler is registered!");
        checkState(handlers.size() == 1, "Can't send to more then one handler!");
        handlers.get(0).handle(command);
    }
}
