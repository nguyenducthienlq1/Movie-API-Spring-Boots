package com.movieflix.service;

import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import com.movieflix.exceptions.MovieNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MovieService {

    MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException;

    MovieDto getMovie(Integer movieId) throws MovieNotFoundException;

    List<MovieDto> getAllMovies();

    MovieDto updateMovie(Integer idMovie, MovieDto movieDto, MultipartFile file) throws IOException, MovieNotFoundException;

    String deleteMovie(Integer movieId) throws IOException, MovieNotFoundException;

    MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize);

    MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize
                                                ,String sortField, String sortDirection);
}
