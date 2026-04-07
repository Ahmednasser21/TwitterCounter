package com.halan.twittercounter.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halan.twittercounter.R
import com.halan.twittercounter.ui.theme.AppCardBorder
import com.halan.twittercounter.ui.theme.AppCardLabelBackground
import com.halan.twittercounter.ui.theme.AppTextFieldBorder
import com.halan.twittercounter.ui.theme.ClearRed
import com.halan.twittercounter.ui.theme.CopyGreen
import com.halan.twittercounter.ui.theme.OverLimitRed
import com.halan.twittercounter.ui.theme.TwitterCounterTheme

@Composable
fun TwitterCounterScreen(
    uiState: TwitterCounterUiState = TwitterCounterUiState(),
    onEvent: (TwitterCounterEvent) -> Unit = {},
) {
    TwitterCounterContent(
        uiState = uiState,
        onEvent = onEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwitterCounterContent(
    uiState: TwitterCounterUiState,
    onEvent: (TwitterCounterEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.screen_title),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                actions = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.cd_navigate),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .size(24.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_twitter_bird),
                contentDescription = stringResource(R.string.cd_twitter_logo),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CharacterStatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_characters_typed),
                    value = "${uiState.charactersTyped}/${TwitterCounterUiState.MAX_TWEET_LENGTH}",
                    valueColor = if (uiState.isOverLimit) OverLimitRed else MaterialTheme.colorScheme.onSurface,
                )
                CharacterStatCard(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_characters_remaining),
                    value = "${uiState.charactersRemaining}",
                    valueColor = if (uiState.isOverLimit) OverLimitRed else MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.tweetText,
                onValueChange = { onEvent(TwitterCounterEvent.OnTextChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.text_field_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = AppTextFieldBorder,
                    unfocusedBorderColor = AppTextFieldBorder,
                ),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = { onEvent(TwitterCounterEvent.OnCopyText) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CopyGreen),
                ) {
                    Text(
                        text = stringResource(R.string.btn_copy_text),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
                Button(
                    onClick = { onEvent(TwitterCounterEvent.OnClearText) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClearRed),
                ) {
                    Text(
                        text = stringResource(R.string.btn_clear_text),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onEvent(TwitterCounterEvent.OnPostTweet) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                ),
                enabled = uiState.tweetText.isNotBlank() && !uiState.isOverLimit,
            ) {
                Text(
                    text = stringResource(R.string.btn_post_tweet),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CharacterStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = AppCardBorder,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = AppCardLabelBackground,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                )
                .padding(horizontal = 12.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TwitterCounterScreenPreview() {
    TwitterCounterTheme {
        TwitterCounterScreen()
    }
}