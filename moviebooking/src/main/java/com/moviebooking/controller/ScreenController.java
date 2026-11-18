package com.moviebooking.controller;

import com.moviebooking.entity.Screen;
import com.moviebooking.entity.Theatre;
import com.moviebooking.repository.ScreenRepository;
import com.moviebooking.repository.TheatreRepository;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/screens")
@CrossOrigin(origins = "*")
public class ScreenController {

    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;

    public ScreenController(
            ScreenRepository screenRepository,
            TheatreRepository theatreRepository) {
        this.screenRepository = screenRepository;
        this.theatreRepository = theatreRepository;
    }

    // Get all screens
    @GetMapping
    public List<Screen> getAllScreens() {
        return screenRepository.findAll();
    }

    // Add a screen to a theatre
    @PostMapping
    public Screen addScreen(@RequestBody ScreenRequest request) {

        Theatre theatre = theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new RuntimeException("Theatre not found"));

        Screen screen = new Screen();
        screen.setScreenName(request.getScreenName());
        screen.setTheatre(theatre);

        return screenRepository.save(screen);
    }

    // Request data
    public static class ScreenRequest {
        private String screenName;
        private Long theatreId;

        public String getScreenName() {
            return screenName;
        }

        public void setScreenName(String screenName) {
            this.screenName = screenName;
        }

        public Long getTheatreId() {
            return theatreId;
        }

        public void setTheatreId(Long theatreId) {
            this.theatreId = theatreId;
        }
    }
}