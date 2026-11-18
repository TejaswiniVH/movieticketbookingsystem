package com.moviebooking.service;

import com.moviebooking.entity.*;
import com.moviebooking.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            ShowRepository showRepository,
            SeatRepository seatRepository,
            BookedSeatRepository bookedSeatRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(Long showId, List<Long> seatIds, Long userId) {

        // Check whether user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new RuntimeException("User not found"));

        // Check whether show exists
        Show show = showRepository.findById(showId)
                .orElseThrow(() ->
                    new RuntimeException("Show not found"));

        if (seatIds == null || seatIds.isEmpty()) {
            throw new RuntimeException("Please select at least one seat");
        }

        if (seatIds.size() != seatIds.stream().distinct().count()) {
            throw new RuntimeException("Duplicate seats selected");
        }

        List<Seat> selectedSeats = new ArrayList<>();

        for (Long seatId : seatIds) {

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() ->
                        new RuntimeException("Seat not found: " + seatId));

            if (!seat.getScreen().getScreenId()
                    .equals(show.getScreen().getScreenId())) {

                throw new RuntimeException(
                    "Seat does not belong to this show's screen");
            }

            if (bookedSeatRepository
                    .existsByShow_ShowIdAndSeat_SeatId(showId, seatId)) {

                throw new RuntimeException(
                    "Seat " + seat.getSeatNumber() + " is already booked");
            }

            selectedSeats.add(seat);
        }

        // Calculate total amount
        BigDecimal totalAmount = show.getTicketPrice()
                .multiply(BigDecimal.valueOf(selectedSeats.size()));

        // Create booking
        Booking booking = new Booking();

        booking.setUserId(user.getId());
        booking.setShow(show);
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");

        booking = bookingRepository.save(booking);

        // Save booked seats
        for (Seat seat : selectedSeats) {

            BookedSeat bookedSeat = new BookedSeat();

            bookedSeat.setShow(show);
            bookedSeat.setSeat(seat);
            bookedSeat.setBooking(booking);

            bookedSeatRepository.save(bookedSeat);
        }

        return booking;
    }
}