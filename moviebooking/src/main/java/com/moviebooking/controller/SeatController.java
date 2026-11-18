package com.moviebooking.controller;

import com.moviebooking.entity.Seat;
import com.moviebooking.entity.Screen;
import com.moviebooking.repository.SeatRepository;
import com.moviebooking.repository.ScreenRepository;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "*")
public class SeatController {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    public SeatController(
            SeatRepository seatRepository,
            ScreenRepository screenRepository) {

        this.seatRepository = seatRepository;
        this.screenRepository = screenRepository;
    }

    // Get all seats
    @GetMapping
    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    // Get seats belonging to a particular screen
    @GetMapping("/screen/{screenId}")
    public List<Seat> getSeatsByScreen(@PathVariable Long screenId) {
        return seatRepository.findByScreen_ScreenId(screenId);
    }

    // Add a seat
    @PostMapping
    public Seat addSeat(@RequestBody SeatRequest request) {

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found"));

        Seat seat = new Seat();
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());
        seat.setScreen(screen);

        return seatRepository.save(seat);
    }

    public static class SeatRequest {

        private Long screenId;
        private String seatNumber;
        private String seatType;

        public Long getScreenId() {
            return screenId;
        }

        public void setScreenId(Long screenId) {
            this.screenId = screenId;
        }

        public String getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }

        public String getSeatType() {
            return seatType;
        }

        public void setSeatType(String seatType) {
            this.seatType = seatType;
        }
    }
}