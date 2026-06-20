package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opp.oder.data.repository.MenuRepository
import com.opp.oder.data.db.entity.MenuItemEntity
import com.opp.oder.data.db.entity.RecipeIngredientEntity
import com.opp.oder.data.db.entity.RecipeStepEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MenuViewModel(
    private val repository: MenuRepository
) : ViewModel() {

    val menuItems: StateFlow<List<MenuItemEntity>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMenuItem = MutableStateFlow<MenuItemEntity?>(null)
    val selectedMenuItem: StateFlow<MenuItemEntity?> = _selectedMenuItem

    private val _recipeSteps = MutableStateFlow<List<RecipeStepEntity>>(emptyList())
    val recipeSteps: StateFlow<List<RecipeStepEntity>> = _recipeSteps

    private val _recipeIngredients = MutableStateFlow<List<RecipeIngredientEntity>>(emptyList())
    val recipeIngredients: StateFlow<List<RecipeIngredientEntity>> = _recipeIngredients

    private val _sheetVisible = MutableStateFlow(false)
    val sheetVisible: StateFlow<Boolean> = _sheetVisible

    fun selectItem(item: MenuItemEntity) {
        _selectedMenuItem.value = item
        if (item.hasRecipe) {
            viewModelScope.launch {
                _recipeSteps.value = repository.getRecipeSteps(item.id)
                _recipeIngredients.value = repository.getRecipeIngredients(item.id)
                _sheetVisible.value = true
            }
        }
    }

    fun dismissSheet() {
        _sheetVisible.value = false
    }

    fun addItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.insert(item)
        }
    }

    fun updateItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.update(item)
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            if (_selectedMenuItem.value?.id == id) {
                _selectedMenuItem.value = null
                _sheetVisible.value = false
            }
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
