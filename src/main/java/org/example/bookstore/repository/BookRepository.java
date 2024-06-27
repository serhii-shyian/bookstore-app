package org.example.bookstore.repository;

import java.util.List;
import org.example.bookstore.model.Book;

public interface BookRepository {
    Book save(Book book);

    List<Book> findAll();
}
