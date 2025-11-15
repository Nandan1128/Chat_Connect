package com.example.chatconnect.Data_Model

class User {
    var name : String? = null
    var email: String? = null
    var uid: String? = null
    var phone: String? = null


    constructor(){}

    constructor(name: String?, email: String?, uid: String?,phone: String?){
        this.name = name
        this.email = email
        this.uid = uid
        this.phone = phone
    }


}