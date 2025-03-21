package com.example.booknest.mapper;

import com.example.booknest.config.MapperConfig;
import com.example.booknest.dto.book.BookDto;
import com.example.booknest.dto.book.BookDtoWithoutCategoryIds;
import com.example.booknest.dto.book.CreateBookRequestDto;
import com.example.booknest.model.Book;
import com.example.booknest.model.Category;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    BookDto toDto(Book book);

    Book toEntity(CreateBookRequestDto bookDto);

    List<BookDto> toDtoList(List<Book> books);

    @Mapping(target = "isbn", ignore = true)
    void updateEntityFromDto(CreateBookRequestDto bookDto, @MappingTarget Book book);

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @AfterMapping
    default void setCategoryIds(@MappingTarget BookDto bookDto, Book book) {
        Set<Long> categoryIds = book.getCategories()
                .stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
        bookDto.setCategoryIds(categoryIds);
    }
}
