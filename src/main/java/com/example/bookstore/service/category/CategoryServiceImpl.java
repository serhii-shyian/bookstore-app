package com.example.bookstore.service.category;

import com.example.bookstore.dto.category.CategoryDto;
import com.example.bookstore.dto.category.CreateCategoryRequestDto;
import com.example.bookstore.exception.EntityNotFoundException;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.model.Category;
import com.example.bookstore.repository.category.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto save(CreateCategoryRequestDto categoryDto) {
        Category categoryFromDto = categoryMapper.toEntity(categoryDto);
        return categoryMapper.toDto(categoryRepository.save(categoryFromDto));
    }

    @Override
    public CategoryDto findById(Long categoryId) {
        Category categoryFromDb = findCategoryById(categoryId);
        return categoryMapper.toDto(categoryFromDb);
    }

    @Override
    public List<CategoryDto> findAll(Pageable pageable) {
        return categoryMapper.toDtoList(
                categoryRepository.findAll(pageable).toList());
    }

    @Override
    public CategoryDto updateById(Long categoryId, CreateCategoryRequestDto categoryDto) {
        Category categoryFromDb = findCategoryById(categoryId);
        categoryMapper.updateEntityFromDto(categoryDto, categoryFromDb);
        return categoryMapper.toDto(categoryRepository.save(categoryFromDb));
    }

    @Override
    public void deleteById(Long categoryId) {
        categoryRepository.findById(categoryId).orElseThrow(
                () -> new EntityNotFoundException("Can't delete category by id: " + categoryId));
        categoryRepository.deleteById(categoryId);
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new EntityNotFoundException("Can't find category by id: " + categoryId));
    }
}
