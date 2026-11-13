package com.rudra.quickpass.repository;

import com.rudra.quickpass.entity.Event;
import com.rudra.quickpass.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByEventDateTimeAsc(EventStatus status);
    List<Event> findByStatusOrderByCreatedAtDesc(EventStatus status);
    List<Event> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);
    long countByOrganizerId(Long organizerId);
    List<Event> findAllByOrderByCreatedAtDesc();
    long countByStatus(EventStatus status);

    @Query("SELECT e FROM Event e JOIN e.staffMembers s WHERE s.id = :staffId ORDER BY e.eventDateTime ASC")
    List<Event> findEventsByStaffId(@Param("staffId") Long staffId);

    @Query("SELECT COUNT(e) > 0 FROM Event e JOIN e.staffMembers s WHERE e.id = :eventId AND s.id = :userId")
    boolean isUserStaffForEvent(@Param("eventId") Long eventId, @Param("userId") Long userId);
}

