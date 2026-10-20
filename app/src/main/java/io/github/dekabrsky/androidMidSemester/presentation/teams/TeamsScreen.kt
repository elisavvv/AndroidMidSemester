package io.github.dekabrsky.androidMidSemester.presentation.teams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import io.github.dekabrsky.androidMidSemester.presentation.ui.common.CountryPickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import io.github.dekabrsky.androidMidSemester.R
import io.github.dekabrsky.androidMidSemester.domain.model.Team
import io.github.dekabrsky.androidMidSemester.presentation.ui.common.LoadingState
import io.github.dekabrsky.androidMidSemester.presentation.ui.common.ErrorState
import io.github.dekabrsky.androidMidSemester.presentation.ui.common.errorMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamsScreen(onBack: () -> Unit, viewModel: TeamsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val countries by viewModel.countries.collectAsStateWithLifecycle()
    var countryPickerOpen by rememberSaveable { mutableStateOf(false) }

    if (countryPickerOpen) {
        CountryPickerDialog(
            state = countries,
            onSelect = { viewModel.selectCountry(it); countryPickerOpen = false },
            onRetry = viewModel::retryCountries,
            onDismiss = { countryPickerOpen = false },
        )
    }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.teams_back)) }
                Text(
                    text = stringResource(R.string.teams_title),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                TextButton(
                    onClick = viewModel::clearSelection,
                    enabled = state.selectedTeamIds.isNotEmpty(),
                ) { Text(stringResource(R.string.teams_reset)) }
            }
            Text(
                text = if (state.selectedTeamIds.isEmpty()) {
                    stringResource(R.string.teams_all_hint)
                } else {
                    stringResource(R.string.teams_selected_count, state.selectedTeamIds.size)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = !state.showSelected,
                    onClick = { countryPickerOpen = true },
                    label = { Text(state.country) },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = state.showSelected,
                    onClick = { viewModel.showSelected(!state.showSelected) },
                    label = { Text(stringResource(R.string.teams_selected)) },
                )
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::search,
                label = { Text(stringResource(R.string.teams_search)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val error = state.error
                when {
                    state.loading -> LoadingState()
                    error != null -> ErrorState(message = errorMessage(error), onRetry = viewModel::retry)
                    state.teams.isEmpty() -> Text(
                        text = stringResource(
                            if (state.showSelected && state.selectedTeamIds.isEmpty()) R.string.teams_no_selection
                            else R.string.teams_empty,
                        ),
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    )
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.teams, key = { it.id }) { team ->
                            TeamSelectionItem(
                                team = team,
                                selected = team.id in state.selectedTeamIds,
                                onSelectedChange = { viewModel.setSelected(team.id, it) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamSelectionItem(team: Team, selected: Boolean, onSelectedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = onSelectedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = team.logoUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            placeholder = painterResource(R.drawable.ic_team_placeholder),
            error = painterResource(R.drawable.ic_team_placeholder),
            fallback = painterResource(R.drawable.ic_team_placeholder),
        )
        Text(team.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Checkbox(checked = selected, onCheckedChange = null)
    }
}