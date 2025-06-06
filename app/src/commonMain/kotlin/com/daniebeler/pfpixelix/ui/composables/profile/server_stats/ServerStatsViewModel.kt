package com.daniebeler.pfpixelix.ui.composables.profile.server_stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daniebeler.pfpixelix.domain.service.instance.InstanceService
import com.daniebeler.pfpixelix.domain.service.platform.Platform
import com.daniebeler.pfpixelix.domain.service.utils.Resource
import com.daniebeler.pfpixelix.ui.composables.profile.DomainSoftwareState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

class ServerStatsViewModel @Inject constructor(
    private val platform: Platform,
    private val instanceService: InstanceService,
) : ViewModel() {

    var statsState by mutableStateOf(DomainSoftwareState())

    fun getData(domain: String) {
        getFediServer(domain)
    }

    private fun getFediServer(domain: String) {
        instanceService.getServerFromFediDB(domain).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    statsState = DomainSoftwareState(
                        fediServer = result.data, fediSoftware = statsState.fediSoftware
                    )
                    if (result.data?.software?.name?.isNotEmpty() == true) {
                        getFediSoftware(result.data.software.name.lowercase())
                    }
                }

                is Resource.Error -> {
                    statsState = DomainSoftwareState(
                        error = result.message ?: "An unexpected error occurred",
                        fediServer = statsState.fediServer,
                        fediSoftware = statsState.fediSoftware
                    )
                }

                is Resource.Loading -> {
                    statsState = DomainSoftwareState(
                        isLoading = true,
                        fediServer = statsState.fediServer,
                        fediSoftware = statsState.fediSoftware
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getFediSoftware(softwareSlug: String) {
        instanceService.getSoftwareFromFediDB(softwareSlug).onEach { result ->
            statsState = when (result) {
                is Resource.Success -> {
                    DomainSoftwareState(
                        fediSoftware = result.data, fediServer = statsState.fediServer
                    )
                }

                is Resource.Error -> {
                    DomainSoftwareState(
                        error = result.message ?: "An unexpected error occurred",
                        fediServer = statsState.fediServer,
                        fediSoftware = statsState.fediSoftware
                    )
                }

                is Resource.Loading -> {
                    DomainSoftwareState(
                        isLoading = true,
                        fediServer = statsState.fediServer,
                        fediSoftware = statsState.fediSoftware
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun openUrl(url: String) {
        platform.openUrl(url)
    }

}