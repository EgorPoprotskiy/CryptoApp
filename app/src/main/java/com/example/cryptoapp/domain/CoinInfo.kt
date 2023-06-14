package com.example.cryptoapp.domain
//Перенесли в этот класс все поля, которые используются в адаптере(CoinInfoAdapter) и в представлении(CoinDetailActivity)
//Это бизнес-логика
data class CoinInfo(
    val fromSymbol: String,
    val toSymbol: String?,
    val price: String?,
    val lastUpdate: String,
    val highDay: String?,
    val lowDay: String?,
    val lastMarket: String?,
    val imageUrl: String
)
