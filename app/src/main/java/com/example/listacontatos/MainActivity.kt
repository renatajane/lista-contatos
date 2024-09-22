package com.example.listacontatos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.listacontatos.models.Contact
import com.example.listacontatos.ui.theme.ListaContatosTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListaContatosTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(navController, innerPadding)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, padding: PaddingValues) {
    val context = LocalContext.current
    val dbHelper = ContactDatabaseHelper(context)

    Box(modifier = Modifier.padding(padding)) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") { SplashScreen(navController) }
            composable("contacts") { ContactOptionsScreen(navController) }
            composable("add_contact") { AddContactScreen(navController, dbHelper) }
            composable("view_contacts") { ViewContactsScreen(navController, dbHelper) }
            composable("edit_contact") { EditContactScreen(navController, dbHelper) }
            composable("delete_contact") { DeleteContactScreen(navController, dbHelper) }
        }
    }
}

// Botão customizado verde
@Composable
fun GreenButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Cor verde
    ) {
        content()
    }
}

// Tela de splash com a logo
@Composable
fun SplashScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4CAF50)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_phone),
                contentDescription = "Logo Telefone",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )
        }
    }

    // Aguardar 3 segundos antes de navegar para a tela de opções de contatos
    LaunchedEffect(Unit) {
        delay(3000) // 3 segundos de atraso
        navController.navigate("contacts") {
            popUpTo("splash") { inclusive = true } // Remove a tela de splash da pilha
        }
    }
}

// Tela de opções de contatos
@Composable
fun ContactOptionsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GreenButton(onClick = { navController.navigate("add_contact") }) {
            Text("Adicionar Contato")
        }
        Spacer(modifier = Modifier.height(16.dp))
        GreenButton(onClick = { navController.navigate("view_contacts") }) {
            Text("Visualizar Contatos")
        }
        Spacer(modifier = Modifier.height(16.dp))
        GreenButton(onClick = { navController.navigate("edit_contact") }) {
            Text("Editar Contato")
        }
        Spacer(modifier = Modifier.height(16.dp))
        GreenButton(onClick = { navController.navigate("delete_contact") }) {
            Text("Remover Contato")
        }
    }
}

// Adiciona contato

fun formatPhoneNumber(input: String): String {
    return when {
        input.length > 10 -> "(${input.substring(0, 2)}) ${input.substring(2, 7)}-${input.substring(7, 11)}"
        input.length > 6 -> "(${input.substring(0, 2)}) ${input.substring(2, 6)}-${input.substring(6)}"
        input.length > 2 -> "(${input.substring(0, 2)}) ${input.substring(2)}"
        else -> input
    }
}

