package com.example.listacontatos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Visibility
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

// Botões customizados
@Composable
fun GreenButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Cor verde
    ) {
        content()
    }
}

// Com a borda verde
@Composable
fun BorderedGreenButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White), // Fundo branco
        border = BorderStroke(2.dp, Color(0xFF4CAF50)) // Borda verde
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

@Composable
fun ContactOptionsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(), // Preenche 100% da largura e altura
        verticalArrangement = Arrangement.SpaceEvenly // Espaça os botões igualmente
    ) {
        // Botão para adicionar contato
        OptionButton(
            onClick = { navController.navigate("add_contact") },
            text = "Adicionar Contato",
            icon = Icons.Default.Add,
            color = Color(0xFF4CAF50) // Cor verde para adicionar
        )

        // Botão para visualizar contatos
        OptionButton(
            onClick = { navController.navigate("view_contacts") },
            text = "Visualizar Contatos",
            icon = Icons.Default.List,
            color = Color(0xFF81C784 ) // Cor azul para visualizar
        )

        // Botão para editar contato
        OptionButton(
            onClick = { navController.navigate("edit_contact") },
            text = "Editar Contato",
            icon = Icons.Default.Edit,
            color = Color(0xFFA5D6A7 ) // Cor amarela para editar
        )

        // Botão para remover contato
        OptionButton(
            onClick = { navController.navigate("delete_contact") },
            text = "Remover Contato",
            icon = Icons.Default.Delete,
            color = Color(0xFFC8E6C9 ) // Cor vermelha para remover
        )
    }
}

@Composable
fun OptionButton(onClick: () -> Unit, text: String, icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth() // Preenche toda a largura da tela
            .height(200.dp) // Define uma altura fixa para o botão
            .clickable(onClick = onClick) // Torna o Box clicável
            .background(color) // Aplica a cor de fundo aqui
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Adiciona preenchimento
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp)) // Espaço entre ícone e texto
            Text(text)
        }
    }
}


// Função para formatar telefone
fun formatPhoneNumber(input: String): String {
    return when {
        input.length > 10 -> "(${input.substring(0, 2)}) ${input.substring(2, 7)}-${
            input.substring(
                7,
                11
            )
        }"

        input.length > 6 -> "(${input.substring(0, 2)}) ${
            input.substring(
                2,
                6
            )
        }-${input.substring(6)}"

        input.length > 2 -> "(${input.substring(0, 2)}) ${input.substring(2)}"
        else -> input
    }
}

// Função para adicionar um novo contato
@Composable
fun AddContactScreen(navController: NavHostController, dbHelper: ContactDatabaseHelper) {
    val nameState = remember { mutableStateOf("") }
    val phoneState = remember { mutableStateOf(TextFieldValue("")) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf("") }
    var phoneErrorMessage by remember { mutableStateOf("") }

    // Layout principal com cabeçalho e rodapé
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween // Ajusta para espaço entre
    ) {
        // Cabeçalho verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Altura do cabeçalho
                .background(Color(0xFF4CAF50)), // Cor verde
            contentAlignment = Alignment.Center // Alinha o conteúdo no centro
        ) {
            Text(
                text = "Adicionar Contato",
                color = Color.White, // Cor do texto
                fontSize = 20.sp // Tamanho da fonte
            )
        }

        // Conteúdo principal
        Column(
            modifier = Modifier
                .padding(16.dp) // Adiciona padding apenas ao conteúdo
                .fillMaxWidth(), // Garante que a coluna ocupe toda a largura
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
                    phoneState.value =
                        TextFieldValue(formattedPhone, selection = TextRange(formattedPhone.length))
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
                val isNameValid =
                    nameState.value.length in 3..15 && !nameState.value.any { it.isDigit() }
                val isPhoneValid =
                    phoneState.value.text.matches(Regex("^\\(\\d{2}\\) \\d{5}-\\d{4}$"))

                if (!isNameValid) {
                    nameErrorMessage =
                        "O nome deve ter entre 3 e 15 letras e não pode conter números."
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

            Spacer(modifier = Modifier.height(20.dp))
            BorderedGreenButton(onClick = { navController.navigate("contacts") }) {
                Text(text = "Voltar", color = Color(0xFF4CAF50))
            }

            // Exibe a mensagem de sucesso se o contato for criado
            if (showSuccessMessage) {
                Text(text = "Contato criado com sucesso!", color = Color(0xFF006400))
            }
        }

        // Rodapé verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Altura do rodapé
                .background(Color(0xFF4CAF50)), // Cor verde
            contentAlignment = Alignment.Center // Alinha o conteúdo no centro
        ) {
            Text(
                text = "",
                color = Color.White, // Cor do texto
                fontSize = 14.sp // Tamanho da fonte
            )
        }
    }
}

