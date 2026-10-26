package com.example.catering.navigation

import com.example.catering.presentation.localization.uiMessage
import com.example.catering.R

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.catering.data.local.biometric.BiometricAuthenticator
import com.example.catering.presentation.cart.CartViewModel
import com.example.catering.presentation.checkout.CheckoutViewModel
import com.example.catering.presentation.favorite.FavoriteViewModel
import com.example.catering.presentation.menu.MenuViewModel
import com.example.catering.presentation.order.OrderViewModel
import com.example.catering.presentation.recently.RecentlyViewedViewModel
import com.example.catering.presentation.user.PasswordRecoveryViewModel
import com.example.catering.presentation.user.ProfileActionStatus
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.components.state.LoadingStateContent
import com.example.catering.ui.routes.AddressRoute
import com.example.catering.ui.routes.BiometricUnlockRoute
import com.example.catering.ui.routes.CartRoute
import com.example.catering.ui.routes.ChangePasswordRoute
import com.example.catering.ui.routes.CheckoutRoute
import com.example.catering.ui.routes.ContactsRoute
import com.example.catering.ui.routes.DishDetailRoute
import com.example.catering.ui.routes.EmailVerificationRoute
import com.example.catering.ui.routes.FavoritesRoute
import com.example.catering.ui.routes.ForgotPasswordRoute
import com.example.catering.ui.routes.HomeRoute
import com.example.catering.ui.routes.LoginRoute
import com.example.catering.ui.routes.MenuRoute
import com.example.catering.ui.routes.OrderSuccessRoute
import com.example.catering.ui.routes.OrdersHistoryRoute
import com.example.catering.ui.routes.PersonalDataRoute
import com.example.catering.ui.routes.ProfileRoute
import com.example.catering.ui.routes.RecentlyViewedRoute
import com.example.catering.ui.routes.RegisterRoute
import com.example.catering.ui.routes.ResetPasswordRoute
import com.example.catering.ui.routes.SupportRoute
import com.example.catering.ui.screens.SecurityScreen
import com.example.catering.ui.util.openDialer
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException


@Serializable
private data object SecurityDestination

@Serializable
private data object ChangePasswordDestination

@Serializable
private data object PersonalDataDestination

@Serializable
private data object AddressDestination

@Serializable
private data object PasswordRecoveryGraph

@Serializable
private data object ForgotPasswordDestination

@Serializable
private data object ResetPasswordDestination

@Serializable
private data object EmailVerificationDestination

