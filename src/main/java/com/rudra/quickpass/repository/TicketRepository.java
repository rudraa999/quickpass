package com.rudra.quickpass.repository;

import com.rudra.quickpass.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    boolean existsByTicketCode(String ticketCode);

    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.ticketCode = :ticketCode")
    Optional<Ticket> findByEventIdAndTicketCode(@Param("eventId") Long eventId, @Param("ticketCode") String ticketCode);

    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.booking b LEFT JOIN FETCH t.ticketTier tt WHERE t.event.id = :eventId ORDER BY t.id DESC")
    List<Ticket> findByEventIdWithDetails(@Param("eventId") Long eventId);

    List<Ticket> findByBookingIdOrderByIdAsc(Long bookingId);

    long countByEventId(Long eventId);

    long countByEventIdAndIsUsedTrue(Long eventId);

    long countByIsUsedTrue();

    long countByTicketTierId(Long ticketTierId);

    long countByTicketTierIdAndIsUsedTrue(Long ticketTierId);
}
