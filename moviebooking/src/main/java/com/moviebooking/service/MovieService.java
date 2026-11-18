package com.moviebooking.service;

import com.moviebooking.entity.Movie;
import com.moviebooking.repository.MovieRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Add a movie
    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // Get all movies
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // Get movie by ID
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
    }

    // Delete a movie
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}