@Composable
fun AddContactScreen(navController: NavHostController, dbHelper: ContactDatabaseHelper) {
    val nameState = remember { mutableStateOf("") }
    val phoneState = remember { mutableStateOf(TextFieldValue("")) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf("") }
    var phoneErrorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = nameState.value,
            onValueChange = {
                nameState.value = it
                nameErrorMessage = "" // Limpa a mensagem de erro ao digitar
            },
            label = { Text("Nome") },
            singleLine = true,
            isError = nameErrorMessage.isNotEmpty()
        )
        if (nameErrorMessage.isNotEmpty()) {
            Text(text = nameErrorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = phoneState.value,
            onValueChange = { input ->
                // Remove todos os caracteres que não são dígitos
                val cleanedInput = input.text.replace(Regex("[^\\d]"), "")

                // Aplica a máscara
                val formattedPhone = formatPhoneNumber(cleanedInput)

                // Atualiza o estado e posiciona o cursor no final
                phoneState.value = TextFieldValue(formattedPhone, selection = TextRange(formattedPhone.length))
                phoneErrorMessage = "" // Limpa a mensagem de erro ao digitar
            },
            label = { Text("Telefone") },
            singleLine = true,
            isError = phoneErrorMessage.isNotEmpty()
        )
        if (phoneErrorMessage.isNotEmpty()) {
            Text(text = phoneErrorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        GreenButton(onClick = {
            // Validações
            val isNameValid = nameState.value.length in 3..15 && !nameState.value.any { it.isDigit() }
            val isPhoneValid = phoneState.value.text.matches(Regex("^\\(\\d{2}\\) \\d{5}-\\d{4}$"))

            if (!isNameValid) {
                nameErrorMessage = "O nome deve ter entre 3 e 15 letras e não pode conter números."
            } else {
                nameErrorMessage = ""
            }

            if (!isPhoneValid) {
                phoneErrorMessage = "O telefone deve conter apenas 11 números."
            } else {
                phoneErrorMessage = ""
            }

            if (isNameValid && isPhoneValid) {
                // Insere o contato no banco de dados
                dbHelper.insertContact(nameState.value, phoneState.value.text)
                showSuccessMessage = true // Exibe a mensagem de sucesso
                nameState.value = "" // Limpa o campo
                phoneState.value = TextFieldValue("") // Limpa o campo
            }
        }) {
            Text("Adicionar Contato")
        }

        Spacer(modifier = Modifier.height(16.dp))

        GreenButton(onClick = { navController.navigate("contacts") }) {
            Text("Voltar para o Menu")
        }

        // Exibe a mensagem de sucesso se o contato for criado
        if (showSuccessMessage) {
            Text(text = "Contato criado com sucesso!", color = Color.Green)
        }
    }
}


// Visualizar contatos
@Composable
fun ViewContactsScreen(navController: NavHostController, dbHelper: ContactDatabaseHelper) {
    val contacts = dbHelper.getAllContacts()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Lista de Contatos", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(16.dp))

        // Lista de contatos
        for (contact in contacts) {
            Text(
                text = "${contact.name}: ",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(end = 4.dp) // Espaçamento entre o nome e o número
            )
            Text(
                text = contact.phone,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        GreenButton(onClick = { navController.navigate("contacts") }) {
            Text("Voltar para o Menu")
        }
    }
}


// Edita contatos
@Composable
fun EditContactScreen(navController: NavHostController, dbHelper: ContactDatabaseHelper) {
    val contacts = dbHelper.getAllContacts()
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    val nameState = remember { mutableStateOf("") }
    val phoneState = remember { mutableStateOf(TextFieldValue("")) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf("") }
    var phoneErrorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Editar Contato", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Selecione um contato para editar:", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // Lista de contatos
        for (contact in contacts) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedContact = contact
                        nameState.value = contact.name
                        phoneState.value = TextFieldValue(formatPhoneNumber(contact.phone.replace(Regex("[^\\d]"), "")))
                        nameErrorMessage = "" // Limpa mensagem de erro ao selecionar
                        phoneErrorMessage = ""
                    }
                    .padding(8.dp)
                    .background(
                        if (selectedContact == contact) Color.Green.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campos de edição
        if (selectedContact != null) {
            TextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                label = { Text("Nome") },
                isError = nameErrorMessage.isNotEmpty()
            )
            if (nameErrorMessage.isNotEmpty()) {
                Text(text = nameErrorMessage, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = phoneState.value,
                onValueChange = { input ->
                    val cleanedInput = input.text.replace(Regex("[^\\d]"), "")
                    val formattedPhone = formatPhoneNumber(cleanedInput)
                    phoneState.value = TextFieldValue(formattedPhone, selection = TextRange(formattedPhone.length))
                    phoneErrorMessage = "" // Limpa a mensagem de erro ao digitar
                },
                label = { Text("Telefone") },
                isError = phoneErrorMessage.isNotEmpty()
            )
            if (phoneErrorMessage.isNotEmpty()) {
                Text(text = phoneErrorMessage, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(16.dp))

            GreenButton(onClick = {
                // Validações
                val isNameValid = nameState.value.length in 3..15 && !nameState.value.any { it.isDigit() }
                val isPhoneValid = phoneState.value.text.matches(Regex("^\\(\\d{2}\\) \\d{5}-\\d{4}$"))

                if (!isNameValid) {
                    nameErrorMessage = "O nome deve ter entre 3 e 15 letras e não pode conter números."
                } else {
                    nameErrorMessage = ""
                }

                if (!isPhoneValid) {
                    phoneErrorMessage = "O telefone deve conter apenas 11 números."
                } else {
                    phoneErrorMessage = ""
                }

                if (isNameValid && isPhoneValid) {
                    dbHelper.updateContact(selectedContact!!.id, nameState.value, phoneState.value.text)
                    showSuccessMessage = true
                }
            }) {
                Text("Salvar Alterações")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GreenButton(onClick = { navController.navigate("contacts") }) {
            Text("Voltar para o Menu")
        }

        if (showSuccessMessage) {
            Text(text = "Contato atualizado com sucesso!", color = Color(0xFF006400))
        }
    }
}

// Remove contatos
@Composable
fun DeleteContactScreen(navController: NavHostController, dbHelper: ContactDatabaseHelper) {
    val contacts = dbHelper.getAllContacts()
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Remover Contato", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // Lista de contatos para escolher qual remover
        Text(text = "Selecione um contato para remover:", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // Lista de contatos
        for (contact in contacts) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedContact = contact
                    }
                    .padding(8.dp)
                    .background(
                        if (selectedContact == contact) Color.Red.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de remover contato
        if (selectedContact != null) {
            GreenButton(onClick = {
                dbHelper.deleteContact(selectedContact!!.id)
                showSuccessMessage = true
                selectedContact = null // Limpa a seleção após remoção
            }) {
                Text("Remover Contato")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GreenButton(onClick = { navController.navigate("contacts") }) {
            Text("Voltar para o Menu")
        }

        if (showSuccessMessage) {
            Text(text = "Contato removido com sucesso!", color = Color(0xFF006400))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ListaContatosTheme {
        ContactOptionsScreen(rememberNavController())
    }
}
