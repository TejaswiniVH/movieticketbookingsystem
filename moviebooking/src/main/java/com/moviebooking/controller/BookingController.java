package com.moviebooking.controller;

import com.moviebooking.entity.Booking;
import com.moviebooking.entity.BookedSeat;
import com.moviebooking.entity.User;

import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.BookedSeatRepository;
import com.moviebooking.repository.UserRepository;

import com.moviebooking.service.BookingService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(
    origins = "http://127.0.0.1:5500",
    allowCredentials = "true"
)
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final UserRepository userRepository;

    // SINGLE CONSTRUCTOR
    public BookingController(
            BookingService bookingService,
            BookingRepository bookingRepository,
            BookedSeatRepository bookedSeatRepository,
            UserRepository userRepository) {

        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.bookedSeatRepository = bookedSeatRepository;
        this.userRepository = userRepository;
    }

    // CREATE BOOKING
    @PostMapping
    public Booking createBooking(@RequestBody BookingRequest request) {

        return bookingService.createBooking(
                request.getShowId(),
                request.getSeatIds(),
                request.getUserId()
        );
    }

    // GET BOOKED SEAT IDs FOR A PARTICULAR SHOW
    @GetMapping("/show/{showId}/booked-seats")
    public List<Long> getBookedSeatIds(@PathVariable Long showId) {

        List<BookedSeat> bookedSeats =
                bookedSeatRepository.findByShow_ShowId(showId);

        return bookedSeats.stream()
                .map(bookedSeat -> bookedSeat.getSeat().getSeatId())
                .toList();
    }

    // GET BOOKING HISTORY FOR A USER
    @GetMapping("/user/{userId}/history")
    public List<Map<String, Object>> getUserBookingHistory(
            @PathVariable Long userId) {

        List<Booking> bookings =
                bookingRepository.findByUserId(userId);

        List<Map<String, Object>> history = new ArrayList<>();

        for (Booking booking : bookings) {

            Map<String, Object> details = new HashMap<>();

            List<BookedSeat> bookedSeats =
                    bookedSeatRepository.findByBooking_BookingId(
                            booking.getBookingId()
                    );

            List<String> seatNumbers = new ArrayList<>();

            for (BookedSeat bookedSeat : bookedSeats) {
                seatNumbers.add(
                        bookedSeat.getSeat().getSeatNumber()
                );
            }

            details.put("bookingId", booking.getBookingId());
            details.put("userId", booking.getUserId());
            details.put("movie",
                    booking.getShow().getMovie().getTitle());
            details.put("theatre",
                    booking.getShow().getScreen()
                            .getTheatre().getName());
            details.put("seats", seatNumbers);
            details.put("date", booking.getShow().getShowDate());
            details.put("time", booking.getShow().getShowTime());
            details.put("amount", booking.getTotalAmount());
            details.put("status", booking.getStatus());
            details.put("bookingDate", booking.getBookingDate());

            history.add(details);
        }

        return history;
    }

    // CANCEL BOOKING
    @PutMapping("/{bookingId}/cancel")
    @Transactional
    public Map<String, Object> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));

        if (!userId.equals(booking.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot cancel this booking"
            );
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Booking is already cancelled"
            );
        }

        List<BookedSeat> bookedSeats =
                bookedSeatRepository.findByBooking_BookingId(bookingId);

        bookedSeatRepository.deleteAll(bookedSeats);

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        Map<String, Object> result = new HashMap<>();

        result.put("message", "Booking cancelled successfully");
        result.put("bookingId", bookingId);
        result.put("status", "CANCELLED");

        return result;
    }

    // GET ALL BOOKINGS
    @GetMapping
    public List<Booking> getAllBookings() {

        return bookingRepository.findAll();
    }

    // GET BOOKINGS FOR A PARTICULAR USER
    @GetMapping("/user/{userId}")
    public List<Booking> getBookingsByUser(
            @PathVariable Long userId) {

        return bookingRepository.findByUserId(userId);
    }

    // GET TICKET DETAILS USING BOOKING ID
    @GetMapping("/{bookingId}/ticket")
    public Map<String, Object> getTicket(
            @PathVariable Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        List<BookedSeat> bookedSeats =
                bookedSeatRepository.findByBooking_BookingId(bookingId);

        List<String> seatNumbers = new ArrayList<>();

        for (BookedSeat bookedSeat : bookedSeats) {
            seatNumbers.add(
                    bookedSeat.getSeat().getSeatNumber()
            );
        }

        Map<String, Object> ticket = new HashMap<>();

        ticket.put("bookingId", booking.getBookingId());
        ticket.put("userId", booking.getUserId());

        ticket.put("movie",
                booking.getShow().getMovie().getTitle());

        ticket.put("theatre",
                booking.getShow().getScreen()
                        .getTheatre().getName());

        ticket.put("seats", seatNumbers);
        ticket.put("date", booking.getShow().getShowDate());
        ticket.put("time", booking.getShow().getShowTime());
        ticket.put("amount", booking.getTotalAmount());
        ticket.put("status", booking.getStatus());
        ticket.put("bookingDate", booking.getBookingDate());

        return ticket;
    }

    // ADMIN REPORT - ADMIN ACCESS ONLY
    @GetMapping("/admin-report")
    public ResponseEntity<?> getAdminReport(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        // CHECK LOGIN
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        // FIND USER IN DATABASE
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            session.invalidate();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "User not found. Please log in again."
                    ));
        }

        // CHECK ADMIN ROLE
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "Access denied. Admins only."
                    ));
        }

        // GENERATE ADMIN REPORT
        List<Booking> bookings = bookingRepository.findAll();

        List<Map<String, Object>> report = new ArrayList<>();

        for (Booking booking : bookings) {

            Map<String, Object> details = new HashMap<>();

            List<BookedSeat> bookedSeats =
                    bookedSeatRepository.findByBooking_BookingId(
                            booking.getBookingId()
                    );

            List<String> seatNumbers = new ArrayList<>();

            for (BookedSeat bookedSeat : bookedSeats) {
                seatNumbers.add(
                        bookedSeat.getSeat().getSeatNumber()
                );
            }

            details.put("bookingId", booking.getBookingId());
            details.put("userId", booking.getUserId());
            details.put("movie",
                    booking.getShow().getMovie().getTitle());
            details.put("theatre",
                    booking.getShow().getScreen()
                            .getTheatre().getName());
            details.put("seats", seatNumbers);
            details.put("date", booking.getShow().getShowDate());
            details.put("time", booking.getShow().getShowTime());
            details.put("amount", booking.getTotalAmount());
            details.put("status", booking.getStatus());
            details.put("bookingDate", booking.getBookingDate());

            report.add(details);
        }

        return ResponseEntity.ok(report);
    }

    // BOOKING REQUEST CLASS
    public static class BookingRequest {

        private Long showId;
        private List<Long> seatIds;
        private Long userId;

        public Long getShowId() {
            return showId;
        }

        public void setShowId(Long showId) {
            this.showId = showId;
        }

        public List<Long> getSeatIds() {
            return seatIds;
        }

        public void setSeatIds(List<Long> seatIds) {
            this.seatIds = seatIds;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}