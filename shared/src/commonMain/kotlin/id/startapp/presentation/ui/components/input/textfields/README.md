# Backbone TextField Components

A comprehensive library of specialized TextField components for the Backbone mobile app.

## 📁 File Structure

```
textfields/
├── TextFieldValidation.kt      # Validation utilities & common patterns
├── TextFields.kt                # Type aliases for convenience
│
├── EmailTextField.kt            # Email input with validation
├── PasswordTextField.kt         # Password with strength meter
├── PhoneTextField.kt            # Phone number (Indonesia format)
├── PostalCodeTextField.kt       # 5-digit postal code
├── PriceTextField.kt            # Price with currency prefix
├── NumberTextField.kt           # Numeric with min/max & steppers
├── NameTextField.kt             # Person/business names
├── UrlTextField.kt              # Website/social media URLs
├── SearchTextField.kt           # Search with debounce & clear
├── NotesTextField.kt            # Multiline notes with counter
│
└── domain/
    ├── OutletCodeTextField.kt       # Auto-generated outlet code
    ├── ProviderCodeTextField.kt     # Auto-generated provider code
    ├── WeightTextField.kt           # Weight (kg)
    ├── QuantityTextField.kt         # Order quantity with steppers
    ├── PercentageTextField.kt       # 0-100% input
    └── TimeTextField.kt             # HH:MM format (24-hour)
```

## 🚀 Quick Start

### Basic Usage

```kotlin
import id.startapp.presentation.ui.components.input.textfields.*

@Composable
fun MyForm() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column {
        EmailTextField(
            value = email,
            onValueChange = { email = it }
        )

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            showStrengthIndicator = true,
            showRequirements = true
        )
    }
}
```

### Domain-Specific TextFields

```kotlin
import id.startapp.presentation.ui.components.input.textfields.domain.*

@Composable
fun OrderForm() {
    var weight by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    Column {
        WeightTextField(
            value = weight,
            onValueChange = { weight = it },
            min = 0.1,
            max = 50.0
        )

        QuantityIntTextField(
            value = quantity.toInt(),
            onValueChange = { quantity = it.toString() },
            min = 1,
            max = 100,
            showSteppers = true
        )
    }
}
```

## 📦 Component Reference

### Essential TextFields

| Component | Purpose | Key Features |
|-----------|---------|--------------|
| `EmailTextField` | Email input | Email validation, keyboard type, icon |
| `PasswordTextField` | Password input | Show/hide toggle, strength meter, requirements list |
| `ConfirmPasswordTextField` | Password confirmation | Matching validation |
| `PhoneTextField` | Phone number | Indonesia format, auto-format to +62 |
| `WhatsAppTextField` | WhatsApp number | Optional by default |
| `PostalCodeTextField` | Postal code | 5-digit validation, auto-filter |
| `PriceTextField` | Price/amount | Currency prefix, formatting, min/max |
| `NumberTextField` | Numeric input | Min/max, optional steppers |
| `IntegerTextField` | Integer input | Direct Int value support |
| `NameTextField` | Names | Capitalization, validation |
| `FirstNameTextField` | First name | Single word, min 2 chars |
| `LastNameTextField` | Last name | Hyphen/apostrophe support |
| `BusinessNameTextField` | Business names | Longer, more characters |
| `UrlTextField` | Website URL | Auto-adds https:// prefix |
| `SocialMediaUrlTextField` | Social media | Platform-specific |
| `SearchTextField` | Search input | Debounce (300ms), clear button |
| `NotesTextField` | Multiline notes | Character counter, max length |
| `OrderNotesTextField` | Order notes | Pre-configured for orders |
| `SpecialInstructionsTextField` | Instructions | Pre-configured warnings |
| `AddressTextField` | Address | Multiline, 500 chars |
| `DescriptionTextField` | Description | Long-form, 1000 chars |

### Domain-Specific TextFields (Backbone)

