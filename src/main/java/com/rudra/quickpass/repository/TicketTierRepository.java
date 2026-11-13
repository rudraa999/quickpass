package com.rudra.quickpass.repository;

import com.rudra.quickpass.entity.TicketTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTierRepository extends JpaRepository<TicketTier, Long> {

    List<TicketTier> findByEventIdOrderByIdAsc(Long eventId);

    Optional<TicketTier> findByEventIdAndName(Long eventId, String name);
}

