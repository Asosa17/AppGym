package net.azarquiel.appgym.model

data class User(var username:String,var email:String, var pass:String, var imageUrl:String, var peso:String,var altura:String)
data class Comida(var NombreComida:String,var Cantidad:String, var Kcal100:String, var KcalTotales:String, var id:String)
data class Post(var Foto:String,var Likes:Int, var Comentarios:List<Comentario>, var PieComent:String, var Usuraio:String)
data class Comentario(var Contenido:String,var Usuario:String, var Likes:Int)