// Visualizar contatos
@Composable
fun ViewContactsScreen(navController: NavHostController, dbHelper: ContactDatabaseHelper) {
    val contacts = dbHelper.getAllContacts()

    // Layout principal com cabeçalho e rodapé
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween // Ajusta para espaço entre
    ) {
        // Cabeçalho verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Altura do cabeçalho
                .background(Color(0xFF4CAF50)), // Cor verde
            contentAlignment = Alignment.Center // Alinha o conteúdo no centro
        ) {
            Text(
                text = "Lista de Contatos",
                color = Color.White, // Cor do texto
                fontSize = 20.sp // Tamanho da fonte
            )
        }

        // Conteúdo principal
        Column(
            modifier = Modifier
                .weight(1f) // Permite que esta coluna ocupe o espaço restante
                .padding(top = 8.dp, start = 10.dp), // Adiciona padding apenas no topo e à esquerda
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (contacts.isEmpty()) {
                // Mensagem caso não haja contatos
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Centraliza o conteúdo
                    modifier = Modifier.fillMaxWidth() // Ocupa toda a largura
                ) {
                    Text(
                        text = "Ainda não há contatos salvos.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray // Cor da mensagem
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Espaçamento entre a mensagem e o botão

                    // Botão "Adicionar Contato" centralizado
                                            GreenButton(onClick = { navController.navigate("add_contact") }) {
                        Text(text = "Adicionar Contato", color = Color.White)
                    }
                }
            } else {
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
                    Spacer(modifier = Modifier.height(8.dp)) // Espaçamento entre contatos
                }
            }
            // Botão "Voltar"
            Spacer(modifier = Modifier.height(16.dp))

            // Botão "Voltar" centralizado
            Box(
                modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura
                contentAlignment = Alignment.Center // Centraliza o botão
            ) {
                BorderedGreenButton(onClick = { navController.navigate("contacts") }) {
                    Text(text = "Voltar", color = Color(0xFF4CAF50))
                }
            }
        }

        // Espaço entre o botão "Voltar" e o rodapé
        Spacer(modifier = Modifier.height(16.dp))

        // Rodapé verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Altura do rodapé
                .background(Color(0xFF4CAF50)), // Cor verde
            contentAlignment = Alignment.Center // Alinha o conteúdo no centro
        ) {
            GreenButton(onClick = { navController.navigate("contacts") }) {
                Text(text = "", color = Color.White)
            }
        }
    }
}


