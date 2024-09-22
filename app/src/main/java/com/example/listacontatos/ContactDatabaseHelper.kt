package com.example.listacontatos

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.listacontatos.models.Contact

class ContactDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "contacts"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_PHONE TEXT)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Métodos para CRUD

    // Cria um contato novo
    fun insertContact(name: String, phone: String) {
        if (name.isNotBlank() && phone.isNotBlank()) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_NAME, name)
                put(COLUMN_PHONE, phone)
            }
            db.insert(TABLE_NAME, null, values)
            db.close()
        }
    }

    // Lista contatos
    fun getAllContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val db = readableDatabase
        val cursor: Cursor? = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        cursor?.use {
            val idIndex = it.getColumnIndex(COLUMN_ID)
            val nameIndex = it.getColumnIndex(COLUMN_NAME)
            val phoneIndex = it.getColumnIndex(COLUMN_PHONE)

            if (idIndex != -1 && nameIndex != -1 && phoneIndex != -1) {
                if (it.moveToFirst()) {
                    do {
                        val contact = Contact(
                            id = it.getInt(idIndex),
                            name = it.getString(nameIndex),
                            phone = it.getString(phoneIndex)
                        )
                        contacts.add(contact)
                    } while (it.moveToNext())
                }
            } else {
                // Aqui você pode adicionar um log ou tratamento de erro
                // Se alguma coluna não existir
            }
        }
        db.close()
        return contacts
    }

    // Atualiza contatos
    fun updateContact(id: Int, newName: String, newPhone: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("name", newName)
            put("phone", newPhone)
        }
        db.update("contacts", contentValues, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    // Remove o contato
    fun deleteContact(contactId: Int) {
        val db = this.writableDatabase
        db.delete("contacts", "id = ?", arrayOf(contactId.toString()))
        db.close()
    }

}