package com.movieflix.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import com.movieflix.exceptions.EmptyFileException;
import com.movieflix.exceptions.MovieNotFoundException;
import com.movieflix.service.MovieService;
import com.movieflix.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovieHandler(@RequestPart MultipartFile file,
                                                    @RequestPart String MovieDto) throws IOException, EmptyFileException {
        if (file.isEmpty()){
            throw new EmptyFileException("File không tồn tại, hãy tải lại file khác");
        }
        MovieDto dto = convertToMovieDto(MovieDto);
        return new ResponseEntity<>(movieService.addMovie(dto,file), HttpStatus.CREATED);
    }
    @GetMapping("{idMovie}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer idMovie) throws MovieNotFoundException {
        return ResponseEntity.ok(movieService.getMovie(idMovie));
    }
    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMovieHandler(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }
    @PutMapping("/update/{idMovie}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer idMovie,
                                                       @RequestPart MultipartFile file,
                                                       @RequestPart String movieDtoObj) throws IOException, MovieNotFoundException {
        if (file.isEmpty()) {
            file = null;
        }
        MovieDto dto = convertToMovieDto(movieDtoObj);
        return ResponseEntity.ok(movieService.updateMovie(idMovie,dto,file));
    }
    @DeleteMapping("/delete/{idMovie}")
    public ResponseEntity<String> deleteMovieHandler(@PathVariable Integer idMovie) throws IOException, MovieNotFoundException {
        return ResponseEntity.ok(movieService.deleteMovie(idMovie));
    }
    private MovieDto convertToMovieDto(String movieDtoObj) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(movieDtoObj, MovieDto.class);
    }
    @GetMapping("/allMoviesPage")
    public ResponseEntity<MoviePageResponse> getMoviesWithPagination(
        @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumBer,
        @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ){
        return ResponseEntity.ok(movieService.getAllMoviesWithPagination(pageNumBer, pageSize));
    }
    @GetMapping("/allMoviesPageAndSorting")
    public ResponseEntity<MoviePageResponse> getMoviesWithPaginationAndSorting(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumBer,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIRECTION, required = false) String sortDirection
    ){
        return ResponseEntity.ok(movieService.getAllMoviesWithPaginationAndSorting(pageNumBer, pageSize, sortBy, sortDirection));
    }
}
