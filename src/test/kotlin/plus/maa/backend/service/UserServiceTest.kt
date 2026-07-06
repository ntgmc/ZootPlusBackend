package plus.maa.backend.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ktorm.database.Database
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.server.ResponseStatusException
import plus.maa.backend.repository.entity.UserEntity
import plus.maa.backend.repository.ktorm.CopilotKtormRepository
import plus.maa.backend.repository.ktorm.UserKtormRepository
import plus.maa.backend.service.jwt.JwtService
import java.time.Instant

class UserServiceTest {
    private val database = mockk<Database>()
    private val userKtormRepository = mockk<UserKtormRepository>()
    private val copilotKtormRepository = mockk<CopilotKtormRepository>()
    private val emailService = mockk<EmailService>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val userDetailService = mockk<UserDetailServiceImpl>()
    private val jwtService = mockk<JwtService>()

    private val service = UserService(
        database,
        userKtormRepository,
        copilotKtormRepository,
        emailService,
        passwordEncoder,
        userDetailService,
        jwtService,
    )

    @Test
    fun getProfileStatsReturnsUserAndCopilotStats() {
        val user = userEntity(userId = 123L, followingCount = 7, fansCount = 8)
        every { userKtormRepository.findById(123L) } returns user
        every { copilotKtormRepository.getPublicProfileStatsByUploader(123L) } returns (5L to 34L)

        val result = service.getProfileStats(123L)

        check(result.userId == "123")
        check(result.followingCount == 7)
        check(result.fansCount == 8)
        check(result.copilotCount == 5L)
        check(result.receivedLikeCount == 34L)
    }

    @Test
    fun getProfileStatsFailsWhenUserNotFound() {
        every { userKtormRepository.findById(404L) } returns null

        val error = assertThrows<ResponseStatusException> {
            service.getProfileStats(404L)
        }

        check(error.statusCode == HttpStatus.NOT_FOUND)
    }

    @Test
    fun getProfileStatsReturnsZeroWhenUserHasNoPublicCopilots() {
        val user = userEntity(userId = 456L, followingCount = 0, fansCount = 1)
        every { userKtormRepository.findById(456L) } returns user
        every { copilotKtormRepository.getPublicProfileStatsByUploader(456L) } returns (0L to 0L)

        val result = service.getProfileStats(456L)

        check(result.userId == "456")
        check(result.followingCount == 0)
        check(result.fansCount == 1)
        check(result.copilotCount == 0L)
        check(result.receivedLikeCount == 0L)
    }

    private fun userEntity(userId: Long, followingCount: Int, fansCount: Int): UserEntity {
        return UserEntity {
            this.userId = userId
            this.userName = "user$userId"
            this.email = "user$userId@example.com"
            this.password = "encoded"
            this.status = 1
            this.pwdUpdateTime = Instant.now()
            this.followingCount = followingCount
            this.fansCount = fansCount
        }
    }
}
