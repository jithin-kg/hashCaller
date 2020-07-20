package com.nibble.hashcaller.view.ui.tabian

data class BlogPost(

    var title: String,

    var body: String,

    var image: String,

    var username: String // Author of blog post


) {

    override fun toString(): String {
        return "BlogPost(title='$title', image='$image', username='$username')"
    }


}
























