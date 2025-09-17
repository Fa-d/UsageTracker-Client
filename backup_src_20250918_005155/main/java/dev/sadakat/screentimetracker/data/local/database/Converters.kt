package dev.sadakat.screentimetracker.data.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.sadakat.screentimetracker.data.local.entities.PetType

class Converters {
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromPetType(petType: PetType): String {
        return petType.name
    }
    
    @TypeConverter
    fun toPetType(petType: String): PetType {
        return PetType.valueOf(petType)
    }
}