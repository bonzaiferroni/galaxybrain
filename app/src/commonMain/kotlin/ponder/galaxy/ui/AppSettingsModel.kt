package ponder.galaxy.ui

import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class AppSettingsModel(
    private val valueRepo: ValueRepository = LocalValueRepository()
): StateModel<AppSettingsState>() {
    override val state = ViewState(AppSettingsState(
        redditUsername = valueRepo.readString(REDDIT_USERNAME_KEY, ""),
        redditPassword = valueRepo.readString(REDDIT_PASSWORD_KEY, ""),
        redditAppId = valueRepo.readString(REDDIT_APP_ID_KEY, ""),
        redditAppSecret = valueRepo.readString(REDDIT_APP_SECRET_KEY, "")
    ))

    fun setRedditUsername(value: String) {
        valueRepo.writeString(REDDIT_USERNAME_KEY, value)
        setState { it.copy(redditUsername = value) }
    }

    fun setRedditPassword(value: String) {
        valueRepo.writeString(REDDIT_PASSWORD_KEY, value)
        setState { it.copy(redditPassword = value) }
    }

    fun setRedditAppId(value: String) {
        valueRepo.writeString(REDDIT_APP_ID_KEY, value)
        setState { it.copy(redditAppId = value) }
    }

    fun setRedditAppSecret(value: String) {
        valueRepo.writeString(REDDIT_APP_SECRET_KEY, value)
        setState { it.copy(redditAppSecret = value) }
    }
}

data class AppSettingsState(
    val redditUsername: String,
    val redditPassword: String,
    val redditAppId: String,
    val redditAppSecret: String
)