// Editar contatos
@Composable
fun EditContactScreen(navController: NavHostController, dbHelper: ContactDatabaseHelper) {
    val contacts = dbHelper.getAllContacts()
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    val nameState = remember { mutableStateOf("") }
    val phoneState = remember { mutableStateOf(TextFieldValue("")) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var nameErrorMessage by remember { mutableStateOf("") }
    var phoneErrorMessage by remember { mutableStateOf("") }

    // Layout principal com cabeçalho e rodapé
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween // Ajusta para espaço entre
    ) {
        // Cabeçalho verde com botão de voltar e título
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4CAF50)) // Cor verde
        ) {
            // Título
            Text(
                text = "Editar Contato",
                color = Color.White, // Cor do texto
                fontSize = 20.sp, // Tamanho da fonte
                modifier = Modifier.padding(16.dp) // Adiciona padding ao texto
            )
        }

        // Conteúdo principal
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp), // Adiciona padding ao conteúdo
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally // Alinha itens no centro horizontalmente
        ) {
            if (contacts.isEmpty()) {
                // Mensagem caso não haja contatos
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Centraliza o conteúdo
                    modifier = Modifier.fillMaxWidth() // Ocupa toda a largura
                ) {
                    Text(
                        text = "Ainda não há contatos salvos.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray // Cor da mensagem
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Espaçamento entre a mensagem e o botão

                    // Botão "Adicionar Contato" centralizado
                    GreenButton(onClick = { navController.navigate("add_contact") }) {
                        Text(text = "Adicionar Contato", color = Color.White)
                    }
                }
            } else {
                // Apenas mostra a mensagem de selecionar contato se houver contatos
                Text(
                    text = "Selecione um contato para editar:",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Lista de contatos
                for (contact in contacts) {
                    val isSelected = selectedContact == contact
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedContact = contact
                                nameState.value = contact.name
                                phoneState.value = TextFieldValue(
                                    formatPhoneNumber(
                                        contact.phone.replace(Regex("[^\\d]"), "")
                                    )
                                )
                            }
                            .padding(8.dp)
                            .background(if (isSelected) Color(0xFF81C784) else Color.Transparent) // Altera a cor de fundo se selecionado
                    ) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black // Altera a cor do texto se selecionado
                        )
                    }
                }

                // Campos para editar
                Spacer(modifier = Modifier.height(16.dp))
                if (selectedContact != null) {
                    // Usando um Box para centralizar
                    Box(modifier = Modifier.fillMaxWidth(0.8f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Campo de nome
                            TextField(
                                value = nameState.value,
                                onValueChange = {
                                    nameState.value = it
                                    nameErrorMessage = "" // Limpa a mensagem de erro ao digitar
                                },
                                label = { Text("Nome") },
                                singleLine = true,
                                isError = nameErrorMessage.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth() // Preenche a largura do Box
                            )
                            if (nameErrorMessage.isNotEmpty()) {
                                Text(text = nameErrorMessage, color = Color.Red)
                            }


                            Spacer(modifier = Modifier.height(8.dp))

                            // Campo de telefone
                            TextField(
                                value = phoneState.value,
                                onValueChange = { input ->
                                    // Remove todos os caracteres que não são dígitos
                                    val cleanedInput = input.text.replace(Regex("[^\\d]"), "")
                                    // Aplica a máscara
                                    val formattedPhone = formatPhoneNumber(cleanedInput)
                                    // Atualiza o estado e posiciona o cursor no final
                                    phoneState.value = TextFieldValue(
                                        formattedPhone,
                                        selection = TextRange(formattedPhone.length)
                                    )
                                    phoneErrorMessage = "" // Limpa a mensagem de erro ao digitar
                                },
                                label = { Text("Telefone") },
                                singleLine = true,
                                isError = phoneErrorMessage.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth() // Preenche a largura do Box
                            )
                            if (phoneErrorMessage.isNotEmpty()) {
                                Text(text = phoneErrorMessage, color = Color.Red)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botão de salvar alterações
                            GreenButton(onClick = {
                                // Validações
                                val isNameValid =
                                    nameState.value.length in 3..15 && !nameState.value.any { it.isDigit() }
                                val isPhoneValid =
                                    phoneState.value.text.matches(Regex("^\\(\\d{2}\\) \\d{5}-\\d{4}$"))

                                if (!isNameValid) {
                                    nameErrorMessage =
                                        "O nome deve ter entre 3 e 15 letras e não pode conter números."
                                } else {
                                    nameErrorMessage = ""
                                }

                                if (!isPhoneValid) {
                                    phoneErrorMessage = "O telefone deve conter apenas 11 números."
                                } else {
                                    phoneErrorMessage = ""
                                }

                                if (isNameValid && isPhoneValid) {
                                    // Atualiza o contato no banco de dados
                                    dbHelper.updateContact(
                                        selectedContact!!.id,
                                        nameState.value,
                                        phoneState.value.text
                                    )
                                    showSuccessMessage = true // Exibe a mensagem de sucesso
                                }
                            }) {
                                Text("Salvar Alterações")
                            }

                            // Mensagem de sucesso centralizada abaixo do botão "Salvar Alterações"
                            if (showSuccessMessage) {
                                Spacer(modifier = Modifier.height(8.dp)) // Espaço entre o botão e a mensagem
                                Text(text = "Contato editado com sucesso!", color = Color(0xFF006400))
                            }
                        }
                    }
                }
            }

            // Botão "Voltar" centralizado
            Box(
                modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura
                contentAlignment = Alignment.Center // Centraliza o botão
            ) {
                BorderedGreenButton(onClick = { navController.navigate("contacts") }) {
                    Text(text = "Voltar", color = Color(0xFF4CAF50))
                }
            }
        }

        // Rodapé verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Altura do rodapé
            contentAlignment = Alignment.Center // Alinha o conteúdo no centro
        ) {
        }
    }
}

