package com.example.cryptoapp.domain

import androidx.lifecycle.LiveData

interface CoinRepository {
    //Метод получения списка
    fun getCoinInfoList(): LiveData<List<CoinInfo>>
    //Метод возвращает информацию о конкретной валюте
    fun getCoinInfo(fromSymbol: String): LiveData<CoinInfo>
    //загрузка данных(этот процесс асинхронный и его нельзя выполнять на главном потоке,
    //поэтому метод помечен suspend
    suspend fun loadData()
}
