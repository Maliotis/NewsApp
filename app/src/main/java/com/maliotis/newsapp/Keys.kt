package com.maliotis.newsapp

object Keys {

    init {
        System.loadLibrary("native-lib")
    }

    /**
     * Returns the News API Key
     */
    external fun apiKey(): String
}