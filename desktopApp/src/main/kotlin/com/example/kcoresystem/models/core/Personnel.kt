package com.example.kcoresystem.models.core

data class Personnel(
    val id : Int,
    val num : String,
    val nom : String,
    val prenom : String,
    val postnom : String,
    var etatCivil: String,
    var fonction : String,
    var grade: Grade,
    val genre : String,
    val dateNaissance : String,
    val service: Service,
    val qualification :  Qualification
)


data class Qualification(
    val id : Int,
    val nom : String
)
data class Grade(
    val id : Int,
    val nom : String
)

data class Service(
    val id : Int,
    val nom : String
)

data class Fonction(
    val id : Int,
    val nom : String
)