//
@Composable
fun DeleteContactScreen(navController: NavHostController, dbHelper: ContactDatabaseHelper) {
    val contacts = dbHelper.getAllContacts()
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Layout principal com cabeçalho e rodapé
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween // Ajusta para espaço entre
    ) {
        // Cabeçalho verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Altura do cabeçalho
                .background(Color(0xFF4CAF50)), // Cor verde
            contentAlignment = Alignment.Center // Alinha o conteúdo no centro
        ) {
            Text(
                text = "Remover Contato",
                color = Color.White, // Cor do texto
                fontSize = 20.sp // Tamanho da fonte
            )
        }

        // Conteúdo principal
        Column(
            modifier = Modifier
                .weight(1f) // Permite que esta coluna ocupe o espaço restante
                .padding(top = 8.dp, start = 10.dp, end = 10.dp), // Adiciona padding
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally // Centraliza horizontalmente todos os elementos
        ) {
            if (contacts.isEmpty()) {
                // Mensagem caso não haja contatos
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, // Centraliza o conteúdo
                    modifier = Modifier.fillMaxWidth() // Ocupa toda a largura
                ) {
                    Text(
                        text = "Ainda não há contatos salvos.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray // Cor da mensagem
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Espaçamento entre a mensagem e o botão

                    // Botão "Adicionar Contato" centralizado
                    GreenButton(onClick = { navController.navigate("add_contact") }) {
                        Text(text = "Adicionar Contato", color = Color.White)
                    }
                }
            } else {
                Text(
                    text = "Selecione um contato para remover:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Lista de contatos
                for (contact in contacts) {
                    val isSelected = selectedContact == contact
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedContact = contact
                            }
                            .padding(8.dp)
                            .background(if (isSelected) Color(0xFFFFCDD2) else Color.Transparent) // Vermelho claro se selecionado
                    ) {
                        Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedContact != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally // Centraliza os elementos
                    ) {
                        Text(
                            text = "Você tem certeza que deseja remover ${selectedContact!!.name}?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Red // Define a cor do texto como vermelho
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        GreenButton(onClick = {
                            // Remove o contato do banco de dados
                            dbHelper.deleteContact(selectedContact!!.id)
                            showSuccessMessage = true // Exibe a mensagem de sucesso
                        }) {
                            Text("Remover Contato")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Botão "Voltar" centralizado
            Box(
                modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura
                contentAlignment = Alignment.Center // Centraliza o botão
            ) {
                BorderedGreenButton(onClick = { navController.navigate("contacts") }) {
                    Text(text = "Voltar", color = Color(0xFF4CAF50))
                }
            }

            // Exibe a mensagem de sucesso
            if (showSuccessMessage) {
                Text(
                    text = "Contato removido com sucesso!",
                    color = Color(0xFF006400),
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }

        // Rodapé verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Altura do rodapé
                .background(Color(0xFF4CAF50)), // Cor verde
            contentAlignment = Alignment.Center // Alinha o conteúdo no centro
        ) {
            GreenButton(onClick = { navController.navigate("contacts") }) {
                Text(text = "", color = Color.White)
            }
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
