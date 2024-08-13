package com.ead.payments.confidentiality;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SensitiveDataMaskListener implements PreLoadEventListener {

    @Override
    public void onPreLoad(PreLoadEvent event) {
        Optional.ofNullable(event)
                .map(PreLoadEvent::getEntity)
                .map(entity -> FieldUtils.getFieldsListWithAnnotation(entity.getClass(), SensitiveData.class))
                .stream()
                .flatMap(Collection::stream)
                .filter(this::shouldHideSensitiveData)
                .forEach(field -> this.hideSensitiveData(event, field));
    }

    private void hideSensitiveData(PreLoadEvent event, Field field) {
        int propertyIndex = Arrays.asList(event.getPersister().getPropertyNames()).indexOf(field.getName());

        if (event.getState()[propertyIndex] == null) {
            return;
        }

        event.getState()[propertyIndex] = null;
    }

    private boolean shouldHideSensitiveData(Field field) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Predicate<GrantedAuthority> hasAccessToSensitiveData =
                authority -> "READ_SENSITIVE_DATA".equalsIgnoreCase(authority.getAuthority());

        return !IterableUtils.matchesAny(authentication.getAuthorities(), hasAccessToSensitiveData);
    }
}
