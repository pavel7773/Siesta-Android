package com.example.catering.ui.screens


import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardBottom
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight
import com.example.catering.ui.theme.SiestaCtaInsetBottom
import com.example.catering.ui.theme.SiestaCtaInsetTop
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.R
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.theme.SiestaManrope

private val SuccessGold = SiestaAmber
private val SuccessCream = SiestaTextPrimary
private val SuccessText = SiestaTextPrimary
private val SuccessMuted = SiestaNavUnselected
private val SuccessDisplayFont = FontFamily(Font(R.font.cormorant_garamond_semibold))

@Composable
fun OrderSuccessScreen(
    onGoMenu: () -> Unit,
    onGoToOrdersHistory: () -> Unit,
    onGoHome: () -> Unit = onGoMenu
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    BackHandler(onBack = onGoHome)
    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaBgTop,
                        SiestaBgMiddle,
                        SiestaBgBottom
                    )
                )
            )
    ) {
        ScreenHeader(
            title = texts.getString(R.string.ui_order_404),
            onBack = onGoHome,
            compact = true,
            centerTitle = true,
            titleFontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        // Only the content scrolls on short displays / larger accessibility fonts.
        // The two actions remain outside this viewport and cannot cover the content.
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val artworkHeight = (maxHeight - 278.dp).coerceIn(220.dp, 350.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painterResource(R.drawable.siesta_order_success_cloche),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 0.dp)
                        .height(artworkHeight)
                )

                Text(
                    texts.getString(R.string.ui_order_accepted_405),
                    modifier = Modifier.offset(y = (-40).dp),
                    fontFamily = SuccessDisplayFont,
                    fontSize = 62.sp, lineHeight = 52.sp, color = SuccessCream,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    texts.getString(R.string.ui_thank_you_for_your_order_406),
                    modifier = Modifier.offset(y = (-42).dp),
                    fontFamily = SiestaManrope,
                    fontSize = 25.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium,
                    color = SuccessText, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    texts.getString(R.string.ui_we_have_received_your_order_nand_are_preparing_it_for_you_407),
                    modifier = Modifier.offset(y = (-42).dp),
                    fontFamily = SiestaManrope, fontSize = 18.sp, lineHeight = 21.sp,
                    color = SuccessMuted, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    SiestaNavOutline.copy(alpha = 51 / 255f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Spacer(Modifier.height(4.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .offset(y = (-32).dp), horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuccessFeature(
                        R.drawable.siesta_success_chef,
                        texts.getString(R.string.ui_prepared_nwith_care_408),
                        Modifier.weight(1f)
                    )
                    SuccessFeature(
                        R.drawable.siesta_success_leaf,
                        texts.getString(R.string.ui_only_nfresh_ingredients_409),
                        Modifier.weight(1.2f)
                    )
                    SuccessFeature(
                        R.drawable.siesta_success_heart,
                        texts.getString(R.string.ui_thank_you_nfor_choosing_us_410),
                        Modifier.weight(1.2f)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SuccessGoldButton(onGoToOrdersHistory)
            val shape = RoundedCornerShape(28.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                SiestaCardBottom,
                                SiestaCardBottom
                            )
                        )
                    )
                    .border(1.dp, SiestaNavOutline, shape)
                    .clickable(role = Role.Button, onClick = onGoMenu)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    texts.getString(R.string.ui_back_to_menu_411),
                    fontFamily = SiestaManrope,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessCream,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SuccessFeature(icon: Int, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = SuccessGold,
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.height(9.dp))
        Text(
            label, fontFamily = SiestaManrope, fontSize = 12.sp, lineHeight = 17.sp,
            color = SiestaTextPrimary, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessGoldButton(onClick: () -> Unit) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val shape = RoundedCornerShape(30.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(
                14.dp,
                shape,
                ambientColor = SuccessGold.copy(alpha = .22f),
                spotColor = SuccessGold.copy(alpha = .26f)
            )
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        SiestaCtaGoldLight,
                        SuccessGold,
                        SiestaCtaGoldDark
                    )
                )
            )
            .border(1.dp, SiestaCtaGoldLight.copy(alpha = 170 / 255f), shape)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 58.dp)
            .padding(horizontal = 7.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texts.getString(R.string.ui_view_orders_412), fontFamily = SiestaManrope, fontWeight = FontWeight.Bold,
            fontSize = 14.sp, color = SiestaBackgroundDark, textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 46.dp)
        )
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(SiestaCtaInsetTop, SiestaCtaInsetBottom)))
                .border(1.dp, SiestaCtaGoldLight.copy(alpha = 179 / 255f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null,
                tint = SuccessCream, modifier = Modifier.size(23.dp)
            )
        }
    }
}
