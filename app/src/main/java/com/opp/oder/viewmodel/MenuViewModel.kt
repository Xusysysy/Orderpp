package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opp.oder.data.repository.MenuRepository
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.data.db.entity.RecipeIngredientEntity
import com.opp.oder.data.db.entity.RecipeStepEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MenuViewModel(
    private val repository: MenuRepository
) : ViewModel() {

    private val _menuItems = MutableStateFlow<List<MenuItemEntity>>(emptyList())
    val menuItems: StateFlow<List<MenuItemEntity>> = _menuItems

    private val _selectedMenuItem = MutableStateFlow<MenuItemEntity?>(null)
    val selectedMenuItem: StateFlow<MenuItemEntity?> = _selectedMenuItem

    private val _recipeSteps = MutableStateFlow<List<RecipeStepEntity>>(emptyList())
    val recipeSteps: StateFlow<List<RecipeStepEntity>> = _recipeSteps

    private val _recipeIngredients = MutableStateFlow<List<RecipeIngredientEntity>>(emptyList())
    val recipeIngredients: StateFlow<List<RecipeIngredientEntity>> = _recipeIngredients

    private val _sheetVisible = MutableStateFlow(false)
    val sheetVisible: StateFlow<Boolean> = _sheetVisible

    init {
        loadMenuItems()
    }

    fun loadMenuItems() {
        viewModelScope.launch {
            repository.getAll().collect { _menuItems.value = it }
        }
    }

    fun reorderItems(reordered: List<MenuItemEntity>) {
        _menuItems.value = reordered
        viewModelScope.launch {
            repository.updateSortOrders(reordered.map { it.id })
        }
    }

    fun selectItem(item: MenuItemEntity) {
        _selectedMenuItem.value = item
        if (item.hasRecipe) {
            _sheetVisible.value = true
            viewModelScope.launch {
                _recipeSteps.value = repository.getRecipeSteps(item.id)
                _recipeIngredients.value = repository.getRecipeIngredients(item.id)
            }
        }
    }

    fun dismissSheet() {
        _sheetVisible.value = false
    }

    fun addItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.insert(item)
            repository.getAll().collect { _menuItems.value = it }
        }
    }

    fun updateItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.update(item)
            repository.getAll().collect { _menuItems.value = it }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            if (_selectedMenuItem.value?.id == id) {
                _selectedMenuItem.value = null
                _sheetVisible.value = false
            }
            repository.getAll().collect { _menuItems.value = it }
        }
    }

    fun saveRecipe(
        menuItemId: Long,
        steps: List<RecipeStepEntity>,
        ingredients: List<RecipeIngredientEntity>
    ) {
        viewModelScope.launch {
            repository.saveRecipe(menuItemId, steps, ingredients)
        }
    }
}
