/*
 * Copyright [2024] [Halil İbrahim ÇAKIR]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
Author/Developer: Halil İbrahim ÇAKIR
GitHub: https://github.com/Halil-UtrioN
NOTE: The project is mixed in Turkish and English.
*/
package com.cakirhalil.fotopaylama.fragmnet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.cakirhalil.fotopaylama.model.Post
import com.cakirhalil.fotopaylama.R
import com.cakirhalil.fotopaylama.adapter.PostAdapter
import com.cakirhalil.fotopaylama.databinding.FragmentFeedBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    //private lateinit var popup: PopupMenu
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    val postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { floatingActionButton(it) } //FloatingActionButton butonu

        getDataFromFirestore() // Firestore veri çekme fonksiyonu

        // Recycler Adapter burada yükleniyor.
        adapter = PostAdapter(postList)
        binding.feedRYC.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRYC.adapter = adapter
        
    }
    //onViewCreated Fonksyonları
    fun floatingActionButton(view: View) {
        //FloatingActionButton fonksyonu
        val popup = PopupMenu(requireContext(), binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    // Firestore veri çekme fonksiyonu
    private fun getDataFromFirestore() {
        db.collection("posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null) {
                // Hata durumunda yapılacak işlemler.
                error.printStackTrace()
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
            } else {
                if(value != null) {
                    if (!value.isEmpty) {
                        // Veriler mevcut
                        val documents = value.documents
                        postList.clear() // Listeyi temizliyoruz.

                        // Düzenli bir şekilde verileri çekiyoruz.
                        for (document in documents) {
                            val comment = document.get("comment") as String // Yorumu çekiyoruz.
                            val userEmail = document.get("userEmail") as String //  Kullanıcı adını çekiyoruz.
                            val downloadUrl = document.get("downloadUrl") as String // Görselin URL'sini çekiyoruz.

                            val post = Post(userEmail, comment, downloadUrl) // Post nesnesini oluşturuyoruz.
                            postList.add(post) // Post listesine ekliyoruz. Recycler Adaptör'e hazır halde.
                        }
                    } else {
                        Toast.makeText(requireContext(), "Veri boş.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Veri yok.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Popup Menu Fonksyonları
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.creatPostITM) {
            //Gönderi Oluştur butonu fonksyonu
            val action = FeedFragmentDirections.actionFeedFragmentToYuklemeFragment()
            Navigation.findNavController(requireView()).navigate(action)
        } else if (item?.itemId == R.id.signOutITM) {
            //Çıkış Yap butonu fonksyonu
            auth.signOut()
            val action = FeedFragmentDirections.actionFeedFragmentToKayitFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        return true
    }
}