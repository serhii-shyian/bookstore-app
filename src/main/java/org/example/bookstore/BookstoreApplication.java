package org.example.bookstore;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.example.bookstore.model.Book;
import org.example.bookstore.service.BookService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class BookstoreApplication {
    private final BookService bookService;

    public static void main(String[] args) {
        SpringApplication.run(BookstoreApplication.class, args);
    }

    @Bean
    public CommandLineRunner startup() {
        return args -> {
            Book book = new Book(
                    "Camino Ghosts",
                    "John Grisham",
                    "0385545991",
                    BigDecimal.valueOf(22.20));
            bookService.save(book);
            System.out.println(bookService.findAll());
        };
    }

}
