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
    Optional<Booking> findByTicketCode(String ticketCode);
    Optional<Booking> findByEventIdAndTicketCode(Long eventId, String ticketCode);
    boolean existsByTicketCode(String ticketCode);
    List<Booking> findAllByOrderByBookedAtDesc();
    long countByIsUsedTrue();
    long countByAttendeeId(Long attendeeId);
    long countByEventId(Long eventId);
    boolean existsByEventId(Long eventId);
}

