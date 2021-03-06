/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.material

import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.currentTextStyle
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.LastBaseline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.AlignmentLine
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.Placeable
import androidx.compose.ui.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.id
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.InternalTextApi
import androidx.compose.ui.text.SoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.constrain
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Material Design implementation of a
 * [Filled TextField](https://material.io/components/text-fields/#filled-text-field)
 *
 * If you are looking for an outlined version, see [OutlinedTextField].
 *
 * A simple example looks like:
 *
 * @sample androidx.compose.material.samples.SimpleTextFieldSample
 *
 * You may provide a placeholder:
 *
 * @sample androidx.compose.material.samples.TextFieldWithPlaceholder
 *
 * You can also provide leading and trailing icons:
 *
 * @sample androidx.compose.material.samples.TextFieldWithIcons
 *
 * To handle the error input state, use [isErrorValue] parameter:
 *
 * @sample androidx.compose.material.samples.TextFieldWithErrorState
 *
 * Additionally, you may provide additional message at the bottom:
 *
 * @sample androidx.compose.material.samples.TextFieldWithHelperMessage
 *
 * Password text field example:
 *
 * @sample androidx.compose.material.samples.PasswordTextField
 *
 * Hiding a software keyboard on IME action performed:
 *
 * @sample androidx.compose.material.samples.TextFieldWithHideKeyboardOnImeAction
 *
 * If apart from input text change you also want to observe the cursor location, selection range,
 * or IME composition use the TextField overload with the [TextFieldValue] parameter instead.
 *
 * @param value the input text to be shown in the text field
 * @param onValueChange the callback that is triggered when the input service updates the text. An
 * updated text comes as a parameter of the callback
 * @param label the label to be displayed inside the text field container. The default text style
 * for internal [Text] is [Typography.caption] when the text field is in focus and
 * [Typography.subtitle1] when the text field is not in focus
 * @param modifier a [Modifier] for this text field
 * @param textStyle the style to be applied to the input text. The default [textStyle] uses the
 * [currentTextStyle] defined by the theme
 * @param placeholder the optional placeholder to be displayed when the text field is in focus and
 * the input text is empty. The default text style for internal [Text] is [Typography.subtitle1]
 * @param leadingIcon the optional leading icon to be displayed at the beginning of the text field
 * container
 * @param trailingIcon the optional trailing icon to be displayed at the end of the text field
 * container
 * @param isErrorValue indicates if the text field's current value is in error. If set to true, the
 * label, bottom indicator and trailing icon will be displayed in [errorColor] color
 * @param visualTransformation transforms the visual representation of the input [value].
 * For example, you can use [androidx.compose.ui.text.input.PasswordVisualTransformation] to create a password
 * text field. By default no visual transformation is applied
 * @param keyboardType the keyboard type to be used with the text field.
 * Note that the input type is not guaranteed. For example, an IME may send a non-ASCII character
 * even if you set the keyboard type to [KeyboardType.Ascii]
 * @param imeAction the IME action honored by the IME. The 'enter' key on the soft keyboard input
 * will show a corresponding icon. For example, search icon may be shown if [ImeAction.Search] is
 * selected. When a user taps on that 'enter' key, the [onImeActionPerformed] callback is called
 * with the specified [ImeAction]
 * @param onImeActionPerformed is triggered when the input service performs an [ImeAction].
 * Note that the emitted IME action may be different from what you specified through the
 * [imeAction] field. The callback also exposes a [SoftwareKeyboardController] instance as a
 * parameter that can be used to request to hide the software keyboard
 * @param onFocusChanged a callback to be invoked when the text field receives or loses focus
 * If the boolean parameter value is `true`, it means the text field has focus, and vice versa
 * @param onTextInputStarted a callback to be invoked when the connection with the platform's text
 * input service (e.g. software keyboard on Android) has been established. Called with the
 * [SoftwareKeyboardController] instance that can be used to request to show or hide the software
 * keyboard
 * @param activeColor the color of the label, bottom indicator and the cursor when the text field is
 * in focus
 * @param inactiveColor the color of either the input text or placeholder when the text field is in
 * focus, and the color of the label and bottom indicator when the text field is not in focus
 * @param errorColor the alternative color of the label, bottom indicator, cursor and trailing icon
 * used when [isErrorValue] is set to true
 * @param backgroundColor the background color of the text field's container. To the color provided
 * here there will be applied a transparency alpha defined by Material Design specifications
 * @param shape the shape of the text field's container
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = currentTextStyle(),
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isErrorValue: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Unspecified,
    onImeActionPerformed: (ImeAction, SoftwareKeyboardController?) -> Unit = { _, _ -> },
    onFocusChanged: (Boolean) -> Unit = {},
    onTextInputStarted: (SoftwareKeyboardController) -> Unit = {},
    activeColor: Color = MaterialTheme.colors.primary,
    inactiveColor: Color = MaterialTheme.colors.onSurface,
    errorColor: Color = MaterialTheme.colors.error,
    backgroundColor: Color = MaterialTheme.colors.onSurface,
    shape: Shape =
        MaterialTheme.shapes.small.copy(bottomLeft = ZeroCornerSize, bottomRight = ZeroCornerSize)
) {
    var selection by remember { mutableStateOf(TextRange.Zero) }
    var composition by remember { mutableStateOf<TextRange?>(null) }
    @OptIn(InternalTextApi::class)
    val textFieldValue = TextFieldValue(
        text = value,
        selection = selection.constrain(0, value.length),
        composition = composition?.constrain(0, value.length)
    )

    TextFieldImpl(
        type = TextFieldType.Filled,
        value = textFieldValue,
        onValueChange = {
            selection = it.selection
            composition = it.composition
            if (value != it.text) {
                onValueChange(it.text)
            }
        },
        modifier = modifier,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leading = leadingIcon,
        trailing = trailingIcon,
        isErrorValue = isErrorValue,
        visualTransformation = visualTransformation,
        keyboardType = keyboardType,
        imeAction = imeAction,
        onImeActionPerformed = onImeActionPerformed,
        onFocusChanged = onFocusChanged,
        onTextInputStarted = onTextInputStarted,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        errorColor = errorColor,
        backgroundColor = backgroundColor,
        shape = shape
    )
}