@Composable
fun AppNavHost(
    navController: NavHostController,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel,
    favoriteViewModel: FavoriteViewModel,
    recentlyViewedViewModel: RecentlyViewedViewModel,
    checkoutViewModel: CheckoutViewModel,
    context: Context
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var afterAuthDestination by remember { mutableStateOf<Route>(Home) }
    var authBackDestination by remember { mutableStateOf<Route>(Home) }
    var pendingBiometricPasswordLogin by remember { mutableStateOf(false) }
    val biometricUpgradeScope = rememberCoroutineScope()
    var showBiometricUpgradeOffer by remember {
        mutableStateOf(false)
    }
    var pendingAuthNavigation by remember {
        mutableStateOf<Triple<Route, Route, Boolean>?>(null)
    }
    val snackbarHostState = remember { SnackbarHostState() } // пульт управления сообщениями

    val biometricActivityContext = LocalContext.current

    val menuViewModel: MenuViewModel = hiltViewModel()
    val badgeCartCount by cartViewModel.totalItemsCount.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        cartViewModel.navigateToCartEvent.collect {
            navController.navigate(Cart)
        }
    }

    LaunchedEffect(Unit) {
        cartViewModel.cartMessageEvent.collect { message ->
            snackbarHostState.showSnackbar(texts.uiMessage(message))
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.restoreUser()
    }


    LaunchedEffect(
        pendingBiometricPasswordLogin,
        userViewModel.isSessionRestoring,
        userViewModel.isBiometricUnlockRequired
    ) {
        if (
            pendingBiometricPasswordLogin &&
            !userViewModel.isSessionRestoring &&
            !userViewModel.isBiometricUnlockRequired
        ) {
            pendingBiometricPasswordLogin = false

            afterAuthDestination = Home
            authBackDestination = Home

            navController.navigate(Login) {
                popUpTo(Home) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }


    fun navigateFromAuth() {
        navController.navigate(afterAuthDestination) {
            popUpTo(authBackDestination) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun navigateWithHomeBackStack(destination: Any) {
        navController.navigate(destination) {
            popUpTo(Home) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    fun openLoginAfter(destination: Route, backDestination: Route) {
        afterAuthDestination = destination
        authBackDestination = backDestination
        navController.navigate(Login)
    }

    fun openRegisterAfter(destination: Route, backDestination: Route) {
        afterAuthDestination = destination
        authBackDestination = backDestination
        navController.navigate(Register)
    }

    fun navigateTo(destination: Any) {
        navController.navigate(destination) {
            launchSingleTop = true
        }
    }


    fun navigateWithAuth(
        destination: Route,
        backDestination: Route,
        useHomeBackStack: Boolean = false
    ) {
        // Если аккаунт ещё проверяется, запоминаем переход
        if (userViewModel.isSessionRestoring) {
            pendingAuthNavigation = Triple(
                destination,
                backDestination,
                useHomeBackStack
            )
            return
        }

        // Проверка завершена — решаем, куда перейти
        if (userViewModel.currentUser != null) {
            if (useHomeBackStack) {
                navigateWithHomeBackStack(destination)
            } else {
                navigateTo(destination)
            }
        } else {
            openLoginAfter(
                destination = destination,
                backDestination = backDestination
            )
        }
    }


    LaunchedEffect(
        userViewModel.isSessionRestoring,
        pendingAuthNavigation
    ) {
        if (userViewModel.isSessionRestoring) {
            return@LaunchedEffect
        }

        val request = pendingAuthNavigation
            ?: return@LaunchedEffect

        pendingAuthNavigation = null

        val (destination, backDestination, useHomeBackStack) = request

        if (userViewModel.currentUser != null) {
            if (useHomeBackStack) {
                navigateWithHomeBackStack(destination)
            } else {
                navigateTo(destination)
            }
        } else {
            openLoginAfter(
                destination = destination,
                backDestination = backDestination
            )
        }
    }



    fun navigateBack() {
        navController.popBackStack()
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val currentRoute = currentDestination?.route

    val showBottomBar =
        currentRoute == Home::class.qualifiedName ||
                currentRoute == Menu::class.qualifiedName ||
                currentRoute == OrdersHistory::class.qualifiedName ||
                currentRoute == Favorites::class.qualifiedName ||
                currentRoute == Profile::class.qualifiedName


    when {

        userViewModel.isSessionRestoring -> {
            LoadingStateContent(
                message = texts.getString(R.string.ui_checking_your_account_33)
            )
        }

        userViewModel.isBiometricUnlockRequired -> {

            BiometricUnlockRoute(
                userViewModel = userViewModel,

                prepareUnlockCipher = {
                    userViewModel.prepareBiometricUnlockCipher()
                },

                getProtectedSessionVersion = {
                    userViewModel.getProtectedBiometricSessionVersion()
                },

                onUnlocked = {
                    biometricUpgradeScope.launch {
                        try {
                            if (userViewModel.isBiometricMigrationNeeded()) {
                                showBiometricUpgradeOffer = true
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(
                                texts.getString(R.string.ui_could_not_check_the_security_version_34)
                            )
                        }
                    }
                },

                onPasswordLogin = {
                    pendingBiometricPasswordLogin = true
                }
            )

        }

        else -> {

            Scaffold( // это каркас экрана.
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },

                bottomBar = {
                    if (showBottomBar) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 8.dp,
                                    bottom = 0.dp
                                )
                                .heightIn(min = 72.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF10191F))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF394955),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SiestaBottomTab(
                                title = texts.getString(R.string.ui_home_35),
                                iconType = 0,
                                selected = currentRoute == Home::class.qualifiedName,
                                modifier = Modifier.weight(1f),
                                onClick = { navigateWithHomeBackStack(Home) }
                            )

                            SiestaBottomTab(
                                title = texts.getString(R.string.ui_menu_36),
                                iconType = 1,
                                selected = currentRoute == Menu::class.qualifiedName,
                                modifier = Modifier.weight(1f),
                                onClick = { navigateWithHomeBackStack(Menu) }
                            )

                            SiestaBottomTab(
                                title = texts.getString(R.string.ui_orders_37),
                                iconType = 2,
                                selected = currentRoute == OrdersHistory::class.qualifiedName,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    navigateWithAuth(
                                        destination = OrdersHistory,
                                        backDestination = Home,
                                        useHomeBackStack = true
                                    )
                                }
                            )

                            SiestaBottomTab(
                                title = texts.getString(R.string.ui_favorites_38),
                                iconType = 3,
                                selected = currentRoute == Favorites::class.qualifiedName,
                                modifier = Modifier.weight(1f),
                                onClick = { navigateWithHomeBackStack(Favorites) }
                            )

                            SiestaBottomTab(
                                title = texts.getString(R.string.ui_profile_39),
                                iconType = 4,
                                selected = currentRoute == Profile::class.qualifiedName,
                                modifier = Modifier.weight(1f),
                                onClick = { navigateWithHomeBackStack(Profile) }
                            )
                        }
                    }
                }
            )
            { innerPadding -> // Scaffold, рассчитай мне внутренние отступы, которые нужны для основного контента.
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
                    NavHost(
                        navController = navController,
                        startDestination = Home,
                        modifier = Modifier.widthIn(max = 840.dp).fillMaxSize()
                    ) {

                        composable<Home> {
                            HomeRoute(
                                menuViewModel = menuViewModel,
                                onGoMenu = { navigateTo(Menu) },
                                cartViewModel = cartViewModel,
                                userViewModel = userViewModel,
                                onGoToCart = { navigateTo(Cart) },
                                onExitApp = {
                                    (context as? Activity)?.finish()
                                },
                                onGoViewedDish = {
                                    navigateTo(RecentlyViewed)
                                },
                                onOpenRecentlyViewedDish = { dishId ->
                                    navigateTo(DishDetail(dishId))
                                },
                                onAddRecentlyViewedToCart = { dish ->
                                    cartViewModel.addToCart(dish.id, 1)
                                }
                            )
                        }


                        composable<Cart> {
                            CartRoute(
                                cartViewModel = cartViewModel,
                                onBack = { navigateBack() },
                                onCheckout = {
                                    navigateWithAuth(destination = Checkout, backDestination = Cart)
                                },
                                onGoMenu = { navigateTo(Menu) },
                                favoriteViewModel = favoriteViewModel
                            )
                        }

                        composable<DishDetail> { backStackEntry ->
                            val route = backStackEntry.toRoute<DishDetail>()

                            DishDetailRoute(
                                favoriteViewModel = favoriteViewModel,
                                dishId = route.dishId,
                                onBack = { navigateBack() },
                                cartViewModel = cartViewModel,
                                recentlyViewedViewModel = recentlyViewedViewModel,
                                onGoToCart = { navigateTo(Cart) },
                                badgeCartCount = badgeCartCount
                            )
                        }

                        composable<Menu> {

                            MenuRoute(
                                onOpenDish = { dishId -> navigateTo(DishDetail(dishId)) },
                                onBack = { navigateBack() },
                                onGoCart = { navigateTo(Cart) },
                                onGoSupport = { navigateTo(Support) },
                                favoriteViewModel = favoriteViewModel,
                                menuViewModel = menuViewModel,
                                badgeCartCount = badgeCartCount,
                                onAddToCart = { dishId ->
                                    cartViewModel.addToCart(dishId, 1)
                                }

                            )
                        }

                        composable<Checkout> {
                            CheckoutRoute(
                                cartViewModel = cartViewModel,
                                orderViewModel = orderViewModel,
                                userViewModel = userViewModel,
                                checkoutViewModel = checkoutViewModel,
                                onBack = { navigateBack() },
                                onGoMenu = { navigateTo(Menu) },
                                onGoLogin = {
                                    openLoginAfter(
                                        destination = Checkout,
                                        backDestination = Checkout
                                    )
                                },
                                onOrderSuccess = {
                                    navigateWithHomeBackStack(OrderSuccess)
                                }
                            )
                        }

                        composable<OrderSuccess> {
                            OrderSuccessRoute(
                                onGoHome = { navigateWithHomeBackStack(Home) },
                                onGoMenu = {
                                    navigateWithHomeBackStack(Menu)
                                },
                                onGoToOrdersHistory = {
                                    navigateWithHomeBackStack(OrdersHistory)
                                }
                            )
                        }

                        composable<OrdersHistory> {
                            OrdersHistoryRoute(
                                onGoCart = { navigateTo(Cart) },
                                orderViewModel = orderViewModel,
                                cartViewModel = cartViewModel,
                                onBack = { navigateBack() },
                                onGoMenu = {
                                    navigateWithHomeBackStack(Menu)
                                },
                            )
                        }

                        composable<Contacts> {
                            ContactsRoute(
                                onBack = { navigateBack() },
                                onCallClick = {
                                    openDialer(context, "+70000000000")
                                }
                            )
                        }

                        composable<Support> {
                            SupportRoute(
                                onBack = { navigateBack() },
                                onCallSupportClick = {
                                    openDialer(context, "+79285557773")
                                }
                            )
                        }

                        composable<Register> {
                            RegisterRoute(
                                userViewModel = userViewModel,
                                onBack = {
                                    navigateBack()
                                },
                                onGoLogin = {
                                    if (!navController.popBackStack(Login, false)) {
                                        navController.navigate(Login) { popUpTo<Register> { inclusive = true }; launchSingleTop = true }
                                    }
                                },
                                onRegisterSuccess = { navigateFromAuth() }
                            )
                        }

                        composable<Login> {
                            LoginRoute(
                                onBack = { navigateBack() },
                                userViewModel = userViewModel,
                                noAccount = { navigateTo(Register) },
                                onLoginSuccess = { navigateFromAuth() },
                                onForgotPassword = {
                                    navController.navigate(PasswordRecoveryGraph)
                                }
                            )
                        }


                        navigation<PasswordRecoveryGraph>(
                            startDestination = ForgotPasswordDestination
                        ) {

                            composable<ForgotPasswordDestination>
                            { entry ->

                                val parentEntry = remember(entry) {
                                    navController.getBackStackEntry<PasswordRecoveryGraph>()
                                }

                                val recoveryViewModel: PasswordRecoveryViewModel =
                                    hiltViewModel(parentEntry)

                                ForgotPasswordRoute(
                                    viewModel = recoveryViewModel,
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onCodeRequested = {
                                        navController.navigate(ResetPasswordDestination) {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            composable<ResetPasswordDestination>
                            { entry ->

                                val parentEntry = remember(entry) {
                                    navController.getBackStackEntry<PasswordRecoveryGraph>()
                                }

                                val recoveryViewModel: PasswordRecoveryViewModel =
                                    hiltViewModel(parentEntry)

                                ResetPasswordRoute(
                                    viewModel = recoveryViewModel,
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onGoLogin = {
                                        navController.popBackStack(
                                            route = Login,
                                            inclusive = false
                                        )
                                    }
                                )
                            }
                        }


                        composable<Profile> {
                            ProfileRoute(
                                onGoCart = { navigateTo(Cart) },
                                badgeCartCount = badgeCartCount,
                                onBack = {
                                    navigateBack()
                                },
                                userViewModel = userViewModel,
                                onGoLogin = {
                                    openLoginAfter(
                                        destination = Profile,
                                        backDestination = Profile
                                    )
                                },
                                onGoRegister = {
                                    openLoginAfter(destination = Profile, backDestination = Profile)
                                    navigateTo(Register)
                                },
                                onLogout = {
                                    navigateWithHomeBackStack(Home)
                                },
                                onGoContacts = {
                                    navigateTo(Contacts)
                                },
                                onGoSupport = {
                                    navigateTo(Support)
                                },
                                onGoSecurity = {
                                    if (userViewModel.currentUser != null) {
                                        navigateTo(SecurityDestination)
                                    }
                                },
                                onGoPersonalData = {
                                    if (userViewModel.currentUser != null) {
                                        navigateTo(PersonalDataDestination)
                                    }
                                },
                                onGoAddress = {
                                    if (userViewModel.currentUser != null) {
                                        navigateTo(AddressDestination)
                                    }
                                }
                            )
                        }


                        composable<PersonalDataDestination> {
                            PersonalDataRoute(
                                userViewModel = userViewModel,
                                onBack = {
                                    navigateBack()
                                }
                            )
                        }

                        composable<AddressDestination> {
                            AddressRoute(
                                userViewModel = userViewModel,
                                onBack = {
                                    navigateBack()
                                }
                            )
                        }

                        composable<SecurityDestination> {
                            val biometricContext = LocalContext.current
                            val scope = rememberCoroutineScope()

                            var isPromptActive by remember {
                                mutableStateOf(false)
                            }

                            var showDisableDialog by remember {
                                mutableStateOf(false)
                            }

                            LaunchedEffect(Unit) {
                                userViewModel.refreshBiometricEnabled()
                            }

                            val profileStatus by userViewModel.profileActionStatus
                                .collectAsStateWithLifecycle()

                            LaunchedEffect(profileStatus) {
                                val error = profileStatus as? ProfileActionStatus.Error
                                if (error != null) {
                                    userViewModel.clearProfileActionStatus()
                                    snackbarHostState.showSnackbar(texts.uiMessage(error.message))
                                }
                            }

                            val biometricBusy =
                                isPromptActive ||
                                        userViewModel.isBiometricSetupBusy ||
                                        userViewModel.isLoggingOut

                            SecurityScreen(
                                user = userViewModel.currentUser,

                                onBack = {
                                    if (!biometricBusy) navigateBack()
                                },

                                onChangePassword = {
                                    navigateTo(ChangePasswordDestination)
                                },

                                onLogout = {
                                    if (!biometricBusy) {
                                        userViewModel.logout(
                                            onSuccess = {
                                                navigateWithHomeBackStack(Home)
                                            }
                                        )
                                    }
                                },

                                isLoggingOut = biometricBusy,
                                biometricEnabled = userViewModel.isBiometricEnabled,
                                isBiometricBusy = biometricBusy,
                                onEmailClick = {
                                    navigateTo(EmailVerificationDestination)
                                },
                                onBiometricClick = {
                                    if (!biometricBusy) {

                                        if (userViewModel.isBiometricEnabled) {
                                            showDisableDialog = true

                                        } else {
                                            isPromptActive = true

                                            scope.launch {
                                                try {
                                                    val activity =
                                                        biometricContext.findBiometricActivity()

                                                    checkNotNull(activity) {
                                                        "Не найдена FragmentActivity"
                                                    }

                                                    val cipher =
                                                        userViewModel.prepareBiometricEnableCipher()

                                                    BiometricAuthenticator(activity).authenticate(
                                                        cipher = cipher,
                                                        title = texts.getString(R.string.ui_siesta_quick_sign_in_41),
                                                        subtitle = texts.getString(R.string.ui_use_your_fingerprint_to_enable_protection_42),

                                                        onSuccess = { verifiedCipher ->
                                                            isPromptActive = false

                                                            userViewModel.completeBiometricEnable(
                                                                verifiedCipher
                                                            )
                                                        },

                                                        onCancelled = {
                                                            isPromptActive = false
                                                        },

                                                        onError = { message ->
                                                            isPromptActive = false

                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(texts.uiMessage(message))
                                                            }
                                                        }
                                                    )

                                                } catch (e: CancellationException) {
                                                    isPromptActive = false
                                                    throw e

                                                } catch (e: Exception) {
                                                    isPromptActive = false

                                                    snackbarHostState.showSnackbar(
                                                        texts.getString(R.string.ui_could_not_prepare_biometric_verification_43)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            )

                            if (showDisableDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        showDisableDialog = false
                                    },

                                    title = {
                                        Text(texts.getString(R.string.ui_turn_off_fingerprint_sign_in_44))
                                    },

                                    text = {
                                        Text(
                                            texts.getString(R.string.ui_your_secure_session_will_be_removed_45) +
                                                    texts.getString(R.string.ui_to_continue_you_will_need_to_46) +
                                                    texts.getString(R.string.ui_sign_in_with_your_password_again_47)
                                        )
                                    },

                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                showDisableDialog = false

                                                userViewModel.logout(
                                                    onSuccess = {
                                                        afterAuthDestination = Home
                                                        authBackDestination = Home

                                                        navController.navigate(Login) {
                                                            popUpTo(Home) {
                                                                inclusive = false
                                                            }
                                                            launchSingleTop = true
                                                        }
                                                    }
                                                )
                                            }
                                        ) {
                                            Text(texts.getString(R.string.ui_turn_off_and_sign_out_48))
                                        }
                                    },

                                    dismissButton = {
                                        TextButton(
                                            onClick = {
                                                showDisableDialog = false
                                            }
                                        ) {
                                            Text(texts.getString(R.string.ui_cancel_49))
                                        }
                                    }
                                )
                            }
                        }


                        composable<ChangePasswordDestination> {
                            ChangePasswordRoute(
                                userViewModel = userViewModel,
                                onBack = {
                                    navigateBack()
                                },
                                onSessionCleared = {
                                    // После смены пароля повторно
                                    // авторизуем пользователя.
                                    afterAuthDestination = Home
                                    authBackDestination = Home

                                    // Убираем защищённые экраны
                                    // из истории навигации.
                                    navController.navigate(Login) {
                                        popUpTo(Home) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable<EmailVerificationDestination> {
                            if (userViewModel.currentUser == null) {
                                LaunchedEffect(Unit) {
                                    navController.popBackStack()
                                }
                            } else {
                                EmailVerificationRoute(
                                    onBack = { navigateBack() }
                                )
                            }
                        }

                        composable<Favorites> {
                            FavoritesRoute(
                                badgeCartCount = badgeCartCount,
                                favoriteViewModel = favoriteViewModel,
                                onOpenDish = { navigateTo(DishDetail(it)) },
                                onAddToCart = { cartViewModel.addToCart(it, 1) },
                                onGoCart = { navigateTo(Cart) },
                                onBack = { navigateBack() },
                                onGoMenu = { navigateTo(Menu) })
                        }

                        composable<RecentlyViewed> {
                            RecentlyViewedRoute(
                                onGoCart = { navigateTo(Cart) },
                                badgeCartCount = badgeCartCount,
                                recentlyViewedViewModel = recentlyViewedViewModel,
                                favoriteViewModel = favoriteViewModel,
                                onGoMenu = { navigateTo(Menu) },
                                onOpenDish = { navigateTo(DishDetail(it)) },
                                onAddToCart = { cartViewModel.addToCart(it, 1) },
                                onBack = { navigateBack() })
                        }
                    }
                }
            }
        }
    }
    if (
        showBiometricUpgradeOffer &&
        !userViewModel.isBiometricUnlockRequired
    ) {
        AlertDialog(
            onDismissRequest = {
                showBiometricUpgradeOffer = false
            },

            title = {
                Text(texts.getString(R.string.ui_easy_sign_in_to_siesta_50))
            },

            text = {
                Text(
                    texts.getString(R.string.ui_you_can_now_unlock_siesta_51) +
                            texts.getString(R.string.ui_with_your_fingerprint_or_phone_pin_n_n_52) +
                            texts.getString(R.string.ui_you_will_stay_signed_in_to_your_account_53) +
                            texts.getString(R.string.ui_you_will_not_need_to_enter_your_phone_number_and_password_54)
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showBiometricUpgradeOffer = false

                        biometricUpgradeScope.launch {
                            try {
                                val activity =
                                    biometricActivityContext.findBiometricActivity()
                                        ?: error("Не найдена FragmentActivity")

                                val cipher =
                                    userViewModel
                                        .prepareBiometricMigrationCipher()

                                BiometricAuthenticator(activity)
                                    .authenticate(
                                        cipher = cipher,
                                        title = texts.getString(R.string.ui_siesta_security_update_55),
                                        subtitle = texts.getString(R.string.ui_confirm_with_your_fingerprint_or_pin_56),

                                        allowDeviceCredential = true,

                                        onSuccess = { verifiedCipher ->
                                            userViewModel
                                                .completeBiometricMigration(
                                                    verifiedCipher
                                                )
                                        },

                                        onCancelled = {
                                            // Старая защита V1 сохраняется.
                                        },

                                        onError = { message ->
                                            biometricUpgradeScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message
                                                )
                                            }
                                        }
                                    )

                            } catch (e: CancellationException) {
                                throw e

                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    texts.getString(R.string.ui_could_not_start_the_security_update_57)
                                )
                            }
                        }
                    }
                ) {
                    Text(texts.getString(R.string.ui_update_security_58))
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showBiometricUpgradeOffer = false
                    }
                ) {
                    Text(texts.getString(R.string.ui_later_59))
                }
            }
        )
    }

}

private fun Context.findBiometricActivity(): FragmentActivity? {
    var current: Context = this

    while (true) {
        if (current is FragmentActivity) {
            return current
        }

        val base = (current as? ContextWrapper)?.baseContext
            ?: return null

        if (base === current) return null

        current = base
    }
}
@Composable
private fun SiestaBottomTab(
    title: String,
    iconType: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = Color(0xFFFFB326)
    val inactive = Color(0xFF9CA6AE)
    val contentColor = if (selected) accent else inactive

    Column(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.00f to accent.copy(alpha = 0.90f),
                                    0.35f to accent.copy(alpha = 0.50f),
                                    0.70f to accent.copy(alpha = 0.30f),
                                    1.00f to Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
            SiestaBottomIcon(
                type = iconType,
                color = contentColor
            )
        }


        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = title,
            color = contentColor,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SiestaBottomIcon(
    type: Int,
    color: Color
) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(
            width = 1.8f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        withTransform({
            scale(
                scaleX = size.width / 24f,
                scaleY = size.height / 24f,
                pivot = Offset.Zero
            )
        }) {
            when (type) {
                0 -> {
                    // Главная
                    val path = Path().apply {
                        moveTo(3f, 10f)
                        lineTo(12f, 3f)
                        lineTo(21f, 10f)
                        lineTo(21f, 21f)
                        lineTo(15f, 21f)
                        lineTo(15f, 14f)
                        lineTo(9f, 14f)
                        lineTo(9f, 21f)
                        lineTo(3f, 21f)
                        close()
                    }
                    drawPath(path, color, style = stroke)
                }

                1 -> {
                    // Меню: вилка и нож
                    val path = Path().apply {
                        moveTo(4f, 3f)
                        lineTo(4f, 9f)
                        quadraticBezierTo(4f, 12f, 7f, 12f)
                        quadraticBezierTo(10f, 12f, 10f, 9f)
                        lineTo(10f, 3f)

                        moveTo(7f, 3f)
                        lineTo(7f, 21f)

                        moveTo(18f, 3f)
                        quadraticBezierTo(15f, 8f, 16f, 13f)
                        lineTo(19f, 13f)
                        lineTo(19f, 21f)
                    }
                    drawPath(path, color, style = stroke)
                }

                2 -> {
                    // Заказы: чек
                    val path = Path().apply {
                        moveTo(6f, 3f)
                        lineTo(18f, 3f)
                        lineTo(18f, 21f)
                        lineTo(15f, 19f)
                        lineTo(12f, 21f)
                        lineTo(9f, 19f)
                        lineTo(6f, 21f)
                        close()

                        moveTo(9f, 9f)
                        lineTo(15f, 9f)
                        moveTo(9f, 13f)
                        lineTo(15f, 13f)
                    }
                    drawPath(path, color, style = stroke)
                }

                3 -> {
                    // Избранное: сердце
                    val path = Path().apply {
                        moveTo(12f, 21f)
                        cubicTo(5f, 16f, 2f, 12f, 3f, 7f)
                        cubicTo(4f, 3f, 9f, 2f, 12f, 6f)
                        cubicTo(15f, 2f, 20f, 3f, 21f, 7f)
                        cubicTo(22f, 12f, 19f, 16f, 12f, 21f)
                        close()
                    }
                    drawPath(path, color, style = stroke)
                }

                4 -> {
                    // Профиль
                    drawCircle(
                        color = color,
                        radius = 4f,
                        center = Offset(12f, 8f),
                        style = stroke
                    )

                    val path = Path().apply {
                        moveTo(4f, 21f)
                        lineTo(4f, 19f)
                        cubicTo(4f, 15f, 8f, 13f, 12f, 13f)
                        cubicTo(16f, 13f, 20f, 15f, 20f, 19f)
                        lineTo(20f, 21f)
                    }
                    drawPath(path, color, style = stroke)
                }
            }
        }
    }
}
