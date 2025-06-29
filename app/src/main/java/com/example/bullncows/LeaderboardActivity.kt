package com.example.bullncows

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullncows.adapter.LeaderboardAdapter
import com.example.bullncows.data.AppDatabase
import com.example.bullncows.databinding.ActivityLeaderboardBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        setupRecyclerView()
        setupToolbar()
        loadLeaderboard()
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter()
        binding.rvLeaderboard.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = leaderboardAdapter
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadLeaderboard() {
        lifecycleScope.launch {
            database.gameRecordDao().getTopRecords().collectLatest { records ->
                if (records.isNotEmpty()) {
                    binding.rvLeaderboard.visibility = View.VISIBLE
                    binding.tvNoRecords.visibility = View.GONE
                    leaderboardAdapter.updateRecords(records)
                } else {
                    binding.rvLeaderboard.visibility = View.GONE
                    binding.tvNoRecords.visibility = View.VISIBLE
                }
            }
        }
    }
} 