/**
 * Material Design implementation of a
 * [Filled TextField](https://material.io/components/text-fields/#filled-text-field)
 *
 * If you are looking for an outlined version, see [OutlinedTextField].
 *
 * See example usage:
 * @sample androidx.compose.material.samples.TextFieldSample
 *
 * This overload provides access to the input text, cursor position, selection range and
 * IME composition. If you only want to observe an input text change, use the TextField
 * overload with the [String] parameter instead.
 *
 * @param value the input [TextFieldValue] to be shown in the text field
 * @param onValueChange the callback that is triggered when the input service updates values in
 * [TextFieldValue]. An updated [TextFieldValue] comes as a parameter of the callback
 * @param label the label to be displayed inside the text field container. The default text style
 * for internal [Text] is [Typography.caption] when the text field is in focus and
 * [Typography.subtitle1] when the text field is not in focus
 * @param modifier a [Modifier] for this text field
 * @param textStyle the style to be applied to the input text. The default [textStyle] uses the
 * [currentTextStyle] defined by the theme
 * @param placeholder the optional placeholder to be displayed when the text field is in focus and
 * the input text is empty. The default text style for internal [Text] is [Typography.subtitle1]
 * @param leadingIcon the optional leading icon to be displayed at the beginning of the text field
 * container
 * @param trailingIcon the optional trailing icon to be displayed at the end of the text field
 * container
 * @param isErrorValue indicates if the text field's current value is in error state. If set to
 * true, the label, bottom indicator and trailing icon will be displayed in [errorColor] color
 * @param visualTransformation transforms the visual representation of the input [value].
 * For example, you can use [androidx.compose.ui.text.input.PasswordVisualTransformation] to create a password
 * text field. By default no visual transformation is applied
 * @param keyboardType the keyboard type to be used with the text field.
 * Note that the input type is not guaranteed. For example, an IME may send a non-ASCII character
 * even if you set the keyboard type to [KeyboardType.Ascii]
 * @param imeAction the IME action honored by the IME. The 'enter' key on the soft keyboard input
 * will show a corresponding icon. For example, search icon may be shown if [ImeAction.Search] is
 * selected. When a user taps on that 'enter' key, the [onImeActionPerformed] callback is called
 * with the specified [ImeAction]
 * @param onImeActionPerformed is triggered when the input service performs an [ImeAction].
 * Note that the emitted IME action may be different from what you specified through the
 * [imeAction] field. The callback also exposes a [SoftwareKeyboardController] instance as a
 * parameter that can be used to request to hide the software keyboard
 * @param onFocusChanged a callback to be invoked when the text field receives or loses focus
 * If the boolean parameter value is `true`, it means the text field has focus, and vice versa
 * @param onTextInputStarted a callback to be invoked when the connection with the platform's text
 * input service (e.g. software keyboard on Android) has been established. Called with the
 * [SoftwareKeyboardController] instance that can be used to request to show or hide the software
 * keyboard
 * @param activeColor the color of the label, bottom indicator and the cursor when the text field is
 * in focus
 * @param inactiveColor the color of either the input text or placeholder when the text field is in
 * focus, and the color of the label and bottom indicator when the text field is not in focus
 * @param errorColor the alternative color of the label, bottom indicator, cursor and trailing icon
 * used when [isErrorValue] is set to true
 * @param backgroundColor the background color of the text field's container. To the color provided
 * here there will be applied a transparency alpha defined by Material Design specifications
 * @param shape the shape of the text field's container
 */
