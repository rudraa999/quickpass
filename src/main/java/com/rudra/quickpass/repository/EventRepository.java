package com.rudra.quickpass.repository;

import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByEventDateTimeAsc(EventStatus status);
    List<Event> findByStatusOrderByCreatedAtDesc(EventStatus status);
    List<Event> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);
    List<Event> findAllByOrderByCreatedAtDesc();
    long countByStatus(EventStatus status);
}

