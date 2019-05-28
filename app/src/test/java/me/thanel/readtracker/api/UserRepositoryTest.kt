package me.thanel.readtracker.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import me.thanel.readtracker.Preferences
import me.thanel.readtracker.testbase.BaseRepositoryTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest : BaseRepositoryTest() {

    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        userRepository = UserRepository(goodreadsApi)
    }

    @Test
    fun `should fetch user id from api if it hasn't been saved to preferences`() = runBlocking {
        Preferences.userId = null
        `when`(goodreadsApi.getUserId()).thenReturn(12L)

        val userId = userRepository.getUserId()

        assertThat(userId, equalTo(12L))
    }

    @Test
    fun `should return user id from preferences if it has been saved`() = runBlocking {
        Preferences.userId = 7L

        val userId = userRepository.getUserId()

        assertThat(userId, equalTo(7L))
        verify(goodreadsApi, never()).getUserId()
        return@runBlocking
    }

    @Test
    fun `should save user id to preferences after fetching it from api`() = runBlocking {
        Preferences.userId = null
        `when`(goodreadsApi.getUserId()).thenReturn(15L)

        userRepository.getUserId()

        assertThat(Preferences.userId, equalTo(15L))
    }
}