@Composable
fun TextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = currentTextStyle(),
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isErrorValue: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Unspecified,
    onImeActionPerformed: (ImeAction, SoftwareKeyboardController?) -> Unit = { _, _ -> },
    onFocusChanged: (Boolean) -> Unit = {},
    onTextInputStarted: (SoftwareKeyboardController) -> Unit = {},
    activeColor: Color = MaterialTheme.colors.primary,
    inactiveColor: Color = MaterialTheme.colors.onSurface,
    errorColor: Color = MaterialTheme.colors.error,
    backgroundColor: Color = MaterialTheme.colors.onSurface,
    shape: Shape =
        MaterialTheme.shapes.small.copy(bottomLeft = ZeroCornerSize, bottomRight = ZeroCornerSize)
) {
    TextFieldImpl(
        type = TextFieldType.Filled,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leading = leadingIcon,
        trailing = trailingIcon,
        isErrorValue = isErrorValue,
        visualTransformation = visualTransformation,
        keyboardType = keyboardType,
        imeAction = imeAction,
        onImeActionPerformed = onImeActionPerformed,
        onFocusChanged = onFocusChanged,
        onTextInputStarted = onTextInputStarted,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        errorColor = errorColor,
        backgroundColor = backgroundColor,
        shape = shape
    )
}

@Composable
internal fun TextFieldLayout(
    textFieldModifier: Modifier = Modifier,
    decoratedTextField: @Composable (Modifier) -> Unit,
    decoratedPlaceholder: @Composable (() -> Unit)?,
    decoratedLabel: @Composable () -> Unit,
    leading: @Composable (() -> Unit)?,
    trailing: @Composable (() -> Unit)?,
    leadingColor: Color,
    trailingColor: Color,
    labelProgress: Float,
    indicatorWidth: Dp,
    indicatorColor: Color,
    backgroundColor: Color,
    shape: Shape
) {
    // places leading icon, text field with label and placeholder, trailing icon
    IconsWithTextFieldLayout(
        modifier = textFieldModifier
            .background(
                color = backgroundColor.applyAlpha(alpha = ContainerAlpha),
                shape = shape
            )
            .drawIndicatorLine(
                lineWidth = indicatorWidth,
                color = indicatorColor
            ),
        textField = decoratedTextField,
        placeholder = decoratedPlaceholder,
        label = decoratedLabel,
        leading = leading,
        trailing = trailing,
        leadingColor = leadingColor,
        trailingColor = trailingColor,
        animationProgress = labelProgress
    )
}

/**
 * Layout of the leading and trailing icons and the input field, label and placeholder in
 * [TextField].
 */
