package com.rishabh.monkhoodassignment.models

data class Users (
    val userId : String = "" ,
    val name : String = "",
    val imgProfile : String = "",
    val email : String = "",
    val phone : Long? = null,
    val DOB : String? = ""
)

