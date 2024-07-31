package com.example.bookstore.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookstore.dto.book.BookDto;
import com.example.bookstore.dto.book.CreateBookRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookControllerTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(
                            "database/categories/insert-into-categories.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(
                            "database/books/insert-into-books.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(
                            "database/categories/insert-into-books_categories.sql"));
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(
                            "database/categories/delete-all-from-books_categories.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(
                            "database/books/delete-all-from-books.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource(
                            "database/categories/delete-all-from-categories.sql"));
        }
    }

    @Test
    @Order(1)
    @DisplayName("""
            Get list of all books when the books exists
            """)
    @WithMockUser(username = "user")
    void getAllBooks_BooksExists_ReturnsBookDtoList() throws Exception {
        //Given
        List<BookDto> expected = getBookDtoList();

        //When
        MvcResult result = mockMvc.perform(
                        get("/books")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        BookDto[] actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @Order(2)
    @DisplayName("""
            Get the book by id when book exists
            """)
    @WithMockUser(username = "user")
    void findBook_ExistingBookId_ReturnsBookDto() throws Exception {
        //Given
        BookDto expected = getBookDtoList().get(0);

        //When
        MvcResult result = mockMvc.perform(
                        get("/books/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        BookDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), BookDto.class);
        Assertions.assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Test
    @Order(3)
    @DisplayName("""
            Search books with valid parameters when books exists
            """)
    @WithMockUser(roles = "USER")
    public void searchBooks_ValidParams_ReturnsBookDtoList() throws Exception {
        //Given
        List<BookDto> expected = List.of(getBookDtoList().get(1));
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", "Sample Book 2");

        //When
        MvcResult result = mockMvc.perform(
                        get("/books/search")
                                .params(params)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        List<BookDto> actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsString(), new TypeReference<>() {});
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Order(4)
    @DisplayName("""
            Create a new book from valid DTO
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createBook_ValidRequestDto_ReturnsBookDto() throws Exception {
        //Given
        CreateBookRequestDto requestDto = getCreateBookRequestDtoList().get(0);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        BookDto expected = getBookDtoFromRequestDto(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        post("/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        BookDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), BookDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @Order(5)
    @DisplayName("""
            Update the book by id when book exists
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateBook_ExistingBookId_ReturnsBookDto() throws Exception {
        //Given
        CreateBookRequestDto requestDto = getCreateBookRequestDtoList().get(1);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        BookDto expected = getBookDtoFromRequestDto(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        put("/books/1")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        //Then
        BookDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), BookDto.class);
        Assertions.assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Test
    @Order(6)
    @DisplayName("""
            Delete the book by id when book exists
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteBook_ExistingBookId_ReturnsNothing() throws Exception {
        //When
        MvcResult result = mockMvc.perform(
                        delete("/books/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        //Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertTrue(actual.isEmpty());
    }

    private List<CreateBookRequestDto> getCreateBookRequestDtoList() {
        return List.of(
                new CreateBookRequestDto(
                        "Sample Book 4",
                        "Author D",
                        "978-0-596-52068-7",
                        BigDecimal.valueOf(37.99),
                        "Some another sample book description.",
                        "http://example.com/cover4.jpg",
                        Set.of(3L)),
                new CreateBookRequestDto(
                        "Sample Book 123",
                        "Author ABC",
                        "978-1-23-456789-7",
                        BigDecimal.valueOf(99.99),
                        "This is a sample book description.",
                        "http://example.com/cover1.jpg",
                        Set.of(3L))
        );
    }

    private BookDto getBookDtoFromRequestDto(CreateBookRequestDto requestDto) {
        return new BookDto()
                .setTitle(requestDto.title())
                .setAuthor(requestDto.author())
                .setIsbn(requestDto.isbn())
                .setPrice(requestDto.price())
                .setDescription(requestDto.description())
                .setCoverImage(requestDto.coverImage())
                .setCategoryIds(requestDto.categoryIds());
    }

    private List<BookDto> getBookDtoList() {
        return List.of(
                new BookDto()
                        .setId(1L)
                        .setTitle("Sample Book 1")
                        .setAuthor("Author A")
                        .setIsbn("978-1-23-456789-7")
                        .setPrice(BigDecimal.valueOf(19.99))
                        .setDescription("This is a sample book description.")
                        .setCoverImage("http://example.com/cover1.jpg")
                        .setCategoryIds(Set.of(1L)),
                new BookDto()
                        .setId(2L)
                        .setTitle("Sample Book 2")
                        .setAuthor("Author B")
                        .setIsbn("978-3-16-148410-0")
                        .setPrice(BigDecimal.valueOf(24.99))
                        .setDescription("Another sample book description.")
                        .setCoverImage("http://example.com/cover2.jpg")
                        .setCategoryIds(Set.of(1L)),
                new BookDto()
                        .setId(3L)
                        .setTitle("Sample Book 3")
                        .setAuthor("Author C")
                        .setIsbn("979-0-2600-0043-8")
                        .setPrice(BigDecimal.valueOf(29.99))
                        .setDescription("Yet another sample book description.")
                        .setCoverImage("http://example.com/cover3.jpg")
                        .setCategoryIds(Set.of(3L)));
    }
}
