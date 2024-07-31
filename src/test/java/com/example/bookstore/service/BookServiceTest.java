package com.example.bookstore.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;

import com.example.bookstore.dto.book.BookDto;
import com.example.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.example.bookstore.dto.book.BookSearchParametersDto;
import com.example.bookstore.dto.book.CreateBookRequestDto;
import com.example.bookstore.exception.EntityNotFoundException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.repository.book.BookRepository;
import com.example.bookstore.repository.book.BookSpecificationBuilder;
import com.example.bookstore.service.book.BookServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @InjectMocks
    private BookServiceImpl bookService;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;

    @Test
    @DisplayName("""
            Save custom book to database from valid DTO
            """)
    public void saveBook_ValidCreateBookRequestDto_ReturnsBookDto() {
        //Given
        CreateBookRequestDto requestDto = getCreateBookRequestDto();
        Book book = getBookFromDto(requestDto);
        BookDto expected = getDtoFromBook(book);

        Mockito.when(bookMapper.toEntity(requestDto)).thenReturn(book);
        Mockito.when(bookRepository.save(book)).thenReturn(book);
        Mockito.when(bookMapper.toDto(book)).thenReturn(expected);

        //When
        BookDto actual = bookService.save(requestDto);

        //Then
        Assertions.assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Find book by id when book exists
            """)
    public void findBook_ExistingBookId_ReturnsBook() {
        //Given
        Book book = getBookList().get(0);
        BookDto expected = getDtoFromBook(book);

        Mockito.when(bookRepository.findById(anyLong()))
                .thenReturn(Optional.of(book));
        Mockito.when(bookMapper.toDto(book))
                .thenReturn(expected);

        //When
        BookDto actual = bookService.findById(anyLong());

        //Then
        assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Find book by id when book does not exists
            """)
    public void findBook_NonExistingBookId_ThrowsException() {
        //Given
        Mockito.when(bookRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        //Then
        assertThrows(EntityNotFoundException.class,
                () -> bookService.findById(anyLong()));
        Mockito.verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Find all books by valid parameters when books exists
            """)
    public void getAllBooks_ValidPageable_ReturnsAllBooks() {
        //Given
        Book firstBook = getBookList().get(0);
        Book secondBook = getBookList().get(1);
        BookDto firstBookDto = getDtoFromBook(firstBook);
        BookDto secondBookDto = getDtoFromBook(secondBook);
        List<Book> bookList = List.of(firstBook, secondBook);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Book> page = new PageImpl<>(bookList);
        List<BookDto> expected = List.of(firstBookDto, secondBookDto);

        Mockito.when(bookRepository.findAll(pageable)).thenReturn(page);
        Mockito.when(bookMapper.toDtoList(bookList)).thenReturn(expected);

        //When
        List<BookDto> actual = bookService.findAll(Pageable.ofSize(5));

        //Then
        assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Find all books by category id when category id exists
            """)
    public void findAllBooksByCategoryId_ExistingId_ReturnsBookDtoList() {
        //Given
        Pageable pageable = PageRequest.of(0, 5);
        Book firstBook = getBookList().get(0);
        Book secondBook = getBookList().get(1);
        BookDtoWithoutCategoryIds firstBookDto = getBookDtoWithoutCategoryIdsList().get(0);
        BookDtoWithoutCategoryIds secondBookDto = getBookDtoWithoutCategoryIdsList().get(1);
        List<Book> bookList = List.of(firstBook, secondBook);
        Mockito.when(bookRepository.findAllByCategoryId(1L, pageable)).thenReturn(bookList);
        Mockito.when(bookMapper.toDtoWithoutCategories(firstBook)).thenReturn(firstBookDto);
        Mockito.when(bookMapper.toDtoWithoutCategories(secondBook)).thenReturn(secondBookDto);
        List<BookDtoWithoutCategoryIds> expected = List.of(firstBookDto, secondBookDto);

        //When
        List<BookDtoWithoutCategoryIds> actual =
                bookService.findAllByCategoryId(1L, Pageable.ofSize(5));

        //Then
        assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Update book by id when book id exists
            """)
    public void updateBook_ExistingId_ReturnsBookDto() {
        //Given
        Book book = getBookList().get(0);
        book.setTitle("Sample Book 123");
        book.setAuthor("Author ABC");
        book.setPrice(BigDecimal.valueOf(99.99));
        CreateBookRequestDto requestDto = getCreateBookRequestDto();
        BookDto expected = getDtoFromBook(book);

        Mockito.when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        Mockito.doNothing().when(bookMapper).updateEntityFromDto(requestDto, book);
        Mockito.when(bookRepository.save(book)).thenReturn(book);
        Mockito.when(bookMapper.toDto(book)).thenReturn(expected);

        //When
        BookDto actual = bookService.updateById(anyLong(), requestDto);

        //Then
        assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Update book by id when book id does not exists
            """)
    public void updateBook_NonExistingId_ThrowsException() {
        //Given
        CreateBookRequestDto requestDto = getCreateBookRequestDto();

        Mockito.when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        //Then
        assertThrows(EntityNotFoundException.class,
                () -> bookService.updateById(anyLong(), requestDto));
        Mockito.verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Delete book by id when book id exists
            """)
    public void deleteBook_ExistingId_ReturnsNothing() {
        //Given
        Book book = getBookList().get(0);

        Mockito.when(bookRepository.findById(anyLong()))
                .thenReturn(Optional.of(book));

        //When
        bookService.deleteById(anyLong());

        //Then
        Mockito.verify(bookRepository).deleteById(anyLong());
    }

    @Test
    @DisplayName("""
            Delete book by id when book id does not exists
            """)
    public void deleteBook_NonExistingId_ThrowsException() {
        //Given
        Mockito.when(bookRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        //Then
        assertThrows(EntityNotFoundException.class,
                () -> bookService.deleteById(anyLong()));
        Mockito.verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Search books with valid params when books exists
            """)
    public void searchBooks_ValidBookParams_ReturnsBookDtoList() {
        //Given
        Book firstBook = getBookList().get(0);
        Book secondBook = getBookList().get(1);
        List<Book> booksList = List.of(firstBook, secondBook);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Book> page = new PageImpl<>(booksList);
        BookSearchParametersDto paramsDto = getBookSearchParametersDto();
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(paramsDto);
        BookDto firstBookDto = getDtoFromBook(firstBook);
        BookDto secondBookDto = getDtoFromBook(secondBook);
        List<BookDto> expected = List.of(firstBookDto, secondBookDto);

        Mockito.when(bookSpecificationBuilder.build(paramsDto))
                .thenReturn(bookSpecification);
        Mockito.when(bookRepository.findAll(bookSpecification, pageable))
                .thenReturn(page);
        Mockito.when(bookMapper.toDtoList(booksList)).thenReturn(expected);

        //When
        List<BookDto> actual = bookService.searchByParameters(paramsDto, pageable);

        //Then
        assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    private BookDto getDtoFromBook(Book book) {
        return new BookDto()
                .setId(book.getId())
                .setTitle(book.getTitle())
                .setAuthor(book.getAuthor())
                .setIsbn(book.getIsbn())
                .setPrice(book.getPrice())
                .setDescription(book.getDescription())
                .setCoverImage(book.getCoverImage());
    }

    private Book getBookFromDto(CreateBookRequestDto requestDto) {
        return new Book()
                .setTitle(requestDto.title())
                .setAuthor(requestDto.author())
                .setIsbn(requestDto.isbn())
                .setPrice(requestDto.price())
                .setDescription(requestDto.description())
                .setCoverImage(requestDto.coverImage());
    }

    private CreateBookRequestDto getCreateBookRequestDto() {
        return new CreateBookRequestDto(
                "Sample Book 1",
                "Author A",
                "978-1-23-456789-7",
                BigDecimal.valueOf(19.99),
                "This is a sample book description.",
                "http://example.com/cover1.jpg",
                Set.of());
    }

    private List<BookDtoWithoutCategoryIds> getBookDtoWithoutCategoryIdsList() {
        return List.of(
                new BookDtoWithoutCategoryIds(
                        "Sample Book 1",
                        "Author A",
                        "978-1-23-456789-7",
                        BigDecimal.valueOf(19.99),
                        "This is a sample book description.",
                        "http://example.com/cover1.jpg"),
                new BookDtoWithoutCategoryIds(
                        "Sample Book 2",
                        "Author B",
                        "978-3-16-148410-0",
                        BigDecimal.valueOf(24.99),
                        "Another sample book description.",
                        "http://example.com/cover2.jpg")
        );
    }

    private BookSearchParametersDto getBookSearchParametersDto() {
        return new BookSearchParametersDto(
                "Sample Book 1",
                "Author B",
                "123"
        );
    }

    private List<Book> getBookList() {
        return List.of(
                new Book()
                        .setId(1L)
                        .setTitle("Sample Book 1")
                        .setAuthor("Author A")
                        .setIsbn("978-1-23-456789-7")
                        .setPrice(BigDecimal.valueOf(19.99))
                        .setDescription("This is a sample book description.")
                        .setCoverImage("http://example.com/cover1.jpg")
                        .setCategories(Set.of(getCategory())),
                new Book()
                        .setId(2L)
                        .setTitle("Sample Book 2")
                        .setAuthor("Author B")
                        .setIsbn("978-3-16-148410-0")
                        .setPrice(BigDecimal.valueOf(24.99))
                        .setDescription("Another sample book description.")
                        .setCoverImage("http://example.com/cover2.jpg")
                        .setCategories(Set.of(getCategory()))
        );
    }

    private Category getCategory() {
        return new Category()
                .setId(1L)
                .setName("Fiction")
                .setDescription("Fiction books");
    }
}
