package com.ead.payments;

import java.util.List;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventStoreRepository extends ListPagingAndSortingRepository<EventModel, String>,
        ListCrudRepository<EventModel, String> {

    List<EventModel> findByAggregatedIdentifier(String aggregatedIdentifier);
}
