package com.rudra.quickpass.repository;

import com.rudra.quickpass.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByAttendeeIdOrderByBookedAtDesc(Long attendeeId);
    Optional<Booking> findByTicketCode(String ticketCode);
    boolean existsByTicketCode(String ticketCode);
}

