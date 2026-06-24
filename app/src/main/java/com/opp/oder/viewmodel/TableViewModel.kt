package com.opp.oder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opp.oder.data.repository.TableRepository
import com.opp.oder.data.db.entity.TableEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TableViewModel(
    private val repository: TableRepository
) : ViewModel() {

    private val _tables = MutableStateFlow<List<TableEntity>>(emptyList())
    val tables: StateFlow<List<TableEntity>> = _tables

    private val _selectedTableId = MutableStateFlow<Long?>(null)
    val selectedTableId: StateFlow<Long?> = _selectedTableId

    private val _zones = MutableStateFlow<List<String>>(emptyList())
    val zones: StateFlow<List<String>> = _zones

    init {
        viewModelScope.launch {
            repository.createDefaultTables()
            loadTables()
        }
    }

    private fun loadTables() {
        viewModelScope.launch {
            val list = repository.getAllDirect()
            _tables.value = list
            _zones.value = list.map { it.zone }.distinct()
        }
    }

    fun selectTable(id: Long) {
        _selectedTableId.value = id
    }

    fun addTable(name: String, zone: String = "大厅") {
        viewModelScope.launch {
            repository.insert(TableEntity(name = name, zone = zone))
            loadTables()
        }
    }

    fun updateTable(table: TableEntity) {
        viewModelScope.launch {
            repository.update(table)
            loadTables()
        }
    }

    fun deleteTable(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            if (_selectedTableId.value == id) {
                _selectedTableId.value = null
            }
            loadTables()
        }
    }

    fun createDefaults() {
        viewModelScope.launch {
            repository.createDefaultTables()
            loadTables()
        }
    }

    fun reorderTables(reordered: List<TableEntity>) {
        _tables.value = reordered
        viewModelScope.launch {
            repository.updateSortOrders(reordered.map { it.id })
        }
    }

    fun reload() {
        loadTables()
    }
}
