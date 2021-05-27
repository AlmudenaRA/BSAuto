package com.example.bsauto.myposts

import java.io.Serializable

open class Post (
    var brand: String = "",
    var fuel: String = "",
    var change: String = "",
    var km: String = "",
    var cv: String = "",
    var year: String = "",
    var price: String = "",
    var description: String = "",
    var province: String = "",
    var city: String = "",
    var name: String = "",
    var phone: String = "",
    var image: String = ""

) : Serializable{

    override fun toString(): String {
        return "Post(brand='$brand', fuel='$fuel', change='$change', km='$km', cv='$cv', year='$year', price='$price', description='$description', province='$province', city='$city', name='$name', phone='$phone', image='$image')"
    }
}
