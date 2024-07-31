package com.example.bookstore.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.example.bookstore.dto.category.CategoryDto;
import com.example.bookstore.dto.category.CreateCategoryRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryControllerTest {
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
                            "database/categories/delete-all-from-categories.sql"));
        }
    }

    @Test
    @Order(1)
    @DisplayName("""
            Get list of all categories when the categories exists
            """)
    @WithMockUser(username = "user")
    void getAllCategories_CategoriesExists_ReturnsCategoryDtoList() throws Exception {
        //Given
        List<CategoryDto> expected = getCategoryDtoList();

        //When
        MvcResult result = mockMvc.perform(
                        get("/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CategoryDto[] actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), CategoryDto[].class);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @Order(2)
    @DisplayName("""
            Get the category by id when category exists
            """)
    @WithMockUser(username = "user")
    void getCategory_ExistingCategoryId_ReturnsCategoryDto() throws Exception {
        //Given
        CategoryDto expected = getCategoryDtoList().get(0);

        //When
        MvcResult result = mockMvc.perform(
                        get("/categories/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CategoryDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), CategoryDto.class);
        Assertions.assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Test
    @Order(3)
    @DisplayName("""
            Get all books by category id when books exists
            """)
    @WithMockUser(username = "user")
    @SqlGroup({
            @Sql(
                    scripts = "classpath:database/books/insert-into-books.sql",
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
            ),
            @Sql(
                    scripts = "classpath:database/categories/insert-into-books_categories.sql",
                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
            ),
            @Sql(
                    scripts = "classpath:database/categories/delete-all-from-books_categories.sql",
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
            ),
            @Sql(
                    scripts = "classpath:database/books/delete-all-from-books.sql",
                    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
            )})
    void getAllBooksByCategory_ExistingBooksWithCategoryId_ReturnsBookDtoList() throws Exception {
        //Given
        List<BookDtoWithoutCategoryIds> expected = getBookDtoWithoutCategoryIdsList();

        //When
        MvcResult result = mockMvc.perform(
                        get("/categories/1/books")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        List<BookDtoWithoutCategoryIds> actual = Arrays.stream(
                objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                        BookDtoWithoutCategoryIds[].class)).toList();
        Assertions.assertEquals(2, actual.size());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Order(4)
    @DisplayName("""
            Create a new category from valid DTO
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createCategory_ValidRequestDto_ReturnsCategoryDto() throws Exception {
        //Given
        CreateCategoryRequestDto requestDto = getCreateCategoryRequestDtoList().get(0);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        CategoryDto expected = getCategoryDtoFromRequestDto(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        post("/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        CategoryDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), CategoryDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.id());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @Order(5)
    @DisplayName("""
            Update the category by id when category exists
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateCategory_ExistingCategoryId_ReturnsCategoryDto() throws Exception {
        //Given
        CreateCategoryRequestDto requestDto = getCreateCategoryRequestDtoList().get(1);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        CategoryDto expected = getCategoryDtoFromRequestDto(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        put("/categories/1")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        //Then
        CategoryDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), CategoryDto.class);
        Assertions.assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Test
    @Order(6)
    @DisplayName("""
            Delete the category by id when category exists
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteCategory_ExistingCategoryId_ReturnsNothing() throws Exception {
        //When
        MvcResult result = mockMvc.perform(
                        delete("/categories/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        //Then
        String actual = result.getResponse().getContentAsString();
        Assertions.assertTrue(actual.isEmpty());
    }

    private List<CreateCategoryRequestDto> getCreateCategoryRequestDtoList() {
        return List.of(
                new CreateCategoryRequestDto(
                        "Mystery",
                        "Mystery books"),
                new CreateCategoryRequestDto(
                        "Fiction123",
                        "Fiction123 books"));
    }

    private CategoryDto getCategoryDtoFromRequestDto(CreateCategoryRequestDto requestDto) {
        return new CategoryDto(
                1L,
                requestDto.name(),
                requestDto.description());
    }

    private List<CategoryDto> getCategoryDtoList() {
        return List.of(
                new CategoryDto(
                        1L,
                        "Fiction",
                        "Fiction books"),
                new CategoryDto(
                        2L,
                        "Autobiography",
                        "Autobiography books"),
                new CategoryDto(
                        3L,
                        "Romance",
                        "Romance books"));
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
                        "http://example.com/cover2.jpg"));
    }
}
