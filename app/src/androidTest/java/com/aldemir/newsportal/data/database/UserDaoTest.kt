package com.aldemir.newsportal.data.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.aldemir.newsportal.models.User
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@HiltAndroidTest
@SmallTest
class UserDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
    lateinit var database: ConfigDataBase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        hiltRule.inject()
        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertUser() = runTest {
        val user = User(
            id = 1,
            name = "Clint",
            email = "Clint@gmail.com",
            isLogged = false,
        )
        val userId = userDao.insert(user)
        println("User Id: $userId")

        val allUsers = userDao.getAllUsers()
        assertThat(allUsers).contains(user)
    }

    @Test
    fun updateUser() = runTest {
        val user1 = User(
            id = 1,
            name = "Test",
            email = "Test@gmail.com",
            isLogged = false,
        )
        val user2 = User(
            id = 1,
            name = "Clint",
            email = "Clint@gmail.com",
            isLogged = false,
        )
        val userId = userDao.insert(user1)
        assertThat(userId).isEqualTo(user1.id)

        userDao.update(user2)
        val allUsers = userDao.getAllUsers()
        assertThat(allUsers).contains(user2)

    }

    @Test
    fun getUserEmail() = runTest {
        val user1 = User(
            id = 1,
            name = "Test",
            email = "Test@gmail.com",
            isLogged = false,
        )
        val userId = userDao.insert(user1)
        assertThat(userId).isEqualTo(user1.id)

        val user = userDao.getUserEmail(user1.email)
        assertThat(user!!.email).isEqualTo(user1.email)

    }

    @Test
    fun getAllUsers() = runTest {
        val user1 = User(
            id = 1,
            name = "Test",
            email = "Test@gmail.com",
            isLogged = false,
        )
        val user2 = User(
            id = 2,
            name = "Clint",
            email = "Clint@gmail.com",
            isLogged = false,
        )
        userDao.insert(user1)
        userDao.insert(user2)

        val allUsers = userDao.getAllUsers()

        assertThat(allUsers.size).isEqualTo(2)

    }

    @Test
    fun getUserLogged() = runTest {
        val user1 = User(
            id = 1,
            name = "Test",
            email = "Test@gmail.com",
            isLogged = true,
        )
        val user2 = User(
            id = 2,
            name = "Clint",
            email = "Clint@gmail.com",
            isLogged = false,
        )
        userDao.insert(user1)
        userDao.insert(user2)

        val userLogged = userDao.getUserLogged(user1.email, isLogged = true)

        assertThat(userLogged.email).isEqualTo(user1.email)

    }

    @Test
    fun deleteUser() = runTest {
        val user1 = User(
            id = 1,
            name = "Test",
            email = "Test@gmail.com",
            isLogged = true,
        )
        val user2 = User(
            id = 2,
            name = "Clint",
            email = "Clint@gmail.com",
            isLogged = false,
        )
        userDao.insert(user1)
        userDao.insert(user2)

        val allUsers1 = userDao.getAllUsers()

        assertThat(allUsers1.size).isEqualTo(2)

        userDao.delete(user1)

        val allUsers2 = userDao.getAllUsers()

        assertThat(allUsers2.size).isEqualTo(1)

    }

}