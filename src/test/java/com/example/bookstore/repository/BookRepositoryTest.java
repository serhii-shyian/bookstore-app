package com.example.bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.Category;
import com.example.bookstore.repository.book.BookRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/delete-all-data-before-tests.sql"));
        }
    }

    @Test
    @DisplayName("""
            Find all books in the specified category when the category exists
            """)
    @Sql(
            scripts = {"classpath:database/categories/insert-into-categories.sql",
                    "classpath:database/books/insert-into-books.sql",
                    "classpath:database/categories/insert-into-books_categories.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = {"classpath:database/categories/delete-all-from-books_categories.sql",
                    "classpath:database/books/delete-all-from-books.sql",
                    "classpath:database/categories/delete-all-from-categories.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    void findAllBooksByCategoryId_ExistedCategoryId_ReturnsBooksList() {
        //Given
        List<Book> expected = List.of(getBookList().get(0), getBookList().get(1));

        //When
        List<Book> actual = bookRepository.findAllByCategoryId(1L, Pageable.ofSize(5));

        //Then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
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
                        .setCategories(Set.of(
                                getCategoryList().get(0))),
                new Book()
                        .setId(2L)
                        .setTitle("Sample Book 2")
                        .setAuthor("Author B")
                        .setIsbn("978-3-16-148410-0")
                        .setPrice(BigDecimal.valueOf(24.99))
                        .setDescription("Another sample book description.")
                        .setCoverImage("http://example.com/cover2.jpg")
                        .setCategories(Set.of(
                                getCategoryList().get(0)))
        );
    }

    private List<Category> getCategoryList() {
        return List.of(
                new Category()
                        .setId(1L)
                        .setName("Fiction")
                        .setDescription("Fiction books"),
                new Category()
                        .setId(2L)
                        .setName("Autobiography")
                        .setDescription("Autobiography books"),
                new Category()
                        .setId(3L)
                        .setName("Romance")
                        .setDescription("Romance books")
        );
    }
}
