package plus.maa.backend.controller.response.user

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileStatsInfo(
    val userId: String,
    val followingCount: Int,
    val fansCount: Int,
    val copilotCount: Long,
    val receivedLikeCount: Long,
)
