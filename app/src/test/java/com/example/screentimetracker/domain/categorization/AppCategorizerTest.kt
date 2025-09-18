package dev.sadakat.screentimetracker.domain.categorization

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dev.sadakat.screentimetracker.data.local.entities.AppCategory
import dev.sadakat.screentimetracker.data.local.dao.AppCategoryDao
import dev.sadakat.screentimetracker.data.local.entities.AppCategories
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AppCategorizerTest {
    
    private lateinit var appCategorizer: AppCategorizer
    private val mockContext = mockk<Context>()
    private val mockPackageManager = mockk<PackageManager>()
    private val mockAppCategoryDao = mockk<AppCategoryDao>()
    private val mockAppLogger = mockk<AppLogger>(relaxed = true)
    
    @Before
    fun setup() {
        every { mockContext.packageManager } returns mockPackageManager
        appCategorizer = AppCategorizer(mockContext, mockAppCategoryDao, mockAppLogger)
    }
    
    @Test
    fun `categorizeApp returns cached category when available and not stale`() = runTest {
        // Given
        val packageName = "com.test.app"
        val cachedCategory = AppCategory(
            packageName = packageName,
            category = AppCategories.SOCIAL,
            confidence = 0.8f,
            source = "system",
            lastUpdated = System.currentTimeMillis() - 1000L, // 1 second ago
            appName = "Test App"
        )
        coEvery { mockAppCategoryDao.getCategoryByPackage(packageName) } returns cachedCategory
        
        // When
        val result = appCategorizer.categorizeApp(packageName)
        
        // Then
        assertEquals(AppCategories.SOCIAL, result)
        coVerify(exactly = 1) { mockAppCategoryDao.getCategoryByPackage(packageName) }
    }
    
    @Test
    fun `categorizeApp returns known app category`() = runTest {
        // Given
        val packageName = "com.instagram.android"
        coEvery { mockAppCategoryDao.getCategoryByPackage(packageName) } returns null
        coEvery { mockAppCategoryDao.insertCategory(any()) } returns Unit
        every { mockPackageManager.getApplicationLabel(any()) } returns "Instagram"
        every { mockPackageManager.getApplicationInfo(packageName, 0) } returns mockk<ApplicationInfo>()
        
        // When
        val result = appCategorizer.categorizeApp(packageName)
        
        // Then
        assertEquals(AppCategories.SOCIAL, result)
        coVerify { mockAppCategoryDao.insertCategory(any()) }
    }
    
    @Test
    fun `categorizeApp returns system category when available`() = runTest {
        // Given
        val packageName = "com.unknown.game"
        val appInfo = mockk<ApplicationInfo> {
            every { category } returns ApplicationInfo.CATEGORY_GAME
        }
        coEvery { mockAppCategoryDao.getCategoryByPackage(packageName) } returns null
        every { mockPackageManager.getApplicationInfo(packageName, 0) } returns appInfo
        every { mockPackageManager.getApplicationLabel(any()) } returns "Unknown Game"
        coEvery { mockAppCategoryDao.insertCategory(any()) } returns Unit
        
        // When
        val result = appCategorizer.categorizeApp(packageName)
        
        // Then
        assertEquals(AppCategories.GAMES, result)
    }
    
    @Test
    fun `categorizeApp returns pattern-based category`() = runTest {
        // Given
        val packageName = "com.mybank.mobile"
        coEvery { mockAppCategoryDao.getCategoryByPackage(packageName) } returns null
        every { mockPackageManager.getApplicationInfo(packageName, 0) } throws PackageManager.NameNotFoundException()
        every { mockPackageManager.getApplicationLabel(any()) } returns "My Bank"
        coEvery { mockAppCategoryDao.insertCategory(any()) } returns Unit
        
        // When
        val result = appCategorizer.categorizeApp(packageName)
        
        // Then
        assertEquals(AppCategories.FINANCE, result)
    }
    
    @Test
    fun `categorizeApp returns default category for unknown app`() = runTest {
        // Given
        val packageName = "com.random.unknown"
        coEvery { mockAppCategoryDao.getCategoryByPackage(packageName) } returns null
        every { mockPackageManager.getApplicationInfo(packageName, 0) } throws PackageManager.NameNotFoundException()
        every { mockPackageManager.getApplicationLabel(any()) } returns "Unknown App"
        coEvery { mockAppCategoryDao.insertCategory(any()) } returns Unit
        
        // When
        val result = appCategorizer.categorizeApp(packageName)
        
        // Then
        assertEquals(AppCategories.OTHER, result)
    }
    
    @Test
    fun `categorizeApp handles exceptions gracefully`() = runTest {
        // Given
        val packageName = "com.test.error"
        coEvery { mockAppCategoryDao.getCategoryByPackage(packageName) } throws RuntimeException("Database error")
        
        // When
        val result = appCategorizer.categorizeApp(packageName)
        
        // Then
        assertEquals(AppCategories.OTHER, result)
    }
}