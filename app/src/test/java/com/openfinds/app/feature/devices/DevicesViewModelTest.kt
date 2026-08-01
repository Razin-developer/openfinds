package com.openfinds.app.feature.devices

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.openfinds.app.core.domain.model.ConnectionState
import com.openfinds.app.core.domain.model.DeviceGroup
import com.openfinds.app.core.domain.model.TrustedDevice
import com.openfinds.app.core.domain.repository.DeviceGroupRepository
import com.openfinds.app.core.domain.repository.DeviceRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevicesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val devices = MutableStateFlow<List<TrustedDevice>>(emptyList())
    private val groups = MutableStateFlow<List<DeviceGroup>>(emptyList())

    private fun device(
        id: String,
        name: String,
        online: Boolean,
        groupId: String? = null,
    ) = TrustedDevice(
        id = id,
        displayName = name,
        nickname = null,
        avatarColorArgb = 0,
        publicKeyBase64 = "key-$id",
        lastKnownHost = null,
        lastKnownPort = null,
        pairedAtEpochMillis = 0,
        lastSeenEpochMillis = if (online) System.currentTimeMillis() else 0,
        groupId = groupId,
        connectionState = if (online) ConnectionState.ONLINE else ConnectionState.OFFLINE,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DevicesViewModel {
        val deviceRepository = mockk<DeviceRepository>()
        every { deviceRepository.observeTrustedDevices() } returns devices
        val groupRepository = mockk<DeviceGroupRepository>()
        every { groupRepository.observeGroups() } returns groups
        return DevicesViewModel(deviceRepository, groupRepository)
    }

    @Test
    fun `online devices sort before offline ones by default`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()
            devices.value =
                listOf(
                    device("1", "Zed", online = false),
                    device("2", "Anna", online = true),
                )

            viewModel.uiState.test {
                dispatcher.scheduler.advanceUntilIdle()
                val state = expectMostRecentItem()
                assertThat(state.visibleDevices.map { it.id }).containsExactly("2", "1").inOrder()
            }
        }

    @Test
    fun `search query filters by name case-insensitively`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()
            devices.value = listOf(device("1", "Pixel 8", online = true), device("2", "Galaxy S24", online = true))

            viewModel.onQueryChange("pixel")

            viewModel.uiState.test {
                dispatcher.scheduler.advanceUntilIdle()
                val state = expectMostRecentItem()
                assertThat(state.visibleDevices.map { it.id }).containsExactly("1")
            }
        }

    @Test
    fun `group filter only shows devices in the selected group`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()
            devices.value =
                listOf(
                    device("1", "Work Phone", online = true, groupId = "work"),
                    device("2", "Home Tablet", online = true, groupId = "home"),
                )

            viewModel.onGroupFilterChange("work")

            viewModel.uiState.test {
                dispatcher.scheduler.advanceUntilIdle()
                val state = expectMostRecentItem()
                assertThat(state.visibleDevices.map { it.id }).containsExactly("1")
            }
        }
}
