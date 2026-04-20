package id.startapp.pheromone.presentation.ui.components.printing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.startapp.pheromone.presentation.ui.theme.PheromoneBlue
import id.startapp.pheromone.presentation.ui.theme.ErrorRed
import id.startapp.pheromone.presentation.ui.theme.SuccessGreen
import id.startapp.pheromone.presentation.ui.theme.SurfaceGray100
import id.startapp.pheromone.presentation.util.printing.PrintResult
import id.startapp.pheromone.presentation.util.printing.PrinterDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSelectionBottomSheet(
    printers: List<PrinterDevice>,
    onDismiss: () -> Unit,
    onPrint: (PrinterDevice) -> Unit,
    isPrinting: Boolean = false,
    printResult: PrintResult? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFDDDDDD))
                )
            }
        }
    ) {
        BoxWithConstraints {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight - 50.dp)
                .padding(horizontal = 20.dp)
        ) {
            // Title
            Text(
                text = "Pilih Printer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "Hubungkan ke printer Bluetooth",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Print result feedback
            when (printResult) {
                is PrintResult.Success -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SuccessGreen.copy(alpha = 0.1f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Berhasil dicetak!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                is PrintResult.Error -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ErrorRed.copy(alpha = 0.1f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = printResult.message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ErrorRed
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                null -> { /* No result yet */ }
            }

            // Printing indicator
            if (isPrinting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PheromoneBlue.copy(alpha = 0.08f))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = PheromoneBlue,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Mencetak...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PheromoneBlue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            Spacer(modifier = Modifier.height(12.dp))

            // Printer list
            if (printers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada printer Bluetooth yang terpasang",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(printers, key = { it.address }) { printer ->
                        PrinterItem(
                            printer = printer,
                            onPrint = { onPrint(printer) },
                            isEnabled = !isPrinting
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}

@Composable
private fun PrinterItem(
    printer: PrinterDevice,
    onPrint: () -> Unit,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceGray100)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = printer.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = printer.address,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onPrint,
            enabled = isEnabled,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PheromoneBlue,
                contentColor = Color.White,
                disabledContainerColor = PheromoneBlue.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cetak",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
