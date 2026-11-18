package com.moviebooking.repository;

import com.moviebooking.entity.BookedSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {

    List<BookedSeat> findByShow_ShowId(Long showId);

    boolean existsByShow_ShowIdAndSeat_SeatId(Long showId, Long seatId);

    List<BookedSeat> findByBooking_BookingId(Long bookingId);
}