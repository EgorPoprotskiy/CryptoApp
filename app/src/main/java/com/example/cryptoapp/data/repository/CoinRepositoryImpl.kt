package com.example.cryptoapp.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.cryptoapp.data.database.AppDatabase
import com.example.cryptoapp.data.mapper.CoinMapper
import com.example.cryptoapp.data.network.ApiFactory
import com.example.cryptoapp.domain.CoinInfo
import com.example.cryptoapp.domain.CoinRepository
import kotlinx.coroutines.delay

class CoinRepositoryImpl(
    private val application: Application
) : CoinRepository {

    private val coinInfoDao = AppDatabase.getInstance(application).coinPriceInfoDao()
    private val apiService = ApiFactory.apiService

    private val mapper = CoinMapper()

    override fun getCoinInfoList(): LiveData<List<CoinInfo>> {
        return Transformations.map(coinInfoDao.getPriceList()) {
            it.map {
                mapper.mapDbModelToEntity(it)
            }
        }
    }

    override fun getCoinInfo(fromSymbol: String): LiveData<CoinInfo> {
        return Transformations.map(coinInfoDao.getPriceInfoAboutCoin(fromSymbol)) {
            mapper.mapDbModelToEntity(it)
        }
    }

    override suspend fun loadData() {
        //бесконечный цикл с задержкой 10 сек
        while (true) {
            try {
                //получение топ 50 валют
                val topCoins = apiService.getTopCoinsInfo(limit = 50)
                //преобразование валют в одну строку
                val fSyms = mapper.mapNamesListToString(topCoins)
                //по этой строке загружаем все данные из сети
                val jsonContainer = apiService.getFullPriceList(fSyms = fSyms)
                //преобразовали json-объекты в коллекцию объектов Dto
                val coinInfoDtoList = mapper.mapJsonContainerToListCoinInfo(jsonContainer)
                //коллекцию объектов Dto преобразовали в коллекцию объектов БД
                val dbModelList = coinInfoDtoList.map { mapper.mapDtoToDbModel(it) }
                //Вставили данные в базу
                coinInfoDao.insertPriceList(dbModelList)
            } catch (e: Exception) {
            }
            delay(10000)
        }
    }
}
