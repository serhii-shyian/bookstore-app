package com.example.bookstore.controller;

import com.example.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.example.bookstore.dto.category.CategoryDto;
import com.example.bookstore.dto.category.CreateCategoryRequestDto;
import com.example.bookstore.service.book.BookService;
import com.example.bookstore.service.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@Tag(name = "Category management", description = "Endpoint for managing categories")
@Validated
public class CategoryController {
    private final CategoryService categoryService;
    private final BookService bookService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all categories",
            description = "Getting a list of all available categories")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<CategoryDto> getAll(@ParameterObject
                                    @PageableDefault(
                                            size = 5,
                                            sort = "id",
                                            direction = Sort.Direction.ASC)
                                    Pageable pageable) {
        return categoryService.findAll(pageable);
    }

    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a category by id",
            description = "Getting a category by id if available")
    @PreAuthorize("hasRole('ROLE_USER')")
    public CategoryDto getCategoryById(@PathVariable @Positive Long categoryId) {
        return categoryService.findById(categoryId);
    }

    @GetMapping("/{categoryId}/books")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all books by category id",
            description = "Getting all books by category id if available")
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<BookDtoWithoutCategoryIds> getBooksByCategoryId(
            @PathVariable @Positive Long categoryId,
            @ParameterObject @PageableDefault(
                    size = 5,
                    sort = "title",
                    direction = Sort.Direction.ASC)
            Pageable pageable) {
        return bookService.findAllByCategoryId(categoryId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new category",
            description = "Creating  a new category according to the parameters")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryDto createCategory(@RequestBody @Valid CreateCategoryRequestDto categoryDto) {
        return categoryService.save(categoryDto);
    }

    @PutMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update a category by id",
            description = "Updating a category by id according to the parameters")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryDto updateCategoryById(
            @PathVariable @Positive Long categoryId,
            @RequestBody @Valid CreateCategoryRequestDto categoryDto) {
        return categoryService.updateById(categoryId, categoryDto);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a category by id",
            description = "Deleting a category by id if available")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategoryById(@PathVariable @Positive Long categoryId) {
        categoryService.deleteById(categoryId);
    }
}
