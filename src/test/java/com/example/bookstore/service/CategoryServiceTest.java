package com.example.bookstore.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import com.example.bookstore.dto.category.CategoryDto;
import com.example.bookstore.dto.category.CreateCategoryRequestDto;
import com.example.bookstore.exception.EntityNotFoundException;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.model.Category;
import com.example.bookstore.repository.category.CategoryRepository;
import com.example.bookstore.service.category.CategoryServiceImpl;
import java.util.List;
import java.util.Optional;
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
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @InjectMocks
    private CategoryServiceImpl categoryService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("""
            Save custom category to database from valid DTO
            """)
    public void saveCategory_ValidCreateCategoryDto_ReturnsCategoryDto() {
        //Given
        CreateCategoryRequestDto requestDto = getCreateCategoryRequestDto();
        Category category = getCategoryFromDto(requestDto);
        CategoryDto expected = getDtoFromCategory(category);

        Mockito.when(categoryMapper.toEntity(requestDto)).thenReturn(category);
        Mockito.when(categoryRepository.save(category)).thenReturn(category);
        Mockito.when(categoryMapper.toDto(category)).thenReturn(expected);

        //When
        CategoryDto actual = categoryService.save(requestDto);

        //Then
        Assertions.assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Find category by id when category exists
            """)
    public void findByCategoryId_ExistingCategoryId_ReturnsCategory() {
        //Given
        Category category = getCategoryList().get(0);
        CategoryDto expected = getDtoFromCategory(category);

        Mockito.when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(category));
        Mockito.when(categoryMapper.toDto(category))
                .thenReturn(expected);

        //When
        CategoryDto actual = categoryService.findById(anyLong());

        //Then
        assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Find category by id when category does not exists
            """)
    public void findByCategoryId_NonExistingCategoryId_ThrowsException() {
        //Given
        Mockito.when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        //Then
        assertThrows(EntityNotFoundException.class,
                () -> categoryService.findById(anyLong()));
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("""
            Find all categories by valid parameters when categories exists
            """)
    public void getAllCategories_ValidPageable_ReturnsAllCategories() {
        //Given
        Category firstCategory = getCategoryList().get(0);
        Category secondCategory = getCategoryList().get(1);
        CategoryDto firstCategoryDto = getDtoFromCategory(firstCategory);
        CategoryDto secondCategoryDto = getDtoFromCategory(secondCategory);
        List<Category> categoryList = List.of(firstCategory, secondCategory);
        Page<Category> page = new PageImpl<>(categoryList);
        List<CategoryDto> expected = List.of(firstCategoryDto, secondCategoryDto);

        Mockito.when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);
        Mockito.when(categoryMapper.toDtoList(categoryList)).thenReturn(expected);

        //When
        List<CategoryDto> actual = categoryService.findAll(Pageable.ofSize(5));

        //Then
        assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Update category by id when category id exists
            """)
    public void updateCategory_ExistingId_ReturnsCategoryDto() {
        //Given
        CreateCategoryRequestDto requestDto = getCreateCategoryRequestDto();
        Category category = getCategoryList().get(0);
        category.setName("Fiction123");
        CategoryDto expected = getDtoFromCategory(category);

        Mockito.when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        Mockito.doNothing().when(categoryMapper).updateEntityFromDto(requestDto, category);
        Mockito.when(categoryRepository.save(category)).thenReturn(category);
        Mockito.when(categoryMapper.toDto(category)).thenReturn(expected);

        //When
        CategoryDto actual = categoryService.updateById(anyLong(), requestDto);

        //Then
        assertEquals(expected, actual);
        Mockito.verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Test
    @DisplayName("""
            Update category by id when category id does not exists
            """)
    public void updateCategory_NonExistingId_ThrowsException() {
        //Given
        CreateCategoryRequestDto requestDto = getCreateCategoryRequestDto();

        Mockito.when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        //Then
        assertThrows(EntityNotFoundException.class,
                () -> categoryService.updateById(anyLong(), requestDto));
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    @DisplayName("""
            Delete category by id when category id exists
            """)
    public void deleteCategory_ExistingId_ReturnsNothing() {
        //Given
        Category category = getCategoryList().get(0);

        Mockito.when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.of(category));

        //When
        categoryService.deleteById(anyLong());

        //Then
        Mockito.verify(categoryRepository).deleteById(anyLong());
    }

    @Test
    @DisplayName("""
            Delete category by id when category id does not exists
            """)
    public void deleteCategory_NonExistingId_ThrowsException() {
        //Given
        Mockito.when(categoryRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        //Then
        assertThrows(EntityNotFoundException.class,
                () -> categoryService.deleteById(anyLong()));
        Mockito.verifyNoMoreInteractions(categoryRepository);
    }

    private Category getCategoryFromDto(CreateCategoryRequestDto requestDto) {
        return new Category()
                .setName(requestDto.name())
                .setDescription(requestDto.description());
    }

    private CategoryDto getDtoFromCategory(Category category) {
        return new CategoryDto(
                1L,
                category.getName(),
                category.getDescription());
    }

    private CreateCategoryRequestDto getCreateCategoryRequestDto() {
        return new CreateCategoryRequestDto(
                "Fiction",
                "Fiction books");
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
                        .setDescription("Autobiography books"));
    }
}
