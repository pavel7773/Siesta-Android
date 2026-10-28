package com.example.catering.presentation.user

import com.example.catering.domain.model.User
import com.example.catering.domain.usecase.user.LoginUserResult
import com.example.catering.domain.usecase.user.LoginUserUseCase
import com.example.catering.domain.usecase.user.LogoutUserResult
import com.example.catering.domain.usecase.user.LogoutUserUseCase
import com.example.catering.domain.usecase.user.PhoneRegistrationUseCase
import com.example.catering.domain.usecase.user.RegisterUserResult
import com.example.catering.domain.usecase.user.RegisterUserUseCase
import com.example.catering.domain.usecase.user.ResendPhoneCodeResult
import com.example.catering.domain.usecase.user.RestoreSessionResult
import com.example.catering.domain.usecase.user.RestoreSessionUseCase
import com.example.catering.domain.usecase.user.UpdateUserAddressResult
import com.example.catering.domain.usecase.user.UpdateUserAddressUseCase
import com.example.catering.domain.usecase.user.UpdateUserNameResult
import com.example.catering.domain.usecase.user.UpdateUserNameUseCase
import com.example.catering.domain.usecase.user.VerifyPhoneResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserViewModelTest {

    private val testDispatcher =
        StandardTestDispatcher()

    private lateinit var restoreSessionUseCase:
            RestoreSessionUseCase

    private lateinit var registerUserUseCase:
            RegisterUserUseCase

    private lateinit var loginUserUseCase:
            LoginUserUseCase

    private lateinit var logoutUserUseCase:
            LogoutUserUseCase

    private lateinit var updateUserAddressUseCase:
            UpdateUserAddressUseCase

    private lateinit var updateUserNameUseCase:
            UpdateUserNameUseCase

    private lateinit var phoneRegistrationUseCase:
            PhoneRegistrationUseCase

    private lateinit var viewModel:
            UserViewModel

    @Before
    fun setup() {

        Dispatchers.setMain(testDispatcher)

        restoreSessionUseCase = mockk()
        registerUserUseCase = mockk()
        loginUserUseCase = mockk()
        logoutUserUseCase = mockk()
        updateUserAddressUseCase = mockk()
        updateUserNameUseCase = mockk()
        phoneRegistrationUseCase = mockk()

        viewModel =
            UserViewModel(
                restoreSessionUseCase =
                    restoreSessionUseCase,
                registerUserUseCase =
                    registerUserUseCase,
                loginUserUseCase =
                    loginUserUseCase,
                logoutUserUseCase =
                    logoutUserUseCase,
                updateUserAddressUseCase =
                    updateUserAddressUseCase,
                updateUserNameUseCase =
                    updateUserNameUseCase,
                changePasswordUseCase =
                    mockk(),
                biometricSessionManager =
                    mockk {
                        coEvery {
                            isBiometricEnabled()
                        } returns false
                    },
                biometricSessionRestorer =
                    mockk(),
                phoneRegistrationUseCase =
                    phoneRegistrationUseCase,
                tokenProvider =
                    mockk()
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    @Test
    fun `successful login updates current user clears error and calls success callback`() =
        runTest {

            val user =
                createUser()

            coEvery {
                loginUserUseCase.execute(
                    phone = "79991234567",
                    password = "123456"
                )
            } returns
                    LoginUserResult.Success(
                        user
                    )

            var successCalled = false

            viewModel.loginUser(
                phone = "79991234567",
                password = "123456",
                onSuccess = {
                    successCalled = true
                }
            )

            advanceUntilIdle()

            assertEquals(
                user,
                viewModel.currentUser
            )

            assertNull(
                viewModel.errorMessage.value
            )

            assertTrue(
                successCalled
            )

            coVerify(exactly = 1) {
                loginUserUseCase.execute(
                    phone = "79991234567",
                    password = "123456"
                )
            }
        }

    @Test
    fun `login error exposes error and does not call success callback`() =
        runTest {

            coEvery {
                loginUserUseCase.execute(
                    any(),
                    any()
                )
            } returns
                    LoginUserResult.Error(
                        "Неверный пароль"
                    )

            var successCalled = false

            viewModel.loginUser(
                phone = "79991234567",
                password = "wrong-password",
                onSuccess = {
                    successCalled = true
                }
            )

            advanceUntilIdle()

            assertNull(
                viewModel.currentUser
            )

            assertEquals(
                "Неверный пароль",
                viewModel.errorMessage.value
            )

            assertFalse(
                successCalled
            )
        }

    @Test
    fun `second login while first request is running does not start another request`() =
        runTest {

            val result =
                CompletableDeferred<
                        LoginUserResult
                        >()

            coEvery {
                loginUserUseCase.execute(
                    any(),
                    any()
                )
            } coAnswers {
                result.await()
            }

            viewModel.loginUser(
                phone = "79991234567",
                password = "123456",
                onSuccess = {}
            )

            viewModel.loginUser(
                phone = "79991234567",
                password = "123456",
                onSuccess = {}
            )

            runCurrent()

            coVerify(exactly = 1) {
                loginUserUseCase.execute(
                    any(),
                    any()
                )
            }

            result.complete(
                LoginUserResult.Success(
                    createUser()
                )
            )

            advanceUntilIdle()
        }

    @Test
    fun `login keeps auth loading true while request is running`() =
        runTest {

            val result =
                CompletableDeferred<
                        LoginUserResult
                        >()

            coEvery {
                loginUserUseCase.execute(
                    any(),
                    any()
                )
            } coAnswers {
                result.await()
            }

            assertFalse(
                viewModel.isAuthLoading
            )

            viewModel.loginUser(
                phone = "79991234567",
                password = "123456",
                onSuccess = {}
            )

            assertTrue(
                viewModel.isAuthLoading
            )

            runCurrent()

            assertTrue(
                viewModel.isAuthLoading
            )

            result.complete(
                LoginUserResult.Success(
                    createUser()
                )
            )

            advanceUntilIdle()

            assertFalse(
                viewModel.isAuthLoading
            )
        }

    // ---------------------------------------------------------
    // REGISTRATION
    // ---------------------------------------------------------

    @Test
    fun `successful registration requests phone verification and keeps user logged out`() =
        runTest {

            coEvery {
                registerUserUseCase.execute(
                    name = "Павел",
                    phone = "79991234567",
                    password = "123456",
                    passwordRepeat = "123456"
                )
            } returns
                    RegisterUserResult
                        .VerificationRequired(
                            phone =
                                "79991234567",
                            codeSent =
                                true
                        )

            var callbackPhone:
                    String? = null

            var callbackCodeSent:
                    Boolean? = null

            viewModel.validateAndRegister(
                name = "Павел",
                phone = "79991234567",
                password = "123456",
                passwordRepeat = "123456",
                onVerificationRequired = {
                        phone,
                        codeSent ->

                    callbackPhone = phone
                    callbackCodeSent =
                        codeSent
                }
            )

            advanceUntilIdle()

            assertNull(
                viewModel.currentUser
            )

            assertNull(
                viewModel.errorMessage.value
            )

            assertEquals(
                "79991234567",
                callbackPhone
            )

            assertEquals(
                true,
                callbackCodeSent
            )

            assertFalse(
                viewModel.isAuthLoading
            )

            coVerify(exactly = 1) {
                registerUserUseCase.execute(
                    name = "Павел",
                    phone = "79991234567",
                    password = "123456",
                    passwordRepeat = "123456"
                )
            }
        }

    @Test
    fun `registration error exposes error and does not request phone verification`() =
        runTest {

            coEvery {
                registerUserUseCase.execute(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns
                    RegisterUserResult.Error(
                        "Пароли не совпадают"
                    )

            var verificationRequested =
                false

            viewModel.validateAndRegister(
                name = "Павел",
                phone = "79991234567",
                password = "123456",
                passwordRepeat = "654321",
                onVerificationRequired = {
                        _,
                        _ ->

                    verificationRequested = true
                }
            )

            advanceUntilIdle()

            assertNull(
                viewModel.currentUser
            )

            assertEquals(
                "Пароли не совпадают",
                viewModel.errorMessage.value
            )

            assertFalse(
                verificationRequested
            )
        }

    @Test
    fun `second registration while first request is running does not start another request`() =
        runTest {

            val result =
                CompletableDeferred<
                        RegisterUserResult
                        >()

            coEvery {
                registerUserUseCase.execute(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } coAnswers {
                result.await()
            }

            viewModel.validateAndRegister(
                name = "Павел",
                phone = "79991234567",
                password = "123456",
                passwordRepeat = "123456",
                onVerificationRequired = {
                        _,
                        _ ->
                }
            )

            viewModel.validateAndRegister(
                name = "Павел",
                phone = "79991234567",
                password = "123456",
                passwordRepeat = "123456",
                onVerificationRequired = {
                        _,
                        _ ->
                }
            )

            runCurrent()

            coVerify(exactly = 1) {
                registerUserUseCase.execute(
                    any(),
                    any(),
                    any(),
                    any()
                )
            }

            result.complete(
                RegisterUserResult
                    .VerificationRequired(
                        phone =
                            "79991234567",
                        codeSent =
                            true
                    )
            )

            advanceUntilIdle()

            assertFalse(
                viewModel.isAuthLoading
            )
        }

    // ---------------------------------------------------------
    // PHONE VERIFICATION
    // ---------------------------------------------------------

    @Test
    fun `begin phone verification stores phone and starts resend cooldown`() =
        runTest {

            viewModel.beginPhoneVerification(
                phone = "79991234567",
                codeSent = true
            )

            runCurrent()

            val state =
                viewModel
                    .phoneVerificationState
                    .value

            assertEquals(
                "79991234567",
                state.phone
            )

            assertTrue(
                state.codeSent
            )

            assertFalse(
                state.busy
            )

            assertNull(
                state.error
            )

            assertEquals(
                60,
                state.resendSeconds
            )

            // Завершаем таймер,
            // чтобы не оставлять активную coroutine.
            advanceUntilIdle()
        }

    @Test
    fun `successful phone verification logs user in and calls success callback`() =
        runTest {

            val user =
                createUser()

            viewModel.beginPhoneVerification(
                phone = "79991234567",
                codeSent = false
            )

            coEvery {
                phoneRegistrationUseCase.verify(
                    phone = "79991234567",
                    code = "123456"
                )
            } returns
                    VerifyPhoneResult.Success(
                        user
                    )

            var successCalled = false

            viewModel.verifyRegistrationPhone(
                code = "123456",
                onSuccess = {
                    successCalled = true
                }
            )

            advanceUntilIdle()

            assertEquals(
                user,
                viewModel.currentUser
            )

            assertNull(
                viewModel.errorMessage.value
            )

            assertNull(
                viewModel
                    .phoneVerificationState
                    .value
                    .error
            )

            assertFalse(
                viewModel
                    .phoneVerificationState
                    .value
                    .busy
            )

            assertTrue(
                successCalled
            )

            coVerify(exactly = 1) {
                phoneRegistrationUseCase.verify(
                    phone = "79991234567",
                    code = "123456"
                )
            }
        }

    @Test
    fun `phone verification error keeps user logged out and exposes verification error`() =
        runTest {

            viewModel.beginPhoneVerification(
                phone = "79991234567",
                codeSent = false
            )

            coEvery {
                phoneRegistrationUseCase.verify(
                    phone = "79991234567",
                    code = "111111"
                )
            } returns
                    VerifyPhoneResult.Error(
                        "Неверный или истёкший код"
                    )

            var successCalled = false

            viewModel.verifyRegistrationPhone(
                code = "111111",
                onSuccess = {
                    successCalled = true
                }
            )

            advanceUntilIdle()

            assertNull(
                viewModel.currentUser
            )

            assertEquals(
                "Неверный или истёкший код",
                viewModel
                    .phoneVerificationState
                    .value
                    .error
            )

            assertFalse(
                viewModel
                    .phoneVerificationState
                    .value
                    .busy
            )

            assertFalse(
                successCalled
            )
        }

    @Test
    fun `successful resend marks code sent and starts cooldown`() =
        runTest {

            viewModel.beginPhoneVerification(
                phone = "79991234567",
                codeSent = false
            )

            coEvery {
                phoneRegistrationUseCase.resend(
                    "79991234567"
                )
            } returns
                    ResendPhoneCodeResult.Success

            viewModel
                .resendRegistrationPhoneCode()

            runCurrent()

            val state =
                viewModel
                    .phoneVerificationState
                    .value

            assertEquals(
                "79991234567",
                state.phone
            )

            assertTrue(
                state.codeSent
            )

            assertFalse(
                state.busy
            )

            assertNull(
                state.error
            )

            assertEquals(
                60,
                state.resendSeconds
            )

            coVerify(exactly = 1) {
                phoneRegistrationUseCase.resend(
                    "79991234567"
                )
            }

            // Завершаем cooldown job.
            advanceUntilIdle()
        }

    @Test
    fun `resend error exposes verification error and does not start cooldown`() =
        runTest {

            viewModel.beginPhoneVerification(
                phone = "79991234567",
                codeSent = false
            )

            coEvery {
                phoneRegistrationUseCase.resend(
                    "79991234567"
                )
            } returns
                    ResendPhoneCodeResult.Error(
                        "Не удалось отправить код"
                    )

            viewModel
                .resendRegistrationPhoneCode()

            advanceUntilIdle()

            val state =
                viewModel
                    .phoneVerificationState
                    .value

            assertFalse(
                state.busy
            )

            assertFalse(
                state.codeSent
            )

            assertEquals(
                0,
                state.resendSeconds
            )

            assertEquals(
                "Не удалось отправить код",
                state.error
            )
        }

    // ---------------------------------------------------------
    // SESSION RESTORE
    // ---------------------------------------------------------

    @Test
    fun `restore session success restores current user`() =
        runTest {

            val user =
                createUser()

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser = user
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            assertEquals(
                user,
                viewModel.currentUser
            )

            assertNull(
                viewModel.errorMessage.value
            )
        }

    @Test
    fun `restore session with no user leaves user logged out`() =
        runTest {

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser = null
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            assertNull(
                viewModel.currentUser
            )

            assertNull(
                viewModel.errorMessage.value
            )
        }

    @Test
    fun `restore session error clears user and exposes error`() =
        runTest {

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Error(
                        "Не удалось проверить сессию"
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            assertNull(
                viewModel.currentUser
            )

            assertEquals(
                "Не удалось проверить сессию",
                viewModel.errorMessage.value
            )
        }

    // ---------------------------------------------------------
    // ERROR
    // ---------------------------------------------------------

    @Test
    fun `clear error removes current error message`() =
        runTest {

            coEvery {
                loginUserUseCase.execute(
                    any(),
                    any()
                )
            } returns
                    LoginUserResult.Error(
                        "Ошибка входа"
                    )

            viewModel.loginUser(
                phone = "79991234567",
                password = "123456",
                onSuccess = {}
            )

            advanceUntilIdle()

            assertEquals(
                "Ошибка входа",
                viewModel.errorMessage.value
            )

            viewModel.clearError()

            assertNull(
                viewModel.errorMessage.value
            )
        }

    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------

    @Test
    fun `successful logout clears current user and error`() =
        runTest {

            val user =
                createUser()

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser = user
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            assertEquals(
                user,
                viewModel.currentUser
            )

            coEvery {
                logoutUserUseCase.execute()
            } returns
                    LogoutUserResult.Success

            viewModel.logout()

            advanceUntilIdle()

            assertNull(
                viewModel.currentUser
            )

            assertNull(
                viewModel.errorMessage.value
            )

            coVerify(exactly = 1) {
                logoutUserUseCase.execute()
            }
        }

    @Test
    fun `logout error keeps current user and exposes error`() =
        runTest {

            val user =
                createUser()

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser = user
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            coEvery {
                logoutUserUseCase.execute()
            } returns
                    LogoutUserResult.Error(
                        "Не удалось выйти"
                    )

            viewModel.logout()

            advanceUntilIdle()

            assertEquals(
                user,
                viewModel.currentUser
            )

            assertEquals(
                "Не удалось выйти",
                viewModel.errorMessage.value
            )
        }

    // ---------------------------------------------------------
    // NAME
    // ---------------------------------------------------------

    @Test
    fun `successful name update replaces current user and exposes success status`() =
        runTest {

            val originalUser =
                createUser()

            val updatedUser =
                User(
                    id =
                        originalUser.id,
                    name =
                        "Новое имя",
                    phone =
                        originalUser.phone,
                    address =
                        originalUser.address
                )

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser =
                            originalUser
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            coEvery {
                updateUserNameUseCase.execute(
                    user =
                        originalUser,
                    name =
                        "Новое имя"
                )
            } returns
                    UpdateUserNameResult.Success(
                        updatedUser
                    )

            viewModel.updateName(
                "Новое имя"
            )

            advanceUntilIdle()

            assertEquals(
                updatedUser,
                viewModel.currentUser
            )

            assertEquals(
                ProfileActionStatus.Success(
                    action =
                        ProfileActionType.NAME,
                    message =
                        "Имя сохранено"
                ),
                viewModel
                    .profileActionStatus
                    .value
            )

            coVerify(exactly = 1) {
                updateUserNameUseCase.execute(
                    user =
                        originalUser,
                    name =
                        "Новое имя"
                )
            }
        }

    @Test
    fun `name update error keeps current user and exposes profile error`() =
        runTest {

            val originalUser =
                createUser()

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser =
                            originalUser
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            coEvery {
                updateUserNameUseCase.execute(
                    user =
                        originalUser,
                    name =
                        "Новое имя"
                )
            } returns
                    UpdateUserNameResult.Error(
                        "Не удалось сохранить имя"
                    )

            viewModel.updateName(
                "Новое имя"
            )

            advanceUntilIdle()

            assertEquals(
                originalUser,
                viewModel.currentUser
            )

            assertEquals(
                ProfileActionStatus.Error(
                    "Не удалось сохранить имя"
                ),
                viewModel
                    .profileActionStatus
                    .value
            )
        }

    // ---------------------------------------------------------
    // ADDRESS
    // ---------------------------------------------------------

    @Test
    fun `successful address update replaces current user and exposes success status`() =
        runTest {

            val originalUser =
                createUser()

            val updatedUser =
                User(
                    id =
                        originalUser.id,
                    name =
                        originalUser.name,
                    phone =
                        originalUser.phone,
                    address =
                        "Новый адрес"
                )

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser =
                            originalUser
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            coEvery {
                updateUserAddressUseCase.execute(
                    user =
                        originalUser,
                    address =
                        "Новый адрес"
                )
            } returns
                    UpdateUserAddressResult.Success(
                        updatedUser
                    )

            viewModel.updateAddress(
                "Новый адрес"
            )

            advanceUntilIdle()

            assertEquals(
                updatedUser,
                viewModel.currentUser
            )

            assertEquals(
                ProfileActionStatus.Success(
                    action =
                        ProfileActionType.ADDRESS,
                    message =
                        "Адрес сохранён"
                ),
                viewModel
                    .profileActionStatus
                    .value
            )

            coVerify(exactly = 1) {
                updateUserAddressUseCase.execute(
                    user =
                        originalUser,
                    address =
                        "Новый адрес"
                )
            }
        }

    @Test
    fun `address update error keeps current user and exposes profile error`() =
        runTest {

            val originalUser =
                createUser()

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser =
                            originalUser
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            coEvery {
                updateUserAddressUseCase.execute(
                    user =
                        originalUser,
                    address =
                        "Новый адрес"
                )
            } returns
                    UpdateUserAddressResult.Error(
                        "Не удалось сохранить адрес"
                    )

            viewModel.updateAddress(
                "Новый адрес"
            )

            advanceUntilIdle()

            assertEquals(
                originalUser,
                viewModel.currentUser
            )

            assertEquals(
                ProfileActionStatus.Error(
                    "Не удалось сохранить адрес"
                ),
                viewModel
                    .profileActionStatus
                    .value
            )
        }

    @Test
    fun `clear profile action status removes current status`() =
        runTest {

            val user =
                createUser()

            coEvery {
                restoreSessionUseCase.execute()
            } returns
                    RestoreSessionResult.Success(
                        currentUser = user
                    )

            viewModel.restoreUser()

            advanceUntilIdle()

            coEvery {
                updateUserNameUseCase.execute(
                    user = user,
                    name = "Новое имя"
                )
            } returns
                    UpdateUserNameResult.Error(
                        "Ошибка сохранения"
                    )

            viewModel.updateName(
                "Новое имя"
            )

            advanceUntilIdle()

            assertEquals(
                ProfileActionStatus.Error(
                    "Ошибка сохранения"
                ),
                viewModel
                    .profileActionStatus
                    .value
            )

            viewModel
                .clearProfileActionStatus()

            assertNull(
                viewModel
                    .profileActionStatus
                    .value
            )
        }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private fun createUser(): User {

        return User(
            id = 42,
            name = "Павел",
            phone = "79991234567",
            address = "Улица 1"
        )
    }
}