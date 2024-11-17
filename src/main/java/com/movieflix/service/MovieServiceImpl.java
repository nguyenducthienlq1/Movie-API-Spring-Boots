package com.movieflix.service;

import com.movieflix.Repositories.MovieRepository;
import com.movieflix.dto.MovieDto;
import com.movieflix.dto.MoviePageResponse;
import com.movieflix.entities.Movie;
import com.movieflix.exceptions.FileExistsException;
import com.movieflix.exceptions.MovieNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        if (Files.exists(Paths.get(posterPath + File.separator + file.getOriginalFilename()))) {
            throw new FileExistsException("File đã tồn tại, hãy chọn file khác !!!");
        }
        String uploadedFilename = fileService.uploadFile(posterPath, file);
        //2. Dat gia tri truong poster nhu mot filename
        movieDto.setPoster(uploadedFilename);
        //3. map du lieu vao Movie
        Movie movie = new Movie(
                null,
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
    public MovieDto getMovie(Integer movieId) throws MovieNotFoundException {
        //1. Kiem tra xem IdMovie co ton tai trong database hay khong
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Không tìm thấy phim có id = " + movieId));
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

    @Override
    public MovieDto updateMovie(Integer idMovie, MovieDto movieDto, MultipartFile file) throws IOException, MovieNotFoundException {
        //1. Kiểm tra xem idMovie có trong database hay không
        Movie mv = movieRepository.findById(idMovie)
                .orElseThrow(() -> new MovieNotFoundException("Không tìm thấy phim có id = " + idMovie));
        //2. Nếu không cập nhật file, không làm gì cả
        //   Nếu có cập nhật file, xóa file hiện có và upload file mới
        String fileName = mv.getPoster();
        if (file != null){
            Files.deleteIfExists(Paths.get(posterPath + File.separator + fileName));
            fileName = fileService.uploadFile(posterPath, file);
        }
        //3. set giá trị poster cho movieDto
        movieDto.setPoster(fileName);
        //4. Ánh xạ qua Movie
        Movie movie = new Movie(
                mv.getIdMovie(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        //5. lưu Movie vào database, trả về Movie đã lưu
        Movie uploadedMovie = movieRepository.save(movie);
        //6. Tạo ra posterUrl
        String posterUrl = baseUrl + "/file/" + fileName;
        //7. Ánh xạ lại qua MovieDto và trả về
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
    public String deleteMovie(Integer movieId) throws IOException, MovieNotFoundException {
        //1. Kiểm tra xem phim có tồn tại trong database hay không
        Movie mv = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("Không tìm thấy phim có id = " + movieId));
        Integer id = mv.getIdMovie();
        //2. Xóa file của phim cần xóa
        Files.deleteIfExists(Paths.get(posterPath + File.separator + mv.getPoster()));
        //3. Xóa phim
        movieRepository.delete(mv);


        return "Đã xóa phim có id = " + id;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);

        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

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
        return new MoviePageResponse(movieDtos,pageNumber,pageSize,
                                    moviePages.getTotalElements(),
                                    moviePages.getTotalPages(),
                                    moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize,
                                                        String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending()
                                                                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(pageNumber,pageSize, sort);

        Page<Movie> moviePage = movieRepository.findAll(pageable);
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

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
        return new MoviePageResponse(movieDtos,pageNumber,pageSize,
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isLast());
    }
}
