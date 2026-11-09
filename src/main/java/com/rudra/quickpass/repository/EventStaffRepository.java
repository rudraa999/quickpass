package com.rudra.quickpass.repository;

import com.rudra.quickpass.entity.EventStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventStaffRepository extends JpaRepository<EventStaff, Long> {
    List<EventStaff> findByEventIdOrderByAssignedAtDesc(Long eventId);
    List<EventStaff> findByStaffIdOrderByAssignedAtDesc(Long staffId);
    boolean existsByEventIdAndStaffId(Long eventId, Long staffId);
    boolean existsByStaffId(Long staffId);
    Optional<EventStaff> findByEventIdAndStaffId(Long eventId, Long staffId);
    void deleteByEventIdAndStaffId(Long eventId, Long staffId);
}

