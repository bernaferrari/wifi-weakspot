@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.walk

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon
import com.bernaferrari.wifiweakspot.core.rooms.RoomSuggestions

/** "Where are you?": every place as a tile, rooms you've saved highlighted, or type your own. */
@Composable
fun RoomPickerSheet(existing: List<String>, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    val scheme = MaterialTheme.colorScheme
    val saved = existing.map { it.lowercase() }.toSet()
    val places = (existing + RoomSuggestions).distinctBy { it.lowercase() }
    // A different soft shape per place keeps the grid playful without needing colour coding.
    val shapes = listOf(MaterialShapes.Cookie4Sided, MaterialShapes.Clover4Leaf, MaterialShapes.Sunny, MaterialShapes.Pill, MaterialShapes.Cookie6Sided, MaterialShapes.Puffy)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(strings.survey.whereAreYou, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
            places.chunked(3).forEachIndexed { row, chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chunk.forEachIndexed { col, place ->
                        val isSaved = place.lowercase() in saved
                        Surface(
                            onClick = { onSave(place) },
                            shape = WifiWeakspotShapes.large,
                            color = if (isSaved) scheme.secondaryContainer else scheme.surfaceContainerHigh,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ShapeIcon(
                                    spotIcon(place),
                                    shapes[(row * 3 + col) % shapes.size],
                                    containerColor = if (isSaved) scheme.secondary else scheme.primaryContainer,
                                    contentColor = if (isSaved) scheme.onSecondary else scheme.onPrimaryContainer,
                                    size = 44.dp
                                )
                                Text(place, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    repeat(3 - chunk.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.TextField(
                value = name,
                onValueChange = { name = it.take(40) },
                placeholder = { Text(strings.survey.somewhereElse) },
                singleLine = true,
                shape = WifiWeakspotShapes.full,
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = scheme.surfaceContainerHigh,
                    unfocusedContainerColor = scheme.surfaceContainerHigh,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onSave(name) }),
                trailingIcon = {
                    if (name.isNotBlank()) androidx.compose.material3.FilledIconButton(onClick = { onSave(name) }, modifier = Modifier.padding(end = 4.dp)) {
                        Icon(Icons.Rounded.Check, strings.common.save)
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            )
        }
    }
}

internal fun spotIcon(name: String): ImageVector {
    // Catalog order is stable across languages; custom names retain the English keyword fallback.
    val suggestion = RoomSuggestions.indexOfFirst { it.equals(name, ignoreCase = true) }
    val icons = listOf(
        Icons.Rounded.Weekend, Icons.Rounded.Kitchen, Icons.Rounded.Bed,
        Icons.Rounded.Computer, Icons.Rounded.ChildCare, Icons.Rounded.Bathtub,
        Icons.Rounded.DinnerDining, Icons.Rounded.DoorFront, Icons.Rounded.Garage,
        Icons.Rounded.Stairs, Icons.Rounded.Balcony, Icons.Rounded.Yard
    )
    icons.getOrNull(suggestion)?.let { return it }
    val n = name.lowercase()
    return when {
        "kitchen" in n -> Icons.Rounded.Kitchen
        "bed" in n -> Icons.Rounded.Bed
        "living" in n || "lounge" in n || "family" in n -> Icons.Rounded.Weekend
        "bath" in n || "toilet" in n -> Icons.Rounded.Bathtub
        "office" in n || "study" in n || "desk" in n -> Icons.Rounded.Computer
        "kid" in n || "nursery" in n -> Icons.Rounded.ChildCare
        "dining" in n -> Icons.Rounded.DinnerDining
        "hall" in n || "entr" in n -> Icons.Rounded.DoorFront
        "garage" in n -> Icons.Rounded.Garage
        "basement" in n || "attic" in n || "stair" in n -> Icons.Rounded.Stairs
        "balcony" in n || "terrace" in n || "patio" in n -> Icons.Rounded.Balcony
        "garden" in n || "yard" in n -> Icons.Rounded.Yard
        else -> Icons.Rounded.MeetingRoom
    }
}