@Composable
private fun IconsWithTextFieldLayout(
    modifier: Modifier = Modifier,
    textField: @Composable (Modifier) -> Unit,
    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)?,
    leading: @Composable (() -> Unit)?,
    trailing: @Composable (() -> Unit)?,
    leadingColor: Color,
    trailingColor: Color,
    animationProgress: Float
) {
    Layout(
        children = {
            if (leading != null) {
                Box(Modifier.layoutId("leading").iconPadding(start = HorizontalIconPadding)) {
                    Decoration(
                        contentColor = leadingColor,
                        children = leading
                    )
                }
            }
            if (trailing != null) {
                Box(Modifier.layoutId("trailing").iconPadding(end = HorizontalIconPadding)) {
                    Decoration(
                        contentColor = trailingColor,
                        children = trailing
                    )
                }
            }
            val padding = Modifier.padding(horizontal = TextFieldPadding)
            if (placeholder != null) {
                Box(
                    modifier = Modifier.layoutId(PlaceholderId).then(padding),
                    children = placeholder
                )
            }
            Box(
                modifier = Modifier
                    .layoutId(LabelId)
                    .iconPadding(
                        start = TextFieldPadding,
                        end = TextFieldPadding
                    ),
                children = label
            )
            textField(Modifier.layoutId(TextFieldId).then(padding))
        },
        modifier = modifier
    ) { measurables, incomingConstraints ->
        val baseLineOffset = FirstBaselineOffset.toIntPx()
        val bottomPadding = LastBaselineOffset.toIntPx()
        val topPadding = TextFieldTopPadding.toIntPx()
        var occupiedSpaceHorizontally = 0

        // measure leading icon
        val constraints = incomingConstraints.copy(minWidth = 0, minHeight = 0)
        val leadingPlaceable =
            measurables.find { it.id == "leading" }?.measure(constraints)
        occupiedSpaceHorizontally += widthOrZero(
            leadingPlaceable
        )

        // measure trailing icon
        val trailingPlaceable = measurables.find { it.id == "trailing" }
            ?.measure(constraints.offset(horizontal = -occupiedSpaceHorizontally))
        occupiedSpaceHorizontally += widthOrZero(
            trailingPlaceable
        )

        // measure label
        val labelConstraints = constraints
            .offset(
                vertical = -bottomPadding,
                horizontal = -occupiedSpaceHorizontally
            )
        val labelPlaceable =
            measurables.first { it.id == LabelId }.measure(labelConstraints)
        val lastBaseline = labelPlaceable[LastBaseline].let {
            if (it != AlignmentLine.Unspecified) it else labelPlaceable.height
        }
        val effectiveLabelBaseline = max(lastBaseline, baseLineOffset)

        // measure input field
        val textFieldConstraints = incomingConstraints
            .copy(minHeight = 0)
            .offset(
                vertical = -bottomPadding - topPadding - effectiveLabelBaseline,
                horizontal = -occupiedSpaceHorizontally
            )
        val textFieldPlaceable = measurables
            .first { it.id == TextFieldId }
            .measure(textFieldConstraints)

        // measure placeholder
        val placeholderConstraints = textFieldConstraints.copy(minWidth = 0)
        val placeholderPlaceable = measurables
            .find { it.id == PlaceholderId }
            ?.measure(placeholderConstraints)

        val width = calculateWidth(
            leadingPlaceable,
            trailingPlaceable,
            textFieldPlaceable,
            labelPlaceable,
            placeholderPlaceable,
            incomingConstraints
        )
        val height = calculateHeight(
            textFieldPlaceable,
            effectiveLabelBaseline,
            leadingPlaceable,
            trailingPlaceable,
            placeholderPlaceable,
            incomingConstraints,
            density
        )

        layout(width, height) {
            if (labelPlaceable.width != 0) {
                val labelEndPosition =
                    (baseLineOffset - lastBaseline).coerceAtLeast(0)
                place(
                    width,
                    height,
                    textFieldPlaceable,
                    labelPlaceable,
                    placeholderPlaceable,
                    leadingPlaceable,
                    trailingPlaceable,
                    labelEndPosition,
                    effectiveLabelBaseline + topPadding,
                    animationProgress
                )
            } else {
                // text field should be centered vertically if there is no label
                placeWithoutLabel(
                    width,
                    height,
                    textFieldPlaceable,
                    placeholderPlaceable,
                    leadingPlaceable,
                    trailingPlaceable
                )
            }
        }
    }
}

