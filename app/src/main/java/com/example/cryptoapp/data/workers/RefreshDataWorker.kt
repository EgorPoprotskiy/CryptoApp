package com.example.cryptoapp.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.cryptoapp.data.database.AppDatabase
import com.example.cryptoapp.data.mapper.CoinMapper
import com.example.cryptoapp.data.network.ApiFactory
import kotlinx.coroutines.delay

class RefreshDataWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private val coinInfoDao = AppDatabase.getInstance(context).coinPriceInfoDao()
    private val apiService = ApiFactory.apiService

    private val mapper = CoinMapper()

    override suspend fun doWork(): Result {
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

    companion object {
        const val NAME = "RefreshDataWorker"

        fun makeRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<RefreshDataWorker>().build()
        }
    }
}