package net.azarquiel.appgym.model

data class User(var username:String,var email:String, var pass:String, var imageUrl:String, var peso:String,var altura:String)
data class Comida(var NombreComida:String,var Cantidad:String, var Kcal100:String, var KcalTotales:String, var id:String)
data class Post(var Foto:String,var Likes:MutableList<String>, var Comentarios:MutableList<Comentario>, var PieComent:String, var Usuario:String,var Fecha:String, var id:String)
data class Comentario(var Contenido:String,var Usuario:String, var Likes:Long)
data class Rutina(var Nombre:String)
data class Peso(var fecha:String,var peso: String)
data class Ejercicio(var id:String,var NombreEj:String,var Foto:String, var Descripcion:String)