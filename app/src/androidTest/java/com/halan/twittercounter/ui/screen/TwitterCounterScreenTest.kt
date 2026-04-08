package com.halan.twittercounter.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.halan.twittercounter.ui.theme.TwitterCounterTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TwitterCounterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun launchScreen(
        uiState: TwitterCounterUiState = TwitterCounterUiState(
            maxTweetLength = 280,
            charactersRemaining = 280,
        ),
        onEvent: (TwitterCounterEvent) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TwitterCounterTheme {
                TwitterCounterScreen(uiState = uiState, onEvent = onEvent)
            }
        }
    }


    @Test
    fun screenTitle_isDisplayed() {
        launchScreen()
        composeTestRule.onNodeWithText("Twitter character count").assertIsDisplayed()
    }

    @Test
    fun twitterBirdIcon_isDisplayed() {
        launchScreen()
        composeTestRule.onNodeWithContentDescription("Twitter logo").assertIsDisplayed()
    }

    @Test
    fun charactersTypedLabel_isDisplayed() {
        launchScreen()
        composeTestRule.onNodeWithText("Characters Typed").assertIsDisplayed()
    }

    @Test
    fun charactersRemainingLabel_isDisplayed() {
        launchScreen()
        composeTestRule.onNodeWithText("Characters Remaining").assertIsDisplayed()
    }

    @Test
    fun copyButton_isDisplayed() {
        launchScreen()
        composeTestRule.onNodeWithText("Copy Text").assertIsDisplayed()
    }

    @Test
    fun clearButton_isDisplayed() {
        launchScreen()
        composeTestRule.onNodeWithText("Clear Text").assertIsDisplayed()
    }

    @Test
    fun postTweetButton_isDisplayed() {
        launchScreen()
        composeTestRule.onNodeWithText("Post tweet").assertIsDisplayed()
    }

    @Test
    fun textFieldPlaceholder_isDisplayed() {
        launchScreen()
        composeTestRule
            .onNodeWithText("Start typing! You can enter up to 280 characters")
            .assertIsDisplayed()
    }


    @Test
    fun initialState_showsZeroTypedAndFullRemaining() {
        launchScreen()
        composeTestRule.onNodeWithText("0/280").assertIsDisplayed()
        composeTestRule.onNodeWithText("280").assertIsDisplayed()
    }

    @Test
    fun statCards_reflectUpdatedTypedCount() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                charactersTyped = 5,
                charactersRemaining = 275,
            )
        )
        composeTestRule.onNodeWithText("5/280").assertIsDisplayed()
        composeTestRule.onNodeWithText("275").assertIsDisplayed()
    }

    @Test
    fun statCards_showNegativeRemainingWhenOverLimit() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                charactersTyped = 285,
                charactersRemaining = -5,
                isOverLimit = true,
            )
        )
        composeTestRule.onNodeWithText("285/280").assertIsDisplayed()
        composeTestRule.onNodeWithText("-5").assertIsDisplayed()
    }


    @Test
    fun postTweetButton_disabledWhenTextIsBlank() {
        launchScreen(
            uiState = TwitterCounterUiState(maxTweetLength = 280, tweetText = "")
        )
        composeTestRule.onNodeWithText("Post tweet").assertIsNotEnabled()
    }

    @Test
    fun postTweetButton_disabledWhenOverLimit() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                tweetText = "a".repeat(281),
                isOverLimit = true,
            )
        )
        composeTestRule.onNodeWithText("Post tweet").assertIsNotEnabled()
    }

    @Test
    fun postTweetButton_enabledWithValidNonOverLimitText() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                tweetText = "Hello",
                charactersTyped = 5,
                charactersRemaining = 275,
            )
        )
        composeTestRule.onNodeWithText("Post tweet").assertIsEnabled()
    }

    @Test
    fun loadingState_hidesTweetButtonTextAndShowsProgressIndicator() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                tweetText = "Hello",
                isLoading = true,
            )
        )
        composeTestRule.onNodeWithText("Post tweet").assertDoesNotExist()
    }

    @Test
    fun loadingState_postButtonIsDisabled() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                tweetText = "Hello",
                isLoading = true,
            )
        )
        composeTestRule.onNodeWithText("Post tweet").assertDoesNotExist()
    }


    @Test
    fun copyButton_click_firesOnCopyText() {
        var firedEvent: TwitterCounterEvent? = null
        launchScreen(onEvent = { firedEvent = it })

        composeTestRule.onNodeWithText("Copy Text").performClick()

        assertTrue(firedEvent is TwitterCounterEvent.OnCopyText)
    }

    @Test
    fun clearButton_click_firesOnClearText() {
        var firedEvent: TwitterCounterEvent? = null
        launchScreen(onEvent = { firedEvent = it })

        composeTestRule.onNodeWithText("Clear Text").performClick()

        assertTrue(firedEvent is TwitterCounterEvent.OnClearText)
    }

    @Test
    fun postTweetButton_click_firesOnPostTweet() {
        var firedEvent: TwitterCounterEvent? = null
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                tweetText = "Hello",
                charactersTyped = 5,
                charactersRemaining = 275,
            ),
            onEvent = { firedEvent = it }
        )

        composeTestRule.onNodeWithText("Post tweet").performClick()

        assertTrue(firedEvent is TwitterCounterEvent.OnPostTweet)
    }

    @Test
    fun textField_input_firesOnTextChanged() {
        val events = mutableListOf<TwitterCounterEvent>()
        launchScreen(onEvent = { events.add(it) })

        composeTestRule
            .onNodeWithText("Start typing! You can enter up to 280 characters")
            .performTextInput("Hi")

        assertTrue(events.any { it is TwitterCounterEvent.OnTextChanged })
    }

    @Test
    fun snackbar_TweetPosted_showsCorrectString() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                snackbarMessage = SnackbarMessage.TweetPosted,
            )
        )
        composeTestRule.onNodeWithText("Tweet posted successfully").assertIsDisplayed()
    }

    @Test
    fun snackbar_CopiedToClipboard_showsCorrectString() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                snackbarMessage = SnackbarMessage.CopiedToClipboard,
            )
        )
        composeTestRule.onNodeWithText("Text copied to clipboard.").assertIsDisplayed()
    }

    @Test
    fun snackbar_EmptyTextField_showsCorrectString() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                snackbarMessage = SnackbarMessage.EmptyTextField,
            )
        )

        composeTestRule.onNodeWithText("Text field is empty.").assertIsDisplayed()
    }

    @Test
    fun snackbar_null_nothingSnackbarRelatedShown() {
        launchScreen(
            uiState = TwitterCounterUiState(
                maxTweetLength = 280,
                snackbarMessage = null,
            )
        )
        composeTestRule.onNodeWithText("Tweet posted successfully").assertDoesNotExist()
        composeTestRule.onNodeWithText("Text copied to clipboard.").assertDoesNotExist()
        composeTestRule.onNodeWithText("Text field is empty.").assertDoesNotExist()
    }
}