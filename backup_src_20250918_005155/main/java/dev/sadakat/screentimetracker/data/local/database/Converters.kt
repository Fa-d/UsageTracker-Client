<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/Converters.kt
package dev.sadakat.screentimetracker.core.database
========
package dev.sadakat.screentimetracker.data.local.database
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/database/Converters.kt

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/Converters.kt
import dev.sadakat.screentimetracker.core.database.entities.PetType
========
import dev.sadakat.screentimetracker.data.local.entities.PetType
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/database/Converters.kt

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