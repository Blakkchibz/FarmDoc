package com.starforge.farmdoc.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.starforge.farmdoc.R
import com.starforge.farmdoc.db.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_history)
        val emptyState = view.findViewById<LinearLayout>(R.id.layout_empty_state)

        adapter = HistoryAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe the database Flow
        viewLifecycleOwner.lifecycleScope.launch {
            val scanDao = AppDatabase.getDatabase(requireContext()).scanDao()

            // collectLatest automatically updates the UI whenever the database changes
            scanDao.getAllScans().collectLatest { scans ->
                if (scans.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.setScans(scans)
                }
            }
        }

        return view
    }
}
