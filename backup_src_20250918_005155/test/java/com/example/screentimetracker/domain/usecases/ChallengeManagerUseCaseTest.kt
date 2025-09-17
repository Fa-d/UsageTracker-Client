package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.Challenge
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class ChallengeManagerUseCaseTest {

    private val mockRepository = mockk<TrackerRepository>(relaxed = true)
    private val mockNotificationManager = mockk<AppNotificationManager>(relaxed = true)
    private val mockAppLogger = mockk<AppLogger>(relaxed = true)
    private lateinit var challengeManager: ChallengeManagerUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        challengeManager = ChallengeManagerUseCase(mockRepository, mockNotificationManager, mockAppLogger)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createWeeklyChallenges should create all 5 challenge types`() = runTest {
        coEvery { mockRepository.insertChallenge(any()) } returns 1L

        challengeManager.createWeeklyChallenges()

        // Verify all 5 challenge types are created
        coVerify(exactly = 5) { mockRepository.insertChallenge(any()) }
        
        val challengeSlots = mutableListOf<Challenge>()
        coVerify { mockRepository.insertChallenge(capture(challengeSlots)) }
        
        val challengeIds = challengeSlots.map { it.challengeId }
        assertTrue(challengeIds.contains(ChallengeManagerUseCase.PHONE_FREE_MEAL))
        assertTrue(challengeIds.contains(ChallengeManagerUseCase.DIGITAL_SUNSET))
        assertTrue(challengeIds.contains(ChallengeManagerUseCase.FOCUS_MARATHON))
        assertTrue(challengeIds.contains(ChallengeManagerUseCase.APP_MINIMALIST))
        assertTrue(challengeIds.contains(ChallengeManagerUseCase.STEP_AWAY))
    }

    @Test
    fun `updateChallengeProgress should complete challenge when target reached`() = runTest {
        val testChallenge = Challenge(
            id = 1,
            challengeId = ChallengeManagerUseCase.PHONE_FREE_MEAL,
            name = "Phone-Free Meals",
            description = "Test description",
            emoji = "📱",
            targetValue = 5,
            status = "active",
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
            currentProgress = 4
        )

        coEvery { mockRepository.getLatestChallengeByType(ChallengeManagerUseCase.PHONE_FREE_MEAL) } returns testChallenge
        coEvery { mockRepository.updateChallengeProgress(any(), any()) } just Runs
        coEvery { mockRepository.updateChallengeStatus(any(), any()) } just Runs

        challengeManager.updateChallengeProgress(ChallengeManagerUseCase.PHONE_FREE_MEAL, 5)

        // Verify progress update
        coVerify { mockRepository.updateChallengeProgress(1L, 5) }
        
        // Verify status update to completed
        coVerify { mockRepository.updateChallengeStatus(1L, "completed") }
        
        // Verify notification is sent
        coVerify { mockNotificationManager.showMotivationBoost(any()) }
    }

    @Test
    fun `checkPhoneFreeMeal should update progress correctly for successful meal`() = runTest {
        val testChallenge = Challenge(
            id = 1,
            challengeId = ChallengeManagerUseCase.PHONE_FREE_MEAL,
            name = "Phone-Free Meals",
            description = "Test description",
            emoji = "📱",
            targetValue = 5,
            status = "active",
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
            currentProgress = 2
        )

        coEvery { mockRepository.getLatestChallengeByType(ChallengeManagerUseCase.PHONE_FREE_MEAL) } returns testChallenge
        coEvery { mockRepository.updateChallengeProgress(any(), any()) } just Runs

        val mealStart = System.currentTimeMillis()
        val mealEnd = mealStart + TimeUnit.MINUTES.toMillis(30)
        
        challengeManager.checkPhoneFreemeal(mealStart, mealEnd, hadPhoneUsage = false)

        // Should increment progress by 1
        coVerify { mockRepository.updateChallengeProgress(1L, 3) }
    }

    @Test
    fun `checkPhoneFreeMeal should not update progress for failed meal`() = runTest {
        val testChallenge = Challenge(
            id = 1,
            challengeId = ChallengeManagerUseCase.PHONE_FREE_MEAL,
            name = "Phone-Free Meals",
            description = "Test description",
            emoji = "📱",
            targetValue = 5,
            status = "active",
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
            currentProgress = 2
        )

        coEvery { mockRepository.getLatestChallengeByType(ChallengeManagerUseCase.PHONE_FREE_MEAL) } returns testChallenge
        coEvery { mockRepository.updateChallengeProgress(any(), any()) } just Runs

        val mealStart = System.currentTimeMillis()
        val mealEnd = mealStart + TimeUnit.MINUTES.toMillis(30)
        
        challengeManager.checkPhoneFreemeal(mealStart, mealEnd, hadPhoneUsage = true)

        // Progress should remain the same (2 + 0 = 2)
        coVerify { mockRepository.updateChallengeProgress(1L, 2) }
    }

    @Test
    fun `getActiveChallenges should return flow from repository`() = runTest {
        val mockChallenges = listOf(
            Challenge(
                id = 1,
                challengeId = ChallengeManagerUseCase.DIGITAL_SUNSET,
                name = "Digital Sunset",
                description = "Test description",
                emoji = "🌙",
                targetValue = 5,
                status = "active",
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
                currentProgress = 2
            )
        )
        
        every { mockRepository.getActiveChallenges(any()) } returns flowOf(mockChallenges)

        val result = challengeManager.getActiveChallenges()
        
        verify { mockRepository.getActiveChallenges(any()) }
        assertNotNull(result)
    }

    @Test
    fun `checkFocusMarathon should update progress when 3 or more sessions completed`() = runTest {
        val testChallenge = Challenge(
            id = 1,
            challengeId = ChallengeManagerUseCase.FOCUS_MARATHON,
            name = "Focus Marathon",
            description = "Complete 3 focus sessions in a day",
            emoji = "🎯",
            targetValue = 3,
            status = "active",
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
            currentProgress = 0
        )

        coEvery { mockRepository.getLatestChallengeByType(ChallengeManagerUseCase.FOCUS_MARATHON) } returns testChallenge
        coEvery { mockRepository.updateChallengeProgress(any(), any()) } just Runs

        challengeManager.checkFocusMarathon(3)

        coVerify { mockRepository.updateChallengeProgress(1L, 3) }
    }

    @Test
    fun `checkFocusMarathon should not update progress for less than 3 sessions`() = runTest {
        challengeManager.checkFocusMarathon(2)

        // Should not call any repository methods
        coVerify(exactly = 0) { mockRepository.updateChallengeProgress(any(), any()) }
    }

    @Test
    fun `checkAppMinimalist should update progress when 5 or fewer apps used`() = runTest {
        val testChallenge = Challenge(
            id = 1,
            challengeId = ChallengeManagerUseCase.APP_MINIMALIST,
            name = "App Minimalist",
            description = "Use only 5 essential apps for a day",
            emoji = "📖",
            targetValue = 1,
            status = "active",
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
            currentProgress = 0
        )

        coEvery { mockRepository.getLatestChallengeByType(ChallengeManagerUseCase.APP_MINIMALIST) } returns testChallenge
        coEvery { mockRepository.updateChallengeProgress(any(), any()) } just Runs

        challengeManager.checkAppMinimalist(5)

        coVerify { mockRepository.updateChallengeProgress(1L, 1) }
    }

    @Test
    fun `expireOldChallenges should mark completed challenges as completed and failed ones as failed`() = runTest {
        val completedChallenge = Challenge(
            id = 1,
            challengeId = ChallengeManagerUseCase.DIGITAL_SUNSET,
            name = "Digital Sunset",
            description = "Test description",
            emoji = "🌙",
            targetValue = 5,
            status = "active",
            startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(8),
            endDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            currentProgress = 5
        )

        val failedChallenge = Challenge(
            id = 2,
            challengeId = ChallengeManagerUseCase.PHONE_FREE_MEAL,
            name = "Phone-Free Meals",
            description = "Test description",
            emoji = "📱",
            targetValue = 5,
            status = "active",
            startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(8),
            endDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            currentProgress = 2
        )

        coEvery { mockRepository.getExpiredChallenges(any()) } returns listOf(completedChallenge, failedChallenge)
        coEvery { mockRepository.updateChallengeStatus(any(), any()) } just Runs

        challengeManager.expireOldChallenges()

        // Verify completed challenge is marked as completed
        coVerify { mockRepository.updateChallengeStatus(1L, "completed") }
        
        // Verify failed challenge is marked as failed
        coVerify { mockRepository.updateChallengeStatus(2L, "failed") }
    }
}