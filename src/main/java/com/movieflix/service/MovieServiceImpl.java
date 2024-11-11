package com.movieflix.service;

import com.movieflix.Repositories.MovieRepository;
import com.movieflix.dto.MovieDto;
import com.movieflix.entities.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    private final FileService fileService;

    @Value("${project.poster}")
    private String posterPath;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        //1. Upload file len
        String uploadedFilename = fileService.uploadFile(posterPath, file);
        //2. Dat gia tri truong poster nhu mot filename
        movieDto.setPoster(uploadedFilename);
        //3. map du lieu vao Movie
        Movie movie = new Movie(
                movieDto.getIdMovie(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        //4. luu du lieu -> luu Movie Object
        Movie savedMovie = movieRepository.save(movie);
        //5. tao duong link posterURL
        String posterUrl = baseUrl + "/file/" + uploadedFilename;
        //6. convert Movie -> DTO va return DTO
        return new MovieDto(
                savedMovie.getIdMovie(),
                savedMovie.getTitle(),
                savedMovie.getDirector(),
                savedMovie.getStudio(),
                savedMovie.getMovieCast(),
                savedMovie.getReleaseYear(),
                savedMovie.getPoster(),
                posterUrl
        );
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        //1. Kiem tra xem IdMovie co ton tai trong database hay khong
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Không tìm thấy phim"));
        //2. tao UrlPoster
        String posterUrl = baseUrl + "/file/" + movie.getPoster();
        //3. tao du lieu MovieDto va return
        return new MovieDto(
                movie.getIdMovie(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
    }

    @Override
    public List<MovieDto> getAllMovies() {
        //1. Lay tat ca du lieu trong database
        List<Movie> movies = movieRepository.findAll();

        List<MovieDto> movieDtos = new ArrayList<>();
        //2. Lap qua danh sach, tao posterUrl cho tung Movie va anh xa toi MovieDto
        for (Movie movie : movies){
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getIdMovie(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }
        return movieDtos;
    }
}
