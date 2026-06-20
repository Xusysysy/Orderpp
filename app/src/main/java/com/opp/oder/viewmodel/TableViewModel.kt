package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opp.oder.data.repository.TableRepository
import com.opp.oder.data.db.entity.TableEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TableViewModel(
    private val repository: TableRepository
) : ViewModel() {

    val tables: StateFlow<List<TableEntity>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTableId = MutableStateFlow<Long?>(null)
    val selectedTableId: StateFlow<Long?> = _selectedTableId

    private val _zones = MutableStateFlow<List<String>>(emptyList())
    val zones: StateFlow<List<String>> = _zones

    init {
        viewModelScope.launch {
            tables.collect { list ->
                _zones.value = list.map { it.zone }.distinct()
            }
        }
    }

    fun selectTable(id: Long) {
        _selectedTableId.value = id
    }

    fun addTable(name: String, zone: String = "大厅") {
        viewModelScope.launch {
            repository.insert(TableEntity(name = name, zone = zone))
        }
    }

    fun updateTable(table: TableEntity) {
        viewModelScope.launch {
            repository.update(table)
        }
    }

    fun deleteTable(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            if (_selectedTableId.value == id) {
                _selectedTableId.value = null
            }
        }
    }

    fun createDefaults() {
        viewModelScope.launch {
            repository.createDefaultTables()
        }
    }
}
