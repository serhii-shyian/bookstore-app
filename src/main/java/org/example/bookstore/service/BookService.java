package org.example.bookstore.service;

import java.util.List;
import org.example.bookstore.model.Book;

public interface BookService {
    Book save(Book book);

    List<Book> findAll();
}
