package com.moviebooking.controller;

import com.moviebooking.entity.Show;
import com.moviebooking.entity.Movie;
import com.moviebooking.entity.Screen;

import com.moviebooking.repository.ShowRepository;
import com.moviebooking.repository.MovieRepository;
import com.moviebooking.repository.ScreenRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
@CrossOrigin(origins = "*")
public class ShowController {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public ShowController(
            ShowRepository showRepository,
            MovieRepository movieRepository,
            ScreenRepository screenRepository) {

        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
    }

    // Get all shows
    @GetMapping
    public List<Show> getAllShows() {
        return showRepository.findAll();
    }

    // Add a new show
    @PostMapping
    public Show addShow(@RequestBody ShowRequest request) {

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found"));

        Show show = new Show();

        show.setMovie(movie);
        show.setScreen(screen);
        show.setShowDate(request.getShowDate());
        show.setShowTime(request.getShowTime());
        show.setTicketPrice(request.getTicketPrice());

        return showRepository.save(show);
    }

    // Request data
    public static class ShowRequest {

        private Long movieId;
        private Long screenId;
        private java.time.LocalDate showDate;
        private java.time.LocalTime showTime;
        private java.math.BigDecimal ticketPrice;

        public Long getMovieId() {
            return movieId;
        }

        public void setMovieId(Long movieId) {
            this.movieId = movieId;
        }

        public Long getScreenId() {
            return screenId;
        }

        public void setScreenId(Long screenId) {
            this.screenId = screenId;
        }

        public java.time.LocalDate getShowDate() {
            return showDate;
        }

        public void setShowDate(java.time.LocalDate showDate) {
            this.showDate = showDate;
        }

        public java.time.LocalTime getShowTime() {
            return showTime;
        }

        public void setShowTime(java.time.LocalTime showTime) {
            this.showTime = showTime;
        }

        public java.math.BigDecimal getTicketPrice() {
            return ticketPrice;
        }

        public void setTicketPrice(java.math.BigDecimal ticketPrice) {
            this.ticketPrice = ticketPrice;
        }
    }
}