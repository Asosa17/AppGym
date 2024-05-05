package net.azarquiel.appgym.model

data class User(var username:String,var email:String, var pass:String, var imageUrl:String, var peso:String,var altura:String)
data class Comida(var NombreComida:String,var Cantidad:String, var Kcal100:String, var KcalTotales:String, var id:String)