private fun calculateWidth(
    leadingPlaceable: Placeable?,
    trailingPlaceable: Placeable?,
    textFieldPlaceable: Placeable,
    labelPlaceable: Placeable,
    placeholderPlaceable: Placeable?,
    constraints: Constraints
): Int {
    val middleSection = maxOf(
        textFieldPlaceable.width,
        labelPlaceable.width,
        widthOrZero(placeholderPlaceable)
    )
    val wrappedWidth =
        widthOrZero(leadingPlaceable) + middleSection + widthOrZero(
            trailingPlaceable
        )
    return max(wrappedWidth, constraints.minWidth)
}

private fun calculateHeight(
    textFieldPlaceable: Placeable,
    labelBaseline: Int,
    leadingPlaceable: Placeable?,
    trailingPlaceable: Placeable?,
    placeholderPlaceable: Placeable?,
    constraints: Constraints,
    density: Float
): Int {
    val bottomPadding = LastBaselineOffset.value * density
    val topPadding = TextFieldTopPadding.value * density
    val inputFieldHeight = max(textFieldPlaceable.height, heightOrZero(placeholderPlaceable))
    val middleSectionHeight = labelBaseline + topPadding + inputFieldHeight + bottomPadding
    return maxOf(
        middleSectionHeight.roundToInt(),
        max(heightOrZero(leadingPlaceable), heightOrZero(trailingPlaceable)),
        constraints.minHeight
    )
}

/**
 * Places the provided text field, placeholder and label with respect to the baseline offsets in
 * [TextField]
 */
private fun Placeable.PlacementScope.place(
    width: Int,
    height: Int,
    textfieldPlaceable: Placeable,
    labelPlaceable: Placeable,
    placeholderPlaceable: Placeable?,
    leadingPlaceable: Placeable?,
    trailingPlaceable: Placeable?,
    labelEndPosition: Int,
    textPosition: Int,
    animationProgress: Float
) {
    leadingPlaceable?.place(
        0,
        Alignment.CenterVertically.align(height - leadingPlaceable.height)
    )
    trailingPlaceable?.place(
        width - trailingPlaceable.width,
        Alignment.CenterVertically.align(height - trailingPlaceable.height)
    )
    val labelCenterPosition = Alignment.CenterStart.align(
        IntSize(
            width - labelPlaceable.width,
            height - labelPlaceable.height
        )
    )
    val labelDistance = labelCenterPosition.y - labelEndPosition
    val labelPositionY =
        labelCenterPosition.y - (labelDistance * animationProgress).roundToInt()
    labelPlaceable.place(widthOrZero(leadingPlaceable), labelPositionY)

    textfieldPlaceable.place(widthOrZero(leadingPlaceable), textPosition)
    placeholderPlaceable?.place(widthOrZero(leadingPlaceable), textPosition)
}

/**
 * Places the provided text field and placeholder center vertically in [TextField]
 */
private fun Placeable.PlacementScope.placeWithoutLabel(
    width: Int,
    height: Int,
    textPlaceable: Placeable,
    placeholderPlaceable: Placeable?,
    leadingPlaceable: Placeable?,
    trailingPlaceable: Placeable?
) {
    leadingPlaceable?.place(
        0,
        Alignment.CenterVertically.align(height - leadingPlaceable.height)
    )
    trailingPlaceable?.place(
        width - trailingPlaceable.width,
        Alignment.CenterVertically.align(height - trailingPlaceable.height)
    )
    textPlaceable.place(
        widthOrZero(leadingPlaceable),
        Alignment.CenterVertically.align(height - textPlaceable.height)
    )
    placeholderPlaceable?.place(
        widthOrZero(leadingPlaceable),
        Alignment.CenterVertically.align(height - placeholderPlaceable.height)
    )
}

/**
 * A draw modifier that draws a bottom indicator line in [TextField]
 */
private fun Modifier.drawIndicatorLine(lineWidth: Dp, color: Color): Modifier {
    return drawBehind {
        val strokeWidth = lineWidth.value * density
        val y = size.height - strokeWidth / 2
        drawLine(
            color,
            Offset(0f, y),
            Offset(size.width, y),
            strokeWidth
        )
    }
}

private val FirstBaselineOffset = 20.dp
private val LastBaselineOffset = 10.dp
private val TextFieldTopPadding = 4.dp
private const val ContainerAlpha = 0.12f