package io.github.dekabrsky.androidMidSemester.data.repository

import android.content.Context
import io.github.dekabrsky.androidMidSemester.domain.model.Team
import io.github.dekabrsky.androidMidSemester.domain.repository.TeamSelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections

class SharedPrefTeamSelectionRepository(
    context: Context
) : TeamSelectionRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _selectedTeams = MutableStateFlow<Map<Long, Team>>(loadTeams())
    override val selectedTeams = _selectedTeams.asStateFlow()

    override fun setSelected(team: Team, selected: Boolean) {
        _selectedTeams.update { current ->
            val updated = if (selected) current + (team.id to team) else current - team.id
            saveTeams(updated)
            Collections.unmodifiableMap(updated)
        }
    }

    override fun clear() {
        _selectedTeams.value = emptyMap()
        prefs.edit().remove(KEY_SELECTED_TEAMS).apply()
    }

    private fun saveTeams(teams: Map<Long, Team>) {
        val jsonArray = JSONArray()
        teams.values.forEach { team ->
            val jsonObject = JSONObject().apply {
                put("id", team.id)
                put("name", team.name)
                put("logoUrl", team.logoUrl)
            }
            jsonArray.put(jsonObject)
        }
        prefs.edit().putString(KEY_SELECTED_TEAMS, jsonArray.toString()).apply()
    }

    private fun loadTeams(): Map<Long, Team> {
        val jsonString = prefs.getString(KEY_SELECTED_TEAMS, null) ?: return emptyMap()
        val result = mutableMapOf<Long, Team>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getLong("id")
                val name = jsonObject.getString("name")
                val logoUrl = if (jsonObject.has("logoUrl") && !jsonObject.isNull("logoUrl")) {
                    jsonObject.getString("logoUrl")
                } else null

                val team = Team(id = id, name = name, logoUrl = logoUrl)
                result[id] = team
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    companion object {
        private const val PREFS_NAME = "team_selection_prefs"
        private const val KEY_SELECTED_TEAMS = "selected_teams"
    }
}