| Component | Purpose | Key Features |
|-----------|---------|--------------|
| `OutletCodeTextField` | Outlet ID | Auto-generated, copy button, read-only |
| `ProviderCodeTextField` | Provider ID | Auto-generated, copy button, read-only |
| `WeightTextField` | Weight | kg suffix, 0.1-50kg range, decimal |
| `QuantityTextField` | Order quantity | Stepper buttons, 1-100 range |
| `QuantityIntTextField` | Quantity (Int) | Direct Int support |
| `PercentageTextField` | Percentage | 0-100%, % suffix, decimal |
| `PercentageDoubleTextField` | Percentage (Double) | Double value support |
| `TimeTextField` | Time input | HH:MM format, auto-format |
| `OperatingHoursField` | Hours range | Open/close time combined |

## 🎨 Common Parameters

All TextFields inherit from `AppTextField` and support:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `value` | `String` | *required* | Current field value |
| `onValueChange` | `(String) -> Unit` | *required* | Value change callback |
| `modifier` | `Modifier` | `Modifier` | Layout modifier |
| `externalLabel` | `String?` | `null` | Label above field |
| `externalLabelRequired` | `Boolean` | `false` | Show asterisk (*) |
| `placeholder` | `String?` | `null` | Placeholder text |
| `enabled` | `Boolean` | `true` | Enable/disable field |
| `readOnly` | `Boolean` | `false` | Read-only mode |
| `error` | `String?` | `null` | External error message |

## 🔒 Validation Features

### Validation Types

- **Real-time validation** on blur
- **Format validation** (email, phone, URL, etc.)
- **Range validation** (min/max for numbers, prices, dates)
- **Length validation** (min/max characters)
- **Pattern validation** (regex-based)

### Error Handling

```kotlin
EmailTextField(
    value = email,
    onValueChange = { email = it },
    onError = { errorMessage ->
        // Handle validation error
        viewModel.setEmailError(errorMessage)
    },
    error = externalError // Override internal validation
)
```

## 🎯 Design Patterns

### State Holder Pattern

```kotlin
data class FormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null
)

@Composable
fun rememberFormState() = remember { FormState() }
```

### Validation Callback

```kotlin
var emailError by remember { mutableStateOf<String?>(null) }

EmailTextField(
    value = email,
    onValueChange = { email = it },
    onError = { emailError = it },
    error = emailError
)
```

## 🔧 Customization

### Currency Symbol

```kotlin
PriceTextField(
    value = price,
    onValueChange = { price = it },
    currency = "Rp"  // Default: "Rp"
)
```

### Min/Max Values

```kotlin
NumberTextField(
    value = quantity,
    onValueChange = { quantity = it },
    min = 1,
    max = 100
)
```

### Stepper Buttons

```kotlin
QuantityIntTextField(
    value = quantity,
    onValueChange = { quantity = it },
    showSteppers = true,
    step = 1
)
```

## 📝 Best Practices

1. **Use specialized TextFields** instead of generic `AppTextField` for consistency
2. **Handle errors** via `onError` callback for form-wide validation
3. **Use external error state** for multi-field validation scenarios
4. **Set appropriate min/max** values for numeric inputs
5. **Use `allowEmpty`** for optional fields

## 🐛 Troubleshooting

### Import Issues

```kotlin
// Import individual components
import id.startapp.presentation.ui.components.input.textfields.EmailTextField

// Or use wildcard
import id.startapp.presentation.ui.components.input.textfields.*
import id.startapp.presentation.ui.components.input.textfields.domain.*
```

### Validation Not Working

Ensure `onBlur` is triggered - validation runs on focus loss:
```kotlin
// ✅ Correct - validation on blur
AppTextField(
    // ...
    onBlur = { /* validates */ }
)

// ❌ Wrong - no blur handler
TextField(
    // ... no validation
)
```

## 📚 Related Components

- `AppTextField` - Base text field component
- `LocationDropdown` - Cascading location dropdowns
- `DatePickerField` - Date selection (if needed)
- `FileUploadField` - File attachment (if needed)

## 🔄 Migration Guide

### From AppTextField to Specialized Fields

**Before:**
```kotlin
AppTextField(
    value = email,
    onValueChange = { email = it },
    externalLabel = "Email",
    keyboardOptions = KeyboardOptions(KeyboardType.Email),
    leadingIcon = { Icon(Icons.Rounded.Email, null) },
    onBlur = { /* validate */ }
)
```

**After:**
```kotlin
EmailTextField(
    value = email,
    onValueChange = { email = it }
    // Validation built-in!
)
```
