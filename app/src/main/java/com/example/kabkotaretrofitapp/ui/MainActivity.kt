package com.example.kabkotaretrofitapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kabkotaretrofitapp.adapter.KabKotaAdapter
import com.example.kabkotaretrofitapp.databinding.ActivityMainBinding
import com.example.kabkotaretrofitapp.helpers.Config
import com.example.kabkotaretrofitapp.model.KabKotaItem
import com.example.kabkotaretrofitapp.response.KabKotaResponse
import com.example.kabkotaretrofitapp.retrofit.ApiConfig
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val layoutManager = LinearLayoutManager(this)
        binding.rvKabKota.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvKabKota.addItemDecoration(itemDecoration)

        getKabKotaList()

        binding.swipeRefresh.setOnRefreshListener {
            getKabKotaList()
        }
    }

    private fun getKabKotaList() {
        showLoading(true)
        val client = ApiConfig.getApiService().getListUsers()
        client.enqueue(object : Callback<KabKotaResponse> {
            override fun onResponse(
                call: Call<KabKotaResponse>,
                response: Response<KabKotaResponse>
            ) {
                showLoading(false)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val mappedData = responseBody.data?.filterNotNull()?.map {
                            KabKotaItem(
                                kabupatenKota = it.kabupatenKota ?: "",
                                jumlahPenduduk = it.jumlahPenduduk ?: 0,
                                tanggalBerdiri = it.tanggalBerdiri ?: "",
                                jumlahKecamatan = it.jumlahKecamatan ?: 0,
                                bupatiWalikota = it.bupatiWalikota ?: "",
                                pusatPemerintahan = it.pusatPemerintahan ?: "",
                                urlPetaWilayah = it.urlPetaWilayah ?: "",
                                logo = it.logo ?: "",
                                id = it.id ?: 0,
                                deskripsi = it.deskripsi ?: "",
                                jumlahKelurahan = it.jumlahKelurahan ?: 0,
                                luasWilayah = it.luasWilayah ?: "",
                                jumlahDesa = it.jumlahDesa ?: 0
                            )
                        }
                        setKabKotaData(mappedData)
                    }
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<KabKotaResponse>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun setKabKotaData(kabKota: List<KabKotaItem>?) {
        val adapter = KabKotaAdapter()
        adapter.submitList(kabKota)
        binding.rvKabKota.adapter = adapter
        
        adapter.setOnItemClickCallback(object : KabKotaAdapter.OnItemClickCallback {
            override fun onItemClicked(data: KabKotaItem) {
                val json = Gson().toJson(data)
                val moveWithObjectIntent = Intent(this@MainActivity, DetailKabKotaActivity::class.java)
                moveWithObjectIntent.putExtra(Config.EXTRA_KAB_KOTA, json)
                startActivity(moveWithObjectIntent)
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        // Tampilkan progress bar hanya jika sedang loading DAN tidak sedang swipe refresh
        if (!binding.swipeRefresh.isRefreshing) {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        // Matikan indikator swipe refresh jika loading selesai (isLoading = false)
        if (!isLoading) {
            binding.swipeRefresh.isRefreshing = false
        }
    }
}
