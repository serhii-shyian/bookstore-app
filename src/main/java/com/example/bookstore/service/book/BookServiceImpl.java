package com.example.bookstore.service.book;

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
import com.example.bookstore.repository.category.CategoryRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;
    private final CategoryRepository categoryRepository;

    @Override
    public BookDto save(CreateBookRequestDto bookDto) {
        Book bookFromDto = bookMapper.toEntity(bookDto);
        bookFromDto.setCategories(getCategoriesFromDto(bookDto));
        return bookMapper.toDto(bookRepository.save(bookFromDto));
    }

    @Override
    public BookDto findById(Long bookId) {
        Book bookFromDb = findBookById(bookId);
        return bookMapper.toDto(bookFromDb);
    }

    @Override
    public List<BookDto> findAll(Pageable pageable) {
        return bookMapper.toDtoList(
                bookRepository.findAll(pageable).toList());
    }

    @Override
    public List<BookDtoWithoutCategoryIds> findAllByCategoryId(
            Long categoryId, Pageable pageable) {
        return bookRepository.findAllByCategoryId(categoryId, pageable).stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
    }

    @Override
    public BookDto updateById(Long bookId, CreateBookRequestDto bookDto) {
        Book bookFromDb = findBookById(bookId);
        bookFromDb.setCategories(getCategoriesFromDto(bookDto));
        bookMapper.updateEntityFromDto(bookDto, bookFromDb);
        return bookMapper.toDto(bookRepository.save(bookFromDb));
    }

    @Override
    public void deleteById(Long bookId) {
        bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("Can't delete book by id: " + bookId));
        bookRepository.deleteById(bookId);
    }

    @Override
    public List<BookDto> searchByParameters(BookSearchParametersDto paramsDto, Pageable pageable) {
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(paramsDto);
        return bookMapper.toDtoList(bookRepository.findAll(bookSpecification, pageable).toList());
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id: " + bookId));
    }

    private Set<Category> getCategoriesFromDto(CreateBookRequestDto bookDto) {
        return bookDto.categoryIds().stream()
                .map(categoryId -> categoryRepository.findById(categoryId).orElseThrow(
                        () -> new EntityNotFoundException(
                                "Can't find category by id: " + categoryId)))
                .collect(Collectors.toSet());
    }
}
