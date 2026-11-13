package com.rudra.quickpass.repository;

import com.rudra.quickpass.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByAttendeeIdOrderByBookedAtDesc(Long attendeeId);
    List<Booking> findByEventIdOrderByBookedAtDesc(Long eventId);
    List<Booking> findByEventOrganizerIdOrderByBookedAtDesc(Long organizerId);
    Optional<Booking> findByOrderNumber(String orderNumber);
    List<Booking> findAllByOrderByBookedAtDesc();
    long countByAttendeeId(Long attendeeId);
    long countByEventId(Long eventId);
    boolean existsByEventId(Long